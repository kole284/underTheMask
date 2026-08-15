# Under The Mask Android

Native Android klijent za postojeći Spring Boot backend. Aplikacija je napisana u Kotlinu, koristi Jetpack Compose, Material 3, MVVM, StateFlow, Hilt, Retrofit, OkHttp, DataStore i Krossbow STOMP klijent preko običnog WebSocket-a.

React frontend ostaje zasebna aplikacija i nije deo Android APK-a.

## Preduslovi

- Android Studio sa Android SDK-om
- JDK 17 ili noviji (Android Studio JBR je dovoljan)
- MySQL 8 ili kompatibilan MariaDB
- laptop i fizički telefoni na istoj lokalnoj Wi-Fi mreži

## 1. Baza

Iz korena repository-ja:

```bash
cd backend
mysql -u root -p < database/create_database.sql
```

Flyway će pri pokretanju backenda napraviti tabele i ubaciti početni katalog reči.

## 2. Backend

```bash
cd backend
mvn spring-boot:run
```

Backend podrazumevano koristi:

```properties
server.address=0.0.0.0
server.port=8080
```

`0.0.0.0` znači da server prihvata konekcije i preko LAN adrese laptopa, ne samo preko `localhost`.

## 3. LAN IP adresa laptopa

Na Linuxu pokreni:

```bash
hostname -I
```

ili:

```bash
ip addr
```

Izaberi IPv4 adresu Wi-Fi interfejsa, obično oblika `192.168.x.x` ili `10.x.x.x`. Nemoj koristiti `127.0.0.1`.

## 4. Android backend host

Kopiraj primer lokalne konfiguracije:

```bash
cd android
cp local.properties.example local.properties
```

Podesi SDK i adresu laptopa:

```properties
sdk.dir=/home/tvoj-korisnik/Android/Sdk
backend.host=192.168.1.50
```

Gradle iz ove vrednosti generiše debug URL-ove:

```text
http://HOST:8080/api/
ws://HOST:8080/ws
```

`local.properties` je ignorisan u Git-u, pa privatna LAN konfiguracija ne ulazi u source code.

Za standardni Android emulator koristi:

```properties
backend.host=10.0.2.2
```

`localhost` ili `127.0.0.1` na telefonu/emulatoru označava sam Android uređaj, ne laptop. Fizički telefon zato koristi LAN IP laptopa, dok standardni Android emulator koristi specijalnu host adresu `10.0.2.2`.

Na Androidu 17 (API 37) i novijem debug/local build pri prvom pokretanju traži sistemsku dozvolu za pristup lokalnoj mreži. Dozvola mora biti prihvaćena da bi telefon mogao da se poveže sa backendom preko LAN IP adrese; sama `INTERNET` dozvola više nije dovoljna za debug build koji gađa lokalni server. Release build koristi javni HTTPS/WSS backend i ne prikazuje local-network onboarding.

## 5. Firewall

Laptop mora dozvoliti dolazni TCP saobraćaj na portu 8080. Na Fedora Linuxu:

```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

Provera:

```bash
sudo firewall-cmd --list-ports
```

Na telefonu možeš prvo otvoriti `http://LAN_IP:8080/api/lobbies/ABC234` u browseru. Strukturisana `400` ili `404` JSON greška potvrđuje da telefon vidi backend; timeout znači da treba proveriti IP, Wi-Fi ili firewall.

## 6. Build i instalacija

Iz `android/` direktorijuma:

```bash
./gradlew test
./gradlew assembleDebug
```

Ako sistemski `java` nije pun JDK, a Android Studio je instaliran u `/opt/android-studio`:

```bash
JAVA_HOME=/opt/android-studio/jbr ./gradlew test
JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug
```

Debug APK se nalazi u:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Instalacija na USB/ADB uređaj:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Cleartext HTTP/WS je dozvoljen samo debug manifestom. Release build nema cleartext dozvolu i očekuje HTTPS/WSS vrednosti `backend.release.apiUrl` i `backend.release.wsUrl`.

Za production build protiv Hetzner deployment-a podesi u `local.properties`:

```properties
backend.release.apiUrl=https://mask.madebykole.dev/api/
backend.release.wsUrl=wss://mask.madebykole.dev/ws
```

Release build ne treba `backend.host` lokalnu IP adresu; ona ostaje samo za debug/local razvoj.

## Production Android release

Release APK mora biti potpisan privatnim keystore-om. Keystore ne sme biti u ovom repository-ju; čuvaj ga trajno i napravi backup. Isti signing key mora da se koristi za sve buduće update verzije aplikacije, inače Android neće dozvoliti instalaciju update-a preko postojeće aplikacije.

Jednom ručno generiši keystore van projekta:

```bash
mkdir -p ~/.android-keystores
keytool -genkeypair \
  -v \
  -keystore ~/.android-keystores/underthemask-release.jks \
  -alias underthemask \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

U `android/local.properties` dodaj production URL-ove i signing podatke:

```properties
backend.release.apiUrl=https://mask.madebykole.dev/api/
backend.release.wsUrl=wss://mask.madebykole.dev/ws

underthemask.signing.storeFile=/home/kole/.android-keystores/underthemask-release.jks
underthemask.signing.storePassword=...
underthemask.signing.keyAlias=underthemask
underthemask.signing.keyPassword=...
```

`local.properties`, `*.jks`, `*.keystore` i `keystore.properties` su ignorisani u Git-u. Nemoj commitovati keystore ni lozinke.

Signed release APK se pravi iz `android/` direktorijuma:

```bash
./gradlew assembleRelease
```

Ako Android Studio JBR treba eksplicitno:

```bash
JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleRelease
```

APK se nalazi u:

```text
app/build/outputs/apk/release/app-release.apk
```

Instalacija preko ADB-a:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

Ako signing properties nedostaju ili keystore fajl ne postoji, `assembleRelease` namerno pada sa jasnom porukom. Release build ne koristi debug key kao fallback.

## 7. Sesija i reconnect

DataStore čuva aktivni `lobbyCode`, `playerId` i `reconnectToken`. Token se automatski dodaje REST zahtevima kao Bearer header, ali se ne prikazuje u UI-ju i Authorization header je redigovan u debug logovima.

Pri ponovnom pokretanju aplikacija poziva reconnect endpoint. Ako je backend restartovan i in-memory lobby više ne postoji, lokalna sesija se briše i otvara se Home ekran.

## 8. Real-time komunikacija

Android koristi običan STOMP 1.2 preko WebSocket-a, bez SockJS-a:

```text
ws://HOST:8080/ws
/topic/lobbies/{LOBBY_CODE}
```

`LOBBY_UPDATED` pokreće REST osvežavanje lobbya. `GAME_UPDATED` pokreće autentifikovani `GET /game`, jer se privatna uloga i tajna reč nikad ne očekuju u javnom WebSocket payload-u.

STOMP klijent automatski pokušava reconnect. Kada WebSocket nije povezan, aktivni lobby/game ekran radi REST fallback osvežavanje na osam sekundi.

## 9. Test pune runde sa tri telefona

1. Poveži laptop i sva tri telefona na istu Wi-Fi mrežu.
2. Na sva tri build-a postavi isti `backend.host`, odnosno LAN IP laptopa.
3. Pokreni MySQL/MariaDB i Spring Boot backend.
4. Na prvom telefonu napravi lobby i zapamti kod.
5. Na drugom i trećem telefonu unesi različita imena i isti kod.
6. Potvrdi da sva tri telefona vide ista tri igrača.
7. Host po želji menja broj impostora i tip hint-a, zatim pokreće igru.
8. Na svakom telefonu otkrij privatnu ulogu, pa je ponovo sakrij.
9. Redom pošalji trag sa telefona igrača na potezu i potvrdi da chat tragova odmah raste na svim uređajima.
10. Svaki igrač izabere tačan broj osumnjičenih i glasa.
11. Posle poslednjeg glasa potvrdi isti rezultat, tajnu reč, impostore i tally na sva tri telefona.
12. Host bira `Nazad u lobby` i svi telefoni se vraćaju u isti lobby.
13. Osveži ili ponovo pokreni jednu aplikaciju i potvrdi reconnect.

## Poznata ograničenja

- Lobbyji i partije su u memoriji jednog backend procesa i nestaju posle restarta.
- Napuštanje aktivne partije resetuje rundu za preostale igrače, prema postojećem backend ponašanju.
- Backend `connected` status još nije vezan za WebSocket disconnect lifecycle.
- Nema produkcijske autentifikacije korisnika; reconnect token je identitet igrača.
- Trajno čuvanje aktivnih partija nije deo ove faze.
