# Under The Mask

Multiplayer igra dedukcije reči. Svi igrači osim impostora dobijaju tajnu reč. Impostor dobija samo kategoriju ili asocijaciju, a zatim igrači redom daju tragove i glasaju.

## Potrebno

- Java 17+
- Maven 3.8+
- MySQL 8+ ili kompatibilan MariaDB
- Node.js 20+
- npm

## Prvo pokretanje

1. Napravi bazu i lokalnog korisnika:

```bash
cd backend
mysql -u root -p < database/create_database.sql
```

2. Pokreni backend u prvom terminalu:

```bash
cd backend
mvn spring-boot:run
```

Flyway pri startup-u kreira tabele i ubacuje početni katalog reči. Backend radi na `http://localhost:8080`. Ta adresa je API server i ne prikazuje frontend.

3. Pokreni frontend u drugom terminalu:

```bash
cp .env.example .env
npm install
npm run dev
```

4. Otvori aplikaciju na `http://localhost:5173`.

## Provera partije

Za lobby proveru dovoljna su dva taba. Za pokretanje igre potrebna su najmanje tri igrača, pa otvori tri taba na `http://localhost:5173`:

1. U prvom tabu napravi lobby i zapamti kod.
2. U drugom i trećem tabu izaberi pridruživanje, unesi različita imena i isti kod.
3. U prvom tabu promeni podešavanja po želji i klikni `Pokreni igru`.
4. U svakom tabu otkrij privatnu ulogu. Samo impostor vidi hint, ostali vide tajnu reč.
5. Daj trag u tabu igrača koji je na potezu.
6. Kada svi daju trag, svaki igrač bira zadati broj osumnjičenih i glasa.
7. Posle poslednjeg glasa svi tabovi odmah prikazuju isti rezultat.
8. Host može da vrati sve igrače u lobby za novu rundu.

Sesije su sačuvane u `localStorage`, a aktivni identitet je izolovan po tabu. Refresh ne menja igrača niti hosta.

## Provera build-a

```bash
npm run lint
npm run build

cd backend
mvn test
```

## Native Android klijent

Zasebna Kotlin/Jetpack Compose aplikacija nalazi se u `android/` direktorijumu. LAN podešavanje, build, instalacija i test plan za tri telefona opisani su u [Android README-u](android/README.md).

## Production deployment na Hetzner server

Production setup koristi Docker Compose:

- `web` - Caddy server koji servira React build, automatski izdaje Let's Encrypt TLS sertifikat i proxy-je `/api` i `/ws`.
- `backend` - single Spring Boot instanca na internoj Docker mreži.
- `db` - MySQL 8.4 sa named volume-om za podatke.

Javno se izlažu samo portovi `80/tcp` i `443/tcp`. MySQL `3306` i Spring Boot `8080` ne treba otvarati prema internetu.

### Prvi deployment

1. DNS: napravi `A` zapis za domen/subdomen, npr. `game.example.com`, ka javnoj IP adresi Hetzner servera.
2. Server: instaliraj Docker Engine i Docker Compose plugin.
3. Firewall: dozvoli samo `22/tcp`, `80/tcp` i `443/tcp`. Ne otvaraj `3306` ni `8080`.
4. Kloniraj repo na server i uđi u projekat.
5. Napravi production env:

```bash
cp .env.production.example .env
```

6. Popuni `.env`:

```env
GAME_DOMAIN=game.example.com
LETSENCRYPT_EMAIL=admin@example.com
DB_NAME=under_the_mask
DB_USER=underthemask
DB_PASSWORD=<jaka-lozinka>
DB_ROOT_PASSWORD=<jaka-root-lozinka>
SPRING_PROFILES_ACTIVE=prod
```

7. Pokreni stack:

```bash
./scripts/deploy.sh
```

Flyway migracije se izvršavaju automatski pri startup-u backend containera. Backend container ne postaje healthy dok ne odgovori na root health endpoint, a pre toga čeka da MySQL prođe health check.

8. Provera:

```bash
curl -I https://game.example.com/
curl https://game.example.com/api/lobbies/ABCDEF
```

Druga komanda treba da vrati kontrolisanu JSON grešku za nepostojeći lobby, što potvrđuje da `/api` proxy radi. WebSocket koristi `wss://game.example.com/ws` kroz isti Caddy proxy.

### Kasniji deployment

```bash
git pull
docker compose up -d --build
```

Ili koristi:

```bash
./scripts/deploy.sh
```

### React production URL-ovi

Production React build podrazumevano koristi isti origin:

- REST: `/api`
- WebSocket: `wss://GAME_DOMAIN/ws` kada je stranica otvorena preko HTTPS-a

`VITE_API_URL` i `VITE_WS_URL` ostaju podržani za lokalni razvoj ili LAN testiranje, ali nisu potrebni za production compose deployment.

### Android production URL-ovi

Debug build koristi `backend.host` iz `android/local.properties`, npr:

```properties
backend.host=192.168.1.50
```

Release build koristi:

```properties
backend.release.apiUrl=https://game.example.com/api/
backend.release.wsUrl=wss://game.example.com/ws
```

Release APK ne treba lokalnu IP adresu. Cleartext HTTP/WS i `ACCESS_LOCAL_NETWORK` dozvola ostaju samo za debug build.

### Ograničenje lobby persistence-a

Aktivni lobbyji, igrači, reconnect tokeni i partije su trenutno u memoriji jednog Spring Boot procesa. Production compose zato pokreće samo jednu backend repliku. Restart ili redeploy backenda prekida aktivne partije i briše aktivne lobbyje. Trajni katalog reči ostaje u MySQL bazi.
