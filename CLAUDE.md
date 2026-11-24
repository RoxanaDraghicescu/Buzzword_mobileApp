# Buzzword — context pentru Claude

Proiect pentru laboratorul de **Aplicații Mobile** (Universitatea Politehnica Timișoara). Echipă de 2 persoane.
**Eu sunt Persoana 2.** Răspunde-mi în română. Codul, comentariile și textele din aplicație rămân în engleză.

## Ce e aplicația
Telefoanele așezate pe aceeași masă își trimit mesaje scurte (max. **32 de caractere**) prin **vibrații**:
emițătorul vibrează mesajul (buzz = 1, liniște = 0, ~10 biți/s), receptorul îl simte cu **accelerometrul**
(și îl aude cu microfonul). Corecție de erori (Off / Repetition×3 / Hamming(7,4)+interleaving, CRC),
confirmare „Delivered to Ana”, mod Secure (pairing cu cod „3-apple-river”, criptare, amprentă/PIN).
**Totul offline**: fără internet, Wi-Fi sau Bluetooth, inclusiv sign in (conturile sunt în baza de date locală).

## Notarea (baremul laboratorului)
Notă = CRUD 5p + Senzori/actuatori 2p + Complexitate 3p − penalizări (max 10).
- CRUD: Create, Read, Update, Delete, Persistență (date păstrate după închiderea aplicației).
- Senzori: accelerometru (primește), motor de vibrație (trimite). Trebuie să facă parte dintr-o funcționalitate reală.
- Complexitate: ≥3 ecrane cu navigare (dialogurile nu contează), autentificare reală (sign up + sign in), un extra feature (offline mode).
- **Un criteriu se punctează doar dacă îl pot explica.** Explică-mi codul pe care îl scrii, pe înțelesul meu.

## Termene (milestone-uri la laborator)
- Milestone 1 (CRUD): prezentat în 16–22 nov 2026.
- Milestone 2 (senzori: mesaj trimis prin vibrație și primit cu accelerometrul): 30 nov – 6 dec.
- Milestone 3 (complexitate: ecrane, sign up/sign in, offline): 14–20 dec.

## Cine ce face
- **Persoana 1:** SensorHub, VibrationMotor, Decoder, Encoder, corecția de erori (Corrector, NoCorrection,
  Repetition3, Hamming74, Interleaver, Crc16), Pairing, MessageLock, SessionKeys.
  **Nu modifica aceste clase fără să-mi spui.** Ea a făcut și Update + Delete pentru mesajele salvate.
- **Persoana 2 (eu):** ecranele (UI), navigarea, baza de date Room, sign up/sign in, TableManager
  (trimitere, retrimitere, „Delivered”), ChatViewModel, Settings, teste, slide-uri.
- Punctul de contact dintre noi: `TableManager.sendFrame()` și `onFrameReceived()` (bucata ei dă/primește biți,
  bucata mea gestionează mesajele).

## Plan pe săptămâni (partea mea)
- Săpt. 1 ✅ proiect, 5 ecrane + navigare, Room: SavedMessage (Create + Read), test de persistență.
- Săpt. 2 ✅ (în lucru/verificare) tabel received_messages + ecranul Receive (istoric), ecranul Diagnostics
  (grafic live accelerometru), ecranul Write (pick saved message, contor 32, alegere destinatar).
- Săpt. 3: în TableManager.send() așteaptă confirmare 3 s, retrimite de max. 3 ori, apoi „Not delivered”;
  în onFrameReceived() trimite confirmare, ignoră mesajele pentru alt telefon, salvează în istoric;
  pe Receive: grafic live + mesaj care apare literă cu literă. Demo Milestone 2 + video de rezervă.
- Săpt. 4: flip to send (când se declanșează onFlip de la Persoana 1 → ChatViewModel.send()),
  TableManager.isTableQuiet() + bară de progres; tabel User (email + parolă HASH-uită), UserDao,
  sign up (refuză email folosit), sign in, sign out, ecran Sign in pornit primul dacă nu e nimeni logat.
- Săpt. 5: ecran Settings cu alegerea corecției, ecrane de pairing, switch Open/Secure pe Table,
  ecran secure chat (mesajul se șterge la închidere, nu intră în istoric). Verificare Milestone 3 offline.
- Săpt. 6: amprentă/PIN înainte de a deschide un mesaj securizat; lista telefoanelor de la masă pe Table; slide-uri.

## Tehnologii
Kotlin, Jetpack Compose (Material 3), Navigation Compose (rute string), Room + KSP, ViewModel + StateFlow,
corutine, JUnit. Pachet: **`com.example.buzzword`**. minSdk 26. Versiunile sunt în `gradle/libs.versions.toml`.

## Structura codului (app/src/main/java/com/example/buzzword/)
- `MainActivity.kt`: entrypoint (MAIN/LAUNCHER în manifest). Creează `ChatViewModel` (by viewModels())
  și îl dă lui `BuzzwordNavHost(viewModel)`, ca toate ecranele să folosească ACELAȘI ChatViewModel.
- `ChatViewModel.kt`: phones (deocamdată demo: Ana, Bob, Cris), messages, mode, send(), pair() încă goale.
  Tot aici: `data class Phone`, `enum class Mode`, `data class Message`.
- `SavedMessagesViewModel.kt`: CRUD mesaje salvate + `validate()` + `MAX_TEXT_LENGTH = 32`.
- `ReceivedMessagesViewModel.kt`: istoricul; `simulateReceived()` e DOAR pentru test până merge decoder-ul.
- `DiagnosticsViewModel.kt`: `accelerometerFlow()` (callbackFlow, filtru de gravitație) + buffer de 300 de valori,
  ecranul se actualizează la ~33 ms. Temporar citește direct senzorul; de înlocuit cu Flow-ul din SensorHub.
- `data/`: `SavedMessage`, `SavedMessageDao`, `ReceivedMessage`, `ReceivedMessageDao`, `AppDatabase`
  (fișier `buzzword.db`, singleton, **version = 2**, `MIGRATION_1_2` creează received_messages).
- `ui/navigation/BuzzwordNavHost.kt`: enum `Screen` (Table, Saved, Write, Receive/„Inbox”, Settings) în bara de jos,
  plus ruta `DIAGNOSTICS_ROUTE` deschisă din Settings (cu buton Back).
- `ui/screens/`: `PlaceholderScreens.kt` (TableScreen + componente comune: IconBadge, EmptyState,
  ComingSoonPill, InitialAvatar), `SavedMessagesScreen.kt`, `ReceiveScreen.kt`, `WriteScreen.kt`,
  `SettingsScreen.kt`, `DiagnosticsScreen.kt`.
- `ui/theme/`: `Color.kt` + `Theme.kt` (culori proprii, portocaliu pe fundal crem, dark mode, colțuri rotunjite).
  Ecranele folosesc `MaterialTheme.colorScheme` / `MaterialTheme.shapes`, nu culori hardcodate.
- Teste: `test/SavedMessageValidationTest` (JUnit, pe calculator), `androidTest/SavedMessageDaoTest`
  (Room in-memory + test de persistență: închide și redeschide baza de date).

## Reguli și lecții învățate
- **Orice tabel nou = `version` + 1 și o `Migration`** (nu `fallbackToDestructiveMigration`, ar șterge datele
  și ar strica punctul de persistență). SQL-ul din migrare trebuie să corespundă exact cu @Entity.
- Operațiile Room sunt `suspend` și se rulează în `viewModelScope.launch`; citirile returnează `Flow`.
- În teste cu `runBlocking`, scrie `runBlocking<Unit> { ... }` (altfel testul poate returna o valoare și JUnit îl refuză).
- Nicio funcție @Composable nu trebuie declarată de două ori în același pachet (am avut o eroare cu WriteScreen dublat).
- Dialogurile și bottom sheet-urile nu sunt ecrane în barem.
- Material3 nou nu mai include iconițele: folosim `material-icons-core` (doar iconițe de bază:
  Home, Star, Edit, Email, Settings, Add, Delete, Phone, Info, Build, Check, Warning, ArrowBack etc.).
- Lucrăm pe ramura **`person2`**, apoi Pull Request în `main`. Nu face commit/push fără să-ți cer.
- Nu poți rula aplicația pe telefon: fă build (`./gradlew assembleDebug`) și teste (`./gradlew test`),
  iar eu testez pe telefon și îți spun ce văd.
