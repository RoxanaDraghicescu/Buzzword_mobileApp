import csv
import math
import re
import sys

import matplotlib.pyplot as plt

plt.rcParams["font.family"] = ["Arial", "DejaVu Sans"]
plt.rcParams["mathtext.default"] = "regular"

TEST_PATTERN = "10101100" * 3   # app's test pattern


def read_csv(path):
    with open(path) as file:
        return [{column: float(value) for column, value in row.items()}
                for row in csv.DictReader(file)]


def bit_length_from_name(path):
    # "_100ms" in the name -> 100
    match = re.search(r"_(\d+)ms", path)
    return int(match.group(1)) if match else 100


def find_pattern_start(rows):
    # motor column marks the start
    for row in rows:
        if row["motor"] == 1:
            return row["time_ms"], "motor column"
    # else: first strong buzz
    quiet_values = sorted(row["buzz"] for row in rows if row["time_ms"] < 1000)
    quiet_level = quiet_values[len(quiet_values) // 2] if quiet_values else 0.01
    for row in rows:
        if row["time_ms"] > 1000 and row["buzz"] > 5 * quiet_level + 0.02:
            return row["time_ms"], "first strong buzz"
    return None, "no buzz found"


def measure_bits(rows, pattern_start, bit_length):
    # mean buzz, middle 60% of each bit
    bit_levels = []
    for bit_index, bit in enumerate(TEST_PATTERN):
        bit_start = pattern_start + bit_index * bit_length
        middle_values = [row["buzz"] for row in rows
                         if bit_start + 0.2 * bit_length <= row["time_ms"] < bit_start + 0.8 * bit_length]
        level = sum(middle_values) / len(middle_values) if middle_values else 0.0
        bit_levels.append((bit_start, level, bit))
    return bit_levels


def count_wrong_bits(bit_levels):
    # best cut-off: fewest wrong bits
    levels = [level for _, level, _ in bit_levels]
    return min(sum(1 for _, level, bit in bit_levels if (bit == "1") != (level >= threshold))
               for threshold in levels + [max(levels) + 1])


def best_threshold(bit_levels):
    # cut-off with fewest wrong bits
    levels = sorted(level for _, level, _ in bit_levels)
    candidates = [(a + b) / 2 for a, b in zip(levels, levels[1:])]
    return min(candidates, key=lambda cut: sum(1 for _, level, bit in bit_levels if (bit == "1") != (level >= cut)))


def find_best_alignment(rows, pattern_start, bit_length):
    # scan start delay and bit length
    best = None
    for delay in range(0, 201, 5):
        for stretch_percent in range(95, 116):
            stretch = stretch_percent / 100
            bit_levels = measure_bits(rows, pattern_start + delay, bit_length * stretch)
            ones = [level for _, level, bit in bit_levels if bit == "1"]
            zeros = [level for _, level, bit in bit_levels if bit == "0"]
            score = (-count_wrong_bits(bit_levels), sum(ones) / len(ones) - sum(zeros) / len(zeros))
            if best is None or score > best[0]:
                best = (score, delay, stretch)
    return best[1], best[2]


def analyze(path):
    rows = read_csv(path)
    bit_length = bit_length_from_name(path)
    times = [row["time_ms"] for row in rows]
    duration_seconds = (times[-1] - times[0]) / 1000
    samples_per_second = len(rows) / duration_seconds if duration_seconds > 0 else 0

    print(f"\n=== {path} ===")
    print(f"Samples: {len(rows)}   Length: {duration_seconds:.1f} s   Rate: {samples_per_second:.0f} per second")
    print(f"Bit length: {bit_length} ms  ->  about {samples_per_second * bit_length / 1000:.0f} samples per bit")

    pattern_start, found_by = find_pattern_start(rows)
    if pattern_start is None:
        print("Could not find the buzz. Was the phone buzzing while recording?")
        return
    print(f"Pattern starts at {pattern_start:.0f} ms (found by {found_by})")

    # 1) as planned
    planned = measure_bits(rows, pattern_start, bit_length)
    print(f"As planned:   {count_wrong_bits(planned)} of {len(TEST_PATTERN)} bits wrong")

    # 2) best fit (late start, stretched bits)
    delay, stretch = find_best_alignment(rows, pattern_start, bit_length)
    bit_levels = measure_bits(rows, pattern_start + delay, bit_length * stretch)
    print(f"Best fit:     {count_wrong_bits(bit_levels)} of {len(TEST_PATTERN)} bits wrong "
          f"(motor starts {delay:.0f} ms late, bits are {bit_length * stretch:.0f} ms long)")

    one_levels = [level for _, level, bit in bit_levels if bit == "1"]
    zero_levels = [level for _, level, bit in bit_levels if bit == "0"]
    quiet_values = [row["buzz"] for row in rows if row["time_ms"] < pattern_start - 200]
    quiet_level = sum(quiet_values) / len(quiet_values) if quiet_values else 0.0

    weakest_one, strongest_zero = min(one_levels), max(zero_levels)
    threshold = (weakest_one + strongest_zero) / 2
    print(f"Quiet table (before the buzz): {quiet_level:.4f}")
    print(f"Bit 1: weakest {weakest_one:.4f}, average {sum(one_levels) / len(one_levels):.4f}")
    print(f"Bit 0: strongest {strongest_zero:.4f}, average {sum(zero_levels) / len(zero_levels):.4f}")
    if weakest_one > strongest_zero:
        print(f"RESULT: CLEAN. Every 1 is above every 0. Threshold = {threshold:.4f}")
        print(f"        Safety margin: weakest 1 is {weakest_one / max(strongest_zero, 1e-6):.1f}x the strongest 0")
    else:
        print(f"RESULT: MIXED. {count_wrong_bits(bit_levels)} of {len(TEST_PATTERN)} bits are on the wrong side.")

    # graph: buzz + bits, raw below
    graph_threshold = best_threshold(bit_levels)
    read_bits = ["1" if level >= graph_threshold else "0" for _, level, _ in bit_levels]
    wrong_count = sum(1 for read, (_, _, sent) in zip(read_bits, bit_levels) if read != sent)
    bit_width = bit_length * stretch
    pattern_end = bit_levels[-1][0] + bit_width
    window_start, window_end = bit_levels[0][0] - 600, pattern_end + 600
    seconds = lambda milliseconds: milliseconds / 1000

    ink, ink_soft = "#0b0b0b", "#52514e"
    blue, red, green = "#2a78d6", "#d6322f", "#1a7f4b"
    figure, (buzz_plot, raw_plot) = plt.subplots(
        2, 1, figsize=(13, 7.5), sharex=True, gridspec_kw={"height_ratios": [4, 1], "hspace": 0.12})

    # blue = 1, grey = 0
    for (bit_start, level, sent), read in zip(bit_levels, read_bits):
        tint = "#dbe8f9" if sent == "1" else "#efeeeb"
        buzz_plot.axvspan(seconds(bit_start), seconds(bit_start + bit_width), color=tint, lw=0)
        buzz_plot.axvline(seconds(bit_start), color="white", lw=1.5)
        middle = seconds(bit_start + bit_width / 2)
        buzz_plot.hlines(level, seconds(bit_start + 0.2 * bit_width), seconds(bit_start + 0.8 * bit_width),
                         colors=red if read != sent else ink, lw=3, zorder=4)
        # sent / read bits (red = wrong)
        above = buzz_plot.get_xaxis_transform()
        buzz_plot.text(middle, 1.11, sent, transform=above, ha="center", va="bottom",
                       fontsize=13, fontweight="bold", color=ink_soft)
        buzz_plot.text(middle, 1.02, read, transform=above, ha="center", va="bottom",
                       fontsize=13, fontweight="bold", color=red if read != sent else green)
    buzz_plot.text(seconds(window_start) - 0.02, 1.11, "sent", transform=buzz_plot.get_xaxis_transform(),
                   ha="right", va="bottom", fontsize=10, color=ink_soft)
    buzz_plot.text(seconds(window_start) - 0.02, 1.02, "read", transform=buzz_plot.get_xaxis_transform(),
                   ha="right", va="bottom", fontsize=10, color=ink_soft)

    buzz_plot.plot([seconds(t) for t in times], [row["buzz"] for row in rows], lw=1.2, color=blue, zorder=3)
    buzz_plot.axhline(graph_threshold, color=ink, ls="--", lw=1.2, zorder=3)
    buzz_plot.text(seconds(window_end), graph_threshold, f" cut-off {graph_threshold:.2f}\n above = 1, below = 0",
                   va="center", ha="left", fontsize=9, color=ink)
    buzz_plot.set_ylabel("vibration strength (buzz)", color=ink_soft)
    buzz_plot.set_xlim(seconds(window_start), seconds(window_end))
    buzz_plot.set_ylim(bottom=0)
    buzz_plot.tick_params(labelbottom=False)

    raw_strength = [math.sqrt(row["x"] ** 2 + row["y"] ** 2 + row["z"] ** 2) for row in rows]
    raw_plot.plot([seconds(t) for t in times], raw_strength, lw=0.8, color="#8a8984")
    raw_plot.set_ylabel("raw (m/s$^2$)\nincl. gravity", color=ink_soft, fontsize=9)
    raw_plot.set_xlabel("time (seconds)", color=ink_soft)

    for plot in (buzz_plot, raw_plot):
        for side in ("top", "right"):
            plot.spines[side].set_visible(False)
        plot.grid(axis="y", color="#e6e5e1", lw=0.8)
        plot.set_axisbelow(True)

    if wrong_count == 0:
        headline = f"All {len(TEST_PATTERN)} bits read correctly"
        headline_color = green
    else:
        headline = f"{wrong_count} of {len(TEST_PATTERN)} bits read wrong"
        headline_color = red
    figure.text(0.07, 0.975, headline, fontsize=20, fontweight="bold", color=headline_color, va="top")
    figure.text(0.07, 0.925,
                f"{path.split('/')[-1].split(chr(92))[-1]}   |   {bit_length} ms bits   |   motor starts {delay:.0f} ms late   |   "
                f"weakest 1 is {weakest_one / max(strongest_zero, 1e-6):.1f}x the strongest 0",
                fontsize=10.5, color=ink_soft, va="top")
    figure.subplots_adjust(top=0.78, left=0.07, right=0.88, bottom=0.08)

    image_path = path.rsplit(".", 1)[0] + ".png"
    plt.savefig(image_path, dpi=130)
    plt.close(figure)
    print(f"Graph saved: {image_path}")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    for csv_path in sys.argv[1:]:
        analyze(csv_path)
