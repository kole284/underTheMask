# Vodic za pokretanje igre Under The Mask

Ovaj vodic je pisan za osobu koja prvi put pokrece projekat na svom racunaru. Najjednostavniji nacin je da se pokrene web verzija igre u browseru. Android aplikacija je opciona i opisana je pri kraju.

## Sta se pokrece

Projekat ima vise delova:

- `backend/` - server igre, pokrece se preko Java/Maven-a i radi na portu `8080`.
- MySQL/MariaDB baza - cuva reci i kategorije za igru.
- web frontend - React/Vite aplikacija, pokrece se preko npm-a i otvara se u browseru na portu `5173`.
- `android/` - opciona Android aplikacija za telefon.

Za normalno lokalno igranje na racunaru treba pokrenuti bazu, backend i frontend.

## 1. Instaliraj potrebne programe

Na racunaru treba da postoje:

- Java JDK 17 ili noviji
- Maven 3.8 ili noviji
- MySQL 8 ili MariaDB
- Node.js 20 ili noviji
- npm
- Git, ako skidas projekat preko GitHub-a

Provera iz terminala:

```bash
java -version
mvn -version
mysql --version
node -v
npm -v
```

Ako neka komanda ne postoji, taj program nije instaliran ili nije dodat u `PATH`.

## 2. Preuzmi projekat

Ako koristis Git:

```bash
git clone <URL_REPOZITORIJUMA>
cd UnderTheMask
```

Ako si dobio ZIP fajl, raspakuj ga i otvori terminal u raspakovanom folderu projekta.

U nastavku se podrazumeva da si u glavnom folderu projekta, gde se nalaze `README.md`, `package.json`, `backend/` i `android/`.

## 3. Pokreni MySQL/MariaDB

MySQL server mora da radi pre pokretanja backenda.

Na Linuxu se cesto pokrece ovako:

```bash
sudo systemctl start mysql
```

ili za MariaDB:

```bash
sudo systemctl start mariadb
```

Na Windowsu ga obicno pokrece MySQL Installer/Services aplikacija. Na macOS-u zavisi od instalacije, npr. preko Homebrew-a:

```bash
brew services start mysql
```

## 4. Napravi bazu

Iz glavnog foldera projekta pokreni:

```bash
cd backend
mysql -u root -p < database/create_database.sql
```

Unesi MySQL root lozinku kada terminal zatrazi.

Ova komanda pravi:

- bazu `under_the_mask`
- korisnika `underthemask`
- lozinku `underthemask`

To su podrazumevana podesavanja koja backend vec ocekuje.

Ako dobijes gresku da korisnik vec postoji ili baza vec postoji, to najcesce nije problem ako su prethodno napravljeni. Probaj sledeci korak.

## 5. Pokreni backend

Ostani u `backend/` folderu i pokreni:

```bash
mvn spring-boot:run
```

Prvo pokretanje moze da potraje jer Maven skida dependency-je.

Backend radi kada u terminalu vidis da je aplikacija startovana i da slusa na portu `8080`.

Nemoj gasiti ovaj terminal dok igras. Backend mora stalno da radi.

Brza provera:

Otvori u browseru:

```text
http://localhost:8080/api/lobbies/ABC123
```

Ako dobijes JSON gresku tipa `404` ili `400`, to je u redu - znaci da server odgovara. Ako browser ne moze da se poveze, backend nije pokrenut ili port nije dostupan.

## 6. Pokreni frontend

Otvori drugi terminal u glavnom folderu projekta.

Ako si i dalje u `backend/`, vrati se jedan folder nazad:

```bash
cd ..
```

Prvi put napravi `.env` fajl:

```bash
cp .env.example .env
```

Instaliraj frontend dependency-je:

```bash
npm install
```

Pokreni web aplikaciju:

```bash
npm run dev
```

Frontend radi kada terminal prikaze adresu slicnu ovoj:

```text
http://localhost:5173
```

Otvori tu adresu u browseru.

Nemoj gasiti ni ovaj terminal dok igras.

## 7. Kako da testiras igru na jednom racunaru

Za punu partiju trebaju najmanje 3 igraca. Na jednom racunaru to mozes testirati sa 3 browser taba.

1. Otvori `http://localhost:5173` u prvom tabu.
2. Napravi lobby i zapamti kod.
3. Otvori jos dva taba na istoj adresi.
4. U druga dva taba izaberi pridruzivanje lobby-ju.
5. Unesi razlicita imena i isti lobby kod.
6. U prvom tabu, kao host, klikni pokretanje igre.
7. Svaki tab predstavlja jednog igraca.

Ako se nesto ne osvezava odmah, osvezi stranicu. Sesija igraca se cuva lokalno u browseru.

## 8. Ako prijatelj hoce da igra sa drugih uredjaja u istoj mrezi

Backend vec slusa na `0.0.0.0:8080`, sto znaci da ga mogu videti uredjaji na istoj Wi-Fi mrezi ako firewall dozvoljava port.

Na racunaru koji pokrece backend nadji lokalnu IP adresu.

Na Linuxu:

```bash
hostname -I
```

Trazi adresu oblika `192.168.x.x` ili `10.x.x.x`. Nemoj koristiti `127.0.0.1`.

Ako drugi racunar/telefon treba da koristi web frontend, najjednostavnije je da se frontend pokrene ovako:

```bash
npm run dev -- --host 0.0.0.0
```

Zatim drugi uredjaj otvara:

```text
http://LAN_IP_RACUNARA:5173
```

Primer:

```text
http://192.168.1.50:5173
```

Ako se frontend ucita, ali igra ne moze da se poveze sa backendom, podesi `.env` da koristi LAN IP umesto `localhost`:

```env
VITE_API_URL=http://192.168.1.50:8080/api
VITE_WS_URL=ws://192.168.1.50:8080/ws
```

Posle izmene `.env` fajla ugasi i ponovo pokreni frontend:

```bash
npm run dev -- --host 0.0.0.0
```

Firewall na racunaru mora dozvoliti portove:

- `8080` za backend
- `5173` za frontend

## 9. Android aplikacija, opciono

Ako zelis da pokrenes Android aplikaciju na telefonu:

1. Instaliraj Android Studio i Android SDK.
2. Pokreni MySQL i backend kao u koracima iznad.
3. Nadji LAN IP adresu racunara koji pokrece backend.
4. U folderu `android/` napravi lokalnu konfiguraciju:

```bash
cd android
cp local.properties.example local.properties
```

5. Otvori `android/local.properties` i podesi:

```properties
sdk.dir=/putanja/do/Android/Sdk
backend.host=192.168.1.50
```

Za Android emulator koristi:

```properties
backend.host=10.0.2.2
```

Za fizicki telefon koristi LAN IP racunara, npr. `192.168.1.50`.

Build:

```bash
./gradlew assembleDebug
```

APK se nalazi u:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

Instalacija preko ADB-a:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Telefon i racunar moraju biti na istoj Wi-Fi mrezi. Ako telefon ne vidi backend, proveri firewall i probaj da u browseru na telefonu otvoris:

```text
http://LAN_IP_RACUNARA:8080/api/lobbies/ABC123
```

JSON greska znaci da telefon vidi backend. Timeout znaci da mreza, IP adresa ili firewall nisu dobri.

## 10. Najcesci problemi

### Backend nece da se pokrene

Proveri:

- da MySQL/MariaDB radi
- da je baza napravljena
- da port `8080` nije zauzet
- da Java verzija nije starija od 17

Ako vidis gresku za login na bazu, backend ocekuje:

```text
database: under_the_mask
username: underthemask
password: underthemask
```

### Frontend se otvara, ali API ne radi

Proveri `.env`:

```env
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
```

Ako igras preko drugog uredjaja u mrezi, `localhost` zameni LAN IP adresom racunara koji pokrece backend.

### Port je zauzet

Ako je port `8080` zauzet, zaustavi drugi program koji ga koristi ili pokreni backend na drugom portu:

```bash
SERVER_PORT=8081 mvn spring-boot:run
```

Tada u `.env` mora da stoji:

```env
VITE_API_URL=http://localhost:8081/api
VITE_WS_URL=ws://localhost:8081/ws
```

### Maven ili npm dugo skidaju dependency-je

To je normalno pri prvom pokretanju. Ako pukne zbog mreze, proveri internet konekciju i pokreni istu komandu ponovo.

### Lobby nestane posle restarta backenda

To je trenutno ocekivano. Aktivni lobbyji i partije su u memoriji backend procesa. Ako ugasis backend, treba napraviti novi lobby.

## 11. Komande koje se najcesce koriste

Terminal 1 - backend:

```bash
cd backend
mvn spring-boot:run
```

Terminal 2 - frontend:

```bash
npm run dev
```

Provera build-a:

```bash
npm run lint
npm run build

cd backend
mvn test
```

