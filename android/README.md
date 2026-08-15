# Under The Mask Android

Native Android klijent za postojeci Spring Boot backend. Aplikacija je napisana u Kotlinu, koristi Jetpack Compose, Material 3, MVVM, StateFlow, Hilt, Retrofit, OkHttp, DataStore i Krossbow STOMP klijent preko obicnog WebSocket-a.

React frontend ostaje zasebna aplikacija i nije deo Android APK-a.

## Preduslovi

- Android Studio sa Android SDK-om
- JDK 17 ili noviji (Android Studio JBR je dovoljan)
- MySQL 8 ili kompatibilan MariaDB
- laptop i fizicki telefoni na istoj lokalnoj Wi-Fi mrezi

## 1. Baza

Iz korena repository-ja:

```bash
cd backend
mysql -u root -p < database/create_database.sql
```

Flyway ce pri pokretanju backenda napraviti tabele i ubaciti pocetni katalog reci.

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

`0.0.0.0` znaci da server prihvata konekcije i preko LAN adrese laptopa, ne samo preko `localhost`.

## 3. LAN IP adresa laptopa

Na Linuxu pokreni:

```bash
hostname -I
```

ili:

```bash
ip addr
```

Izaberi IPv4 adresu Wi-Fi interfejsa, obicno oblika `192.168.x.x` ili `10.x.x.x`. Nemoj koristiti `127.0.0.1`.

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

Gradle iz ove vrednosti generise debug URL-ove:

```text
http://HOST:8080/api/
ws://HOST:8080/ws
```

`local.properties` je ignorisan u Git-u, pa privatna LAN konfiguracija ne ulazi u source code.

Za standardni Android emulator koristi:

```properties
backend.host=10.0.2.2
```

`localhost` ili `127.0.0.1` na telefonu/emulatoru oznacava sam Android uredjaj, ne laptop. Fizicki telefon zato koristi LAN IP laptopa, dok standardni Android emulator koristi specijalnu host adresu `10.0.2.2`.

Na Androidu 17 (API 37) i novijem aplikacija pri prvom pokretanju trazi i sistemsku dozvolu za pristup lokalnoj mrezi. Dozvola mora biti prihvacena da bi telefon mogao da se poveze sa backendom preko LAN IP adrese; sama `INTERNET` dozvola vise nije dovoljna za aplikacije koje ciljaju SDK 37.

## 5. Firewall

Laptop mora dozvoliti dolazni TCP saobracaj na portu 8080. Na Fedora Linuxu:

```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

Provera:

```bash
sudo firewall-cmd --list-ports
```

Na telefonu mozes prvo otvoriti `http://LAN_IP:8080/api/lobbies/ABC234` u browseru. Strukturisana `400` ili `404` JSON greska potvrduje da telefon vidi backend; timeout znaci da treba proveriti IP, Wi-Fi ili firewall.

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

Instalacija na USB/ADB uredjaj:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Cleartext HTTP/WS je dozvoljen samo debug manifestom. Release build nema cleartext dozvolu i ocekuje HTTPS/WSS vrednosti `backend.release.apiUrl` i `backend.release.wsUrl`.

Za production build protiv Hetzner deployment-a podesi u `local.properties`:

```properties
backend.release.apiUrl=https://GAME_DOMAIN/api/
backend.release.wsUrl=wss://GAME_DOMAIN/ws
```

`GAME_DOMAIN` je isti domen koji koristi web aplikacija. Release build ne treba `backend.host` lokalnu IP adresu; ona ostaje samo za debug/local razvoj.

## 7. Sesija i reconnect

DataStore cuva aktivni `lobbyCode`, `playerId` i `reconnectToken`. Token se automatski dodaje REST zahtevima kao Bearer header, ali se ne prikazuje u UI-ju i Authorization header je redigovan u debug logovima.

Pri ponovnom pokretanju aplikacija poziva reconnect endpoint. Ako je backend restartovan i in-memory lobby vise ne postoji, lokalna sesija se brise i otvara se Home ekran.

## 8. Real-time komunikacija

Android koristi obican STOMP 1.2 preko WebSocket-a, bez SockJS-a:

```text
ws://HOST:8080/ws
/topic/lobbies/{LOBBY_CODE}
```

`LOBBY_UPDATED` pokrece REST osvezavanje lobbya. `GAME_UPDATED` pokrece autentifikovani `GET /game`, jer se privatna uloga i tajna rec nikad ne ocekuju u javnom WebSocket payload-u.

STOMP klijent automatski pokusava reconnect. Kada WebSocket nije povezan, aktivni lobby/game ekran radi REST fallback osvezavanje na osam sekundi.

## 9. Test pune runde sa tri telefona

1. Povezi laptop i sva tri telefona na istu Wi-Fi mrezu.
2. Na sva tri build-a postavi isti `backend.host`, odnosno LAN IP laptopa.
3. Pokreni MySQL/MariaDB i Spring Boot backend.
4. Na prvom telefonu napravi lobby i zapamti kod.
5. Na drugom i trecem telefonu unesi razlicita imena i isti kod.
6. Potvrdi da sva tri telefona vide ista tri igraca.
7. Host po zelji menja broj impostora i tip hint-a, zatim pokrece igru.
8. Na svakom telefonu otkrij privatnu ulogu, pa je ponovo sakrij.
9. Redom posalji trag sa telefona igraca na potezu i potvrdi da chat tragova odmah raste na svim uredjajima.
10. Svaki igrac izabere tacan broj osumnjicenih i glasa.
11. Posle poslednjeg glasa potvrdi isti rezultat, tajnu rec, impostore i tally na sva tri telefona.
12. Host bira `Nazad u lobby` i svi telefoni se vracaju u isti lobby.
13. Osvezi ili ponovo pokreni jednu aplikaciju i potvrdi reconnect.

## Poznata ogranicenja

- Lobbyji i partije su u memoriji jednog backend procesa i nestaju posle restarta.
- Napustanje aktivne partije resetuje rundu za preostale igrace, prema postojecem backend ponasanju.
- Backend `connected` status jos nije vezan za WebSocket disconnect lifecycle.
- Nema produkcijske autentifikacije korisnika; reconnect token je identitet igraca.
- Produkcijski deployment, HTTPS/WSS i trajno cuvanje partija nisu deo ove faze.
