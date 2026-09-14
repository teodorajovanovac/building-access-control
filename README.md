# Building Access Control System

Sistem za kontrolu pristupa/ulaska u zgradu (projekat iz predmeta Napredne Java tehnologije). Aplikacija upravlja propusnicama za goste, ličnim bedž-kodovima stanara i osoblja, evidencijom ulaska/izlaska i odbijenim pokušajima pristupa.

## Uloge

- **RESIDENT (stanar)** — kreira, produžava i otkazuje propusnice za goste; ima sopstveni trajni bedž-kod (QR) za brz ulazak.
- **SECURITY (obezbeđenje)** — jedna radnja za sve dolaske: unos ili skeniranje koda (propusnica gosta, bedž stanara ili osoblja); sistem sam prepoznaje tip koda i da li je ulazak ili izlazak; ručna pretraga kao rezervna opcija; ručno odbijanje ulaska sa razlogom.
- **ADMIN (menadžer)** — CRUD nad zgradama, stanovima, korisnicima i osobljem; statistika i pregled evidencije.

Gosti i osoblje zgrade (npr. majstor, spremačica) nemaju korisnički nalog — samo bedž-kod.

## Tehnologije

**Backend** — Java 17, Spring Boot 3.3 (Web, Data JPA, Security, Validation, Mail), MySQL, JWT (jjwt), ZXing (QR kodovi), springdoc-openapi (Swagger UI). Build: Maven.

**Frontend** — React 19, Vite, MUI, React Router, Axios, Recharts, html5-qrcode (skeniranje QR koda kamerom).

## Struktura projekta

```
backend/    Spring Boot REST API
frontend/   React (Vite) klijentska aplikacija
docs/       Programski zahtev, konceptualni UML model, slučajevi korišćenja
```

## Pokretanje — priprema baze

1. Instaliraj i pokreni MySQL lokalno (podrazumevani port `3306`).
2. Nije potrebno ručno praviti bazu ni tabele — pri startu backend-a se baza `building_access_control` automatski kreira (`createDatabaseIfNotExist=true`), a Hibernate (`ddl-auto: update`) sam generiše/ažurira šemu prema entitetima.
3. Podesi korisničko ime/lozinku za MySQL preko env promenljivih ako se razlikuju od podrazumevanih (vidi ispod).

## Pokretanje — backend

Podešavanja se čitaju iz `backend/src/main/resources/application.yml`, sa mogućnošću override-a preko env promenljivih:

| Promenljiva | Podrazumevano | Opis |
|---|---|---|
| `DB_HOST` | `localhost` | Host MySQL baze |
| `DB_PORT` | `3306` | Port MySQL baze |
| `DB_NAME` | `building_access_control` | Naziv baze |
| `DB_USERNAME` | `root` | Korisničko ime za MySQL |
| `DB_PASSWORD` | *(praznо)* | Lozinka za MySQL |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host za slanje mejlova gostima |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | *(praznо)* | Nalog za slanje mejlova |
| `MAIL_PASSWORD` | *(praznо)* | App password za mejl nalog |
| `JWT_SECRET` | dev vrednost | **Mora** se promeniti pre produkcije |
| `JWT_EXPIRATION_MS` | `86400000` | Trajanje JWT tokena (ms) |
| `APP_PUBLIC_URL` | `http://localhost:5173` | Bazni URL frontend-a, koristi se u linku ka javnoj propusnici u mejlu gostu |

Umesto env promenljivih za lokalni razvoj, možeš staviti pravu konfiguraciju (npr. mejl kredencijale) u `backend/config/application.yml` — taj fajl je namerno u `.gitignore` i nikad se ne commituje.

Pokretanje:

```
cd backend
mvn spring-boot:run
```

Backend se pokreće na `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## Pokretanje — frontend

```
cd frontend
npm install
npm run dev
```

Frontend se pokreće na `http://localhost:5173` i po podrazumevanom podešavanju gađa backend na `http://localhost:8080` (može se promeniti preko `VITE_API_BASE_URL` u `.env` fajlu unutar `frontend/`).

## Dokumentacija

U `docs/` se nalaze programski zahtev, konceptualni UML model i slučajevi korišćenja po ulogama.
