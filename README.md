# Buzzword
*The table is the cable.*

Buzzword is a phone app that sends short messages between phones lying on the same table, using only vibrations. No internet, Bluetooth or Wi-Fi is involved: the table itself carries the message.

## How it works
1. **Type and flip.** You type a short message (up to about 32 characters), pick who it's for, and flip your phone face-down.
2. **The phone vibrates the message.** It turns the text into a pattern of short buzzes, a bit like Morse code.
3. **The other phones feel and hear it.** They sense the vibrations through the table and listen to the buzz, then combine both to decode the message.
4. **Errors get fixed.** Extra check information lets the receiver repair bits damaged by someone bumping the table. If the message still arrives broken, the receiver asks for it again.
5. **Delivery is confirmed.** The receiver buzzes back, and the sender sees *"Delivered to X"*.

## How the phone senses things
- **Feeling the table:** picks up the vibrations and tells whether the screen faces down.
- **Hearing the buzz:** confirms what the phone felt, so decoding is more reliable.
- **Sensing the flip:** notices the phone being turned over, which starts sending or listening.
- **Sensing it's covered:** confirms the phone is really lying face-down, so nothing starts by accident.

## Several phones on one table
- Each phone announces itself, so you can see who is at the table.
- Every message names its recipient, and the other phones ignore it.
- A phone only sends when the table is quiet, so messages don't collide.

## Security
Buzzword's security is inspired by Magic Wormhole, a tool that sends files between computers using short one-time codes made of easy words.

**Why a secret vibration code wouldn't help.** Anyone can record the vibrations and work out the pattern, so inventing a private Morse code only hides the message for a while. In Buzzword the vibration language is public on purpose. What stays secret is the key, which never travels through the table. Wi-Fi and Bluetooth are built the same way.

### Pairing with a one-time code
- **A short code of easy words.** In Secure mode, the receiving phone shows a code like `3-apple-river`. The number is the phone's seat at the table, and the words are the secret part.
- **Read it, don't say it.** The other person reads the code from the screen and types it in, so nobody nearby can overhear it.
- **A handshake that can't be cracked later.** The two phones use the code to agree on a strong key, without the code ever crossing the table. Someone who records every vibration still can't try codes afterwards to unlock it.
- **One guess only.** A faker on the table gets a single guess at the code. A wrong guess fails, and both phones show a warning.
- **Clear confirmation.** When pairing succeeds, both screens show *"Paired with Ana ✓"* and the same short number, like Bluetooth pairing, so you can see at a glance that you're connected to the right phone.
- **The code expires after one use.** Every pairing makes a new code, so an old or overheard code is useless.

### Protecting every message
- **Locked messages.** Every message is encrypted, so other phones on the table see only gibberish.
- **Tamper and replay protection.** Every message is checked and numbered, so a changed, fake or replayed message is rejected.
- **The key changes after every message.** Both phones turn the key into a new one after each message and delete the old one. Old messages stay safe even if a phone is stolen later.
- **Nothing left behind.** Secure messages open only after your fingerprint or PIN, and disappear after they are read.

### Using the table as part of the lock
- **The table is the boundary.** To listen in, someone has to be touching the same table. Wireless signals, by contrast, go through walls.
- **Knock to start.** Pairing starts only after a knock rhythm that both phones feel through the table. This proves both phones are on the same table, so nobody can relay the pairing from somewhere else.
- **Lift to wipe.** If a phone is picked up during a secure conversation (other than the owner's flip to send), it instantly destroys its key and hides the messages.
- **Sound cover (experimental).** While a secure message is vibrating, the phones play a masking sound, so a nearby microphone cannot hear the buzz. The receiver relies on feeling the table instead.

To read a secure message, an attacker would have to know a one-time code they never saw and be touching the same table, at the same time.

## Two modes
- **Open mode:** fast, for anything that isn't private.
- **Secure mode:** pair once with a one-time code, then send locked messages. It's slower, but only the person you paired with can read them.

## Use cases
- Sharing a Wi-Fi password with the people at your table.
- Swapping a contact or a short code.
- Pairing two devices with no network at all.
- A quick check-in for everyone sitting at a meeting table.

## Demo
1. Send a word from one phone to another across the table.
2. Tap the table while sending: the message still arrives, and the app shows how many errors were fixed.
3. Add a third phone: only the chosen phone shows the message.
4. Lift the receiving phone off the table: nothing arrives.
5. Send in Secure mode: the third phone sees only gibberish.
6. Type a wrong code during pairing: it fails, and both phones show a warning.
