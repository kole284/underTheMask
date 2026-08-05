# Under The Mask

Multiplayer igra dedukcije reci. Svi igraci osim impostora dobijaju tajnu rec. Impostor dobija samo kategoriju ili asocijaciju, a zatim igraci redom daju tragove i glasaju.

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

Flyway pri startup-u kreira tabele i ubacuje pocetni katalog reci. Backend radi na `http://localhost:8080`. Ta adresa je API server i ne prikazuje frontend.

3. Pokreni frontend u drugom terminalu:

```bash
cp .env.example .env
npm install
npm run dev
```

4. Otvori aplikaciju na `http://localhost:5173`.

## Provera partije

Za lobby proveru dovoljna su dva taba. Za pokretanje igre potrebna su najmanje tri igraca, pa otvori tri taba na `http://localhost:5173`:

1. U prvom tabu napravi lobby i zapamti kod.
2. U drugom i trecem tabu izaberi pridruzivanje, unesi razlicita imena i isti kod.
3. U prvom tabu promeni podesavanja po zelji i klikni `Pokreni igru`.
4. U svakom tabu otkrij privatnu ulogu. Samo impostor vidi hint, ostali vide tajnu rec.
5. Daj trag u tabu igraca koji je na potezu.
6. Kada svi daju trag, svaki igrac bira zadati broj osumnjicenih i glasa.
7. Posle poslednjeg glasa svi tabovi odmah prikazuju isti rezultat.
8. Host moze da vrati sve igrace u lobby za novu rundu.

Sesije su sacuvane u `localStorage`, a aktivni identitet je izolovan po tabu. Refresh ne menja igraca niti hosta.

## Provera build-a

```bash
npm run lint
npm run build

cd backend
mvn test
```

## Native Android klijent

Zasebna Kotlin/Jetpack Compose aplikacija nalazi se u `android/` direktorijumu. LAN podesavanje, build, instalacija i test plan za tri telefona opisani su u [Android README-u](android/README.md).
