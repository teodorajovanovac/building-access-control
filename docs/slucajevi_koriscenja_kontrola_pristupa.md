# Slučajevi korišćenja — Sistem za kontrolu pristupa u zgradu

> Format prati dokumentaciju referentnih projekata (RentaCar — tekstualni opis SK sa oznakama koraka; eFacilityTicketing — dijagrami slučajeva korišćenja po akteru). Dijagrami: `slucajevi-koriscenja-stanar.puml` i `slucajevi-koriscenja-obezbedjenje-menadzer.puml` (+ png/svg). Oznake koraka (АПУСО, АПСО, СО, ИА, АНСО) korišćene su na isti način kao u referentnoj dokumentaciji — obeležavaju tip koraka u scenariju (unos podataka od strane aktera, poziv sistemske funkcije od strane aktera, interna obrada sistema, poruka/rezultat koji sistem prikazuje akteru, odnosno akterova provera sopstvenog unosa pre potvrde). Mesta označena "Slika N" su rezervisana za snimke ekrana koji se dodaju kada aplikacija bude urađena.

## Tabela slučajeva korišćenja

| SK | Naziv | Akter(i) |
| --- | --- | --- |
| SK1 | Prijava korisnika | Stanar, Obezbeđenje, Administrator |
| SK2 | Registracija stanara | Stanar |
| SK3 | Kreiranje propusnice za gosta | Stanar |
| SK4 | Izmena/produženje propusnice | Stanar |
| SK5 | Otkazivanje propusnice | Stanar |
| SK6 | Pregled sopstvenih propusnica i istorije | Stanar |
| SK7 | Pregled evidencije ulazaka za svoj stan | Stanar |
| SK8 | Obrada dolaska gosta (kod/QR propusnice) | Obezbeđenje |
| SK9 | Obrada dolaska stanara/osoblja (lični bedž) | Obezbeđenje |
| SK10 | Ručna pretraga stanara/osoblja (rezervna opcija) | Obezbeđenje |
| SK11 | Odbijanje ulaska sa razlogom | Obezbeđenje |
| SK12 | Pregled dnevne evidencije i odbijenih pokušaja | Obezbeđenje |
| SK13 | Upravljanje zgradama (CRUD) | Administrator |
| SK14 | Upravljanje stanovima (CRUD) | Administrator |
| SK15 | Upravljanje korisnicima (CRUD) | Administrator |
| SK16 | Upravljanje osobljem zgrade (CRUD + bedž kod) | Administrator |
| SK17 | Pretraga, filtriranje, sortiranje i paginacija | Administrator, Obezbeđenje |
| SK18 | Pregled statistike posećenosti (dashboard) | Administrator, Obezbeđenje |

*Slika 1: Dijagram slučajeva korišćenja — Stanar*
*Slika 2: Dijagram slučajeva korišćenja — Obezbeđenje i Menadžer*

---

## SK1 – Prijava korisnika

**Naziv SK**
Prijava korisnika

**Akteri SK**
Stanar, Obezbeđenje, Administrator

**Učesnici SK**
Korisnik, korisnički interfejs (React aplikacija), sistem (Spring Boot server)

**Preduslovi:** Klijentski i serverski program su pokrenuti. Korisnik ima registrovan nalog.

**Osnovni scenario SK:**
- Korisnik unosi email i lozinku. (АПУСО)
- Korisnik poziva sistem za prijavu. (АПСО)
- Sistem proverava podatke i generiše JWT token sa ulogom korisnika. (СО)
- Sistem prijavljuje korisnika i preusmerava ga na početnu stranicu prema njegovoj ulozi. (ИА)

**Alternativna scenarija:**
4.1 Ukoliko podaci nisu validni, sistem prikazuje poruku „Pogrešan email ili lozinka!" (ИА)

---

## SK2 – Registracija stanara

**Naziv SK**
Registracija stanara

**Akteri SK**
Stanar

**Učesnici SK**
Korisnik, korisnički interfejs, sistem

**Preduslovi:** Klijent i server su pokrenuti. Administrator je prethodno uneo zgradu i stan kojem se korisnik prijavljuje.

**Osnovni scenario SK:**
- Korisnik poziva sistem da otvori formu za registraciju. (АПСО)
- Sistem prikazuje formu za unos podataka. (ИА)
- Korisnik unosi podatke (ime, prezime, email, lozinka, stan). (АПУСО)
- Korisnik proverava unos. (АНСО)
- Korisnik poziva sistem da sačuva podatke. (АПСО)
- Sistem čuva novog korisnika sa ulogom RESIDENT i automatski mu generiše lični bedž kod i QR kod za ulazak. (СО)
- Sistem prikazuje poruku „Nalog je uspešno kreiran!" (ИА)

**Alternativna scenarija:**
7.1 Ukoliko nalog sa unetim email-om već postoji, sistem prikazuje poruku „Nalog sa ovim email-om već postoji!" (ИА)

---

## SK3 – Kreiranje propusnice za gosta

**Naziv SK**
Kreiranje propusnice za gosta

**Akteri SK**
Stanar

**Učesnici SK**
Stanar, korisnički interfejs, sistem

**Preduslovi:** Stanar je prijavljen na sistem.

**Osnovni scenario SK:**
- Stanar poziva sistem da otvori formu za kreiranje propusnice. (АПСО)
- Sistem prikazuje formu (ime gosta, telefon, razlog dolaska, period važenja, broj dozvoljenih ulazaka). (ИА)
- Stanar unosi podatke o poseti. (АПУСО)
- Stanar proverava unos. (АНСО)
- Stanar poziva sistem da sačuva propusnicu. (АПСО)
- Sistem generiše jedinstven kod i odgovarajući QR kod, i čuva propusnicu sa statusom ACTIVE. (СО)
- Sistem prikazuje kod, QR kod i javni link za deljenje propusnice gostu. (ИА)

**Alternativna scenarija:**
7.1 Ukoliko su period važenja ili broj ulazaka neispravni (npr. datum u prošlosti), sistem prikazuje poruku o grešci validacije. (ИА)

---

## SK4 – Izmena/produženje propusnice

**Naziv SK**
Izmena/produženje propusnice

**Akteri SK**
Stanar

**Učesnici SK**
Stanar, korisnički interfejs, sistem

**Preduslovi:** Propusnica postoji i u statusu je ACTIVE.

**Osnovni scenario SK:**
- Stanar pretražuje svoje propusnice. (АПУСО)
- Sistem prikazuje listu. (ИА)
- Stanar bira propusnicu i menja podatke (period važenja, broj ulazaka, razlog). (АПУСО)
- Stanar poziva sistem da sačuva izmene. (АПСО)
- Sistem proverava da je propusnica i dalje u statusu ACTIVE i pamti izmene. (СО)
- Sistem prikazuje poruku „Propusnica je uspešno izmenjena!" (ИА)

**Alternativna scenarija:**
5.1 Ukoliko je propusnica u međuvremenu iskorišćena, istekla ili otkazana, sistem odbija izmenu i prikazuje poruku „Propusnica se više ne može menjati!" (ИА)

---

## SK5 – Otkazivanje propusnice

**Naziv SK**
Otkazivanje propusnice

**Akteri SK**
Stanar

**Učesnici SK**
Stanar, korisnički interfejs, sistem

**Preduslovi:** Propusnica postoji i u statusu je ACTIVE.

**Osnovni scenario SK:**
- Stanar bira propusnicu iz liste. (АПУСО)
- Stanar poziva sistem za otkazivanje. (АПСО)
- Sistem menja status propusnice u CANCELED i beleži promenu u istoriji statusa. (СО)
- Sistem prikazuje poruku „Propusnica je otkazana!" (ИА)

---

## SK6 – Pregled sopstvenih propusnica i istorije

**Naziv SK**
Pregled sopstvenih propusnica i istorije

**Akteri SK**
Stanar

**Učesnici SK**
Stanar, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Stanar poziva sistem da prikaže listu propusnica. (АПСО)
- Sistem dohvata propusnice stanara (aktivne, iskorišćene, istekle, otkazane) sa statusom i brojem iskorišćenih ulazaka. (СО)
- Sistem prikazuje listu sa statusima. (ИА)
- Stanar bira propusnicu da vidi detalje i istoriju promena statusa. (АПУСО)
- Sistem prikazuje istoriju izmena statusa propusnice. (ИА)

---

## SK7 – Pregled evidencije ulazaka za svoj stan

**Naziv SK**
Pregled evidencije ulazaka za svoj stan

**Akteri SK**
Stanar

**Učesnici SK**
Stanar, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Stanar poziva sistem da prikaže evidenciju ulazaka. (АПСО)
- Sistem dohvata zapise evidencije povezane sa gostima stanara (preko propusnica) i njegovim ličnim ulascima/izlascima. (СО)
- Sistem prikazuje listu ulazaka sa vremenom ulaska i izlaska. (ИА)

---

## SK8 – Obrada dolaska gosta (kod/QR propusnice)

**Naziv SK**
Obrada dolaska gosta

**Akteri SK**
Obezbeđenje

**Učesnici SK**
Obezbeđenje, korisnički interfejs, sistem

**Preduslovi:** Obezbeđenje je prijavljeno i dodeljeno konkretnoj zgradi.

**Osnovni scenario SK:**
- Obezbeđenje unosi kod propusnice ili skenira QR kod kamerom uređaja. (АПУСО)
- Obezbeđenje poziva sistem da proveri propusnicu. (АПСО)
- Sistem proverava da li propusnica postoji, da li je u statusu ACTIVE, da nije istekla i da nije dostigla maksimalan broj ulazaka. (СО)
- Sistem odobrava ulazak, upisuje zapis u evidenciju ulazaka i uvećava broj iskorišćenih ulazaka; ako je time dostignut limit ili je istekao rok, menja status propusnice. (СО)
- Sistem prikazuje podatke o gostu i poruku „Ulazak odobren!" (ИА)

**Alternativna scenarija:**
5.1 Ukoliko propusnica nije validna (ne postoji, istekla je, iskorišćena je ili je otkazana), izvršava se SK11 – Odbijanje ulaska sa razlogom. (ИА)

---

## SK9 – Obrada dolaska stanara/osoblja (lični bedž)

**Naziv SK**
Obrada dolaska stanara/osoblja

**Akteri SK**
Obezbeđenje

**Učesnici SK**
Obezbeđenje, korisnički interfejs, sistem

**Preduslovi:** Obezbeđenje je prijavljeno. Stanar ili član osoblja poseduje svoj lični bedž kod.

**Osnovni scenario SK:**
- Obezbeđenje unosi ili skenira lični bedž kod stanara/osoblja. (АПУСО)
- Obezbeđenje poziva sistem da obradi dolazak. (АПСО)
- Sistem prepoznaje kom stanaru/članu osoblja kod pripada i proverava poslednji zapis evidencije za taj kod. (СО)
- Ukoliko poslednji zapis nema evidentiran izlazak, sistem upisuje vreme izlaska u taj zapis; u suprotnom kreira novi zapis sa vremenom ulaska. (СО)
- Sistem prikazuje ime osobe i poruku „Ulazak evidentiran!" odnosno „Izlazak evidentiran!" (ИА)

**Alternativna scenarija:**
3.1 Ukoliko kod nije prepoznat ili je bedž deaktiviran, izvršava se SK11 – Odbijanje ulaska sa razlogom. (ИА)

---

## SK10 – Ručna pretraga stanara/osoblja (rezervna opcija)

**Naziv SK**
Ručna pretraga stanara/osoblja

**Akteri SK**
Obezbeđenje

**Učesnici SK**
Obezbeđenje, korisnički interfejs, sistem

**Preduslovi:** Osoba nema svoj kod/QR pri ruci (npr. zaboravljen telefon ili kartica).

**Osnovni scenario SK:**
- Obezbeđenje pretražuje stanare ili osoblje svoje zgrade po imenu ili broju stana. (АПУСО)
- Sistem prikazuje listu rezultata. (ИА)
- Obezbeđenje bira osobu i poziva sistem da evidentira ulazak. (АПСО)
- Sistem primenjuje istu logiku automatski naizmeničnog ulaska/izlaska kao u SK9 i upisuje zapis uz napomenu da je unos ručni. (СО)
- Sistem prikazuje poruku o uspešnoj evidenciji. (ИА)

---

## SK11 – Odbijanje ulaska sa razlogom

**Naziv SK**
Odbijanje ulaska sa razlogom

**Akteri SK**
Obezbeđenje (i sistem, u automatskom slučaju)

**Učesnici SK**
Obezbeđenje, korisnički interfejs, sistem

**Preduslovi:** Uneti/skenirani kod nije validan, ili obezbeđenje odluči da odbije ulazak i pored validnog koda.

**Osnovni scenario SK — automatski slučaj:**
- Sistem ne pronalazi validnu propusnicu ili lični bedž za uneti kod. (СО)
- Sistem samostalno upisuje zapis u evidenciju odbijenih pokušaja, sa odgovarajućim tipom razloga (nevalidan kod, istekla, iskorišćena, otkazana). (СО)
- Sistem prikazuje obezbeđenju poruku o razlogu odbijanja. (ИА)

**Osnovni scenario SK — ručni slučaj:**
- Obezbeđenje poziva sistem da odbije ulazak osobi ispred sebe, iako ima validnu propusnicu/bedž. (АПСО)
- Sistem zahteva unos razloga odbijanja. (ИА)
- Obezbeđenje unosi razlog. (АПУСО)
- Obezbeđenje potvrđuje odbijanje. (АПСО)
- Sistem upisuje zapis u evidenciju odbijenih pokušaja sa tipom RUCNO_ODBIJANJE i unetim razlogom. (СО)

**Alternativna scenarija:**
3.1 (ručni slučaj) Ukoliko obezbeđenje ne unese razlog, sistem ne dozvoljava potvrdu i prikazuje poruku da je razlog obavezan. (ИА)

---

## SK12 – Pregled dnevne evidencije i odbijenih pokušaja

**Naziv SK**
Pregled dnevne evidencije i odbijenih pokušaja

**Akteri SK**
Obezbeđenje

**Učesnici SK**
Obezbeđenje, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Obezbeđenje poziva sistem da prikaže dnevnu evidenciju svoje zgrade. (АПСО)
- Sistem dohvata sve ulaske/izlaske i odbijene pokušaje za tekući dan i zgradu obezbeđenja. (СО)
- Sistem prikazuje listu, uključujući lica trenutno prisutna u zgradi (bez evidentiranog izlaska). (ИА)

---

## SK13 – Upravljanje zgradama (CRUD)

**Naziv SK**
Upravljanje zgradama

**Akteri SK**
Administrator

**Učesnici SK**
Administrator, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Administrator poziva sistem da prikaže listu zgrada. (АПСО)
- Sistem prikazuje listu. (ИА)
- Administrator kreira novu zgradu, ili bira postojeću za izmenu/brisanje, i unosi/menja podatke (naziv, adresa). (АПУСО)
- Administrator poziva sistem da sačuva izmene. (АПСО)
- Sistem čuva podatke o zgradi. (СО)
- Sistem prikazuje poruku o uspešno izvršenoj operaciji. (ИА)

**Alternativna scenarija:**
6.1 Ukoliko se briše zgrada koja ima stanove sa aktivnom evidencijom ili propusnicama, sistem odbija brisanje i prikazuje odgovarajuću poruku. (ИА)

---

## SK14 – Upravljanje stanovima (CRUD)

**Naziv SK**
Upravljanje stanovima

**Akteri SK**
Administrator

**Učesnici SK**
Administrator, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Administrator bira zgradu i poziva sistem da prikaže njene stanove. (АПСО)
- Sistem prikazuje listu stanova. (ИА)
- Administrator kreira novi stan, ili menja/briše postojeći (broj, sprat). (АПУСО)
- Administrator poziva sistem da sačuva izmene. (АПСО)
- Sistem čuva podatke. (СО)
- Sistem prikazuje poruku o uspešno izvršenoj operaciji. (ИА)

**Alternativna scenarija:**
6.1 Ukoliko se briše stan koji ima registrovane stanare ili propusnice, sistem odbija brisanje. (ИА)

---

## SK15 – Upravljanje korisnicima (CRUD)

**Naziv SK**
Upravljanje korisnicima

**Akteri SK**
Administrator

**Učesnici SK**
Administrator, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Administrator poziva sistem da prikaže listu korisnika. (АПСО)
- Sistem prikazuje listu sa ulogama. (ИА)
- Administrator kreira novog korisnika (stanara ili obezbeđenje) ili menja postojećeg, uključujući dodelu/promenu zgrade radniku obezbeđenja. (АПУСО)
- Administrator poziva sistem da sačuva izmene. (АПСО)
- Sistem čuva podatke; za novog stanara automatski generiše lični bedž kod. (СО)
- Sistem prikazuje poruku o uspešno izvršenoj operaciji. (ИА)

---

## SK16 – Upravljanje osobljem zgrade (CRUD + bedž kod)

**Naziv SK**
Upravljanje osobljem zgrade

**Akteri SK**
Administrator

**Učesnici SK**
Administrator, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Administrator poziva sistem da prikaže listu osoblja zgrade. (АПСО)
- Sistem prikazuje listu. (ИА)
- Administrator unosi podatke o novom članu osoblja (ime, opis uloge, zgrada) ili menja/deaktivira postojećeg. (АПУСО)
- Administrator poziva sistem da sačuva podatke. (АПСО)
- Sistem generiše lični bedž kod za novog člana osoblja i čuva podatke. (СО)
- Sistem prikazuje bedž kod i QR kod, spremne za štampu, i poruku o uspešno izvršenoj operaciji. (ИА)

---

## SK17 – Pretraga, filtriranje, sortiranje i paginacija

**Naziv SK**
Pretraga, filtriranje, sortiranje i paginacija

**Akteri SK**
Administrator, Obezbeđenje (ograničeno na svoju zgradu)

**Učesnici SK**
Korisnik, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Korisnik unosi parametre pretrage (status, period, zgrada, tekst pretrage) i bira kriterijum sortiranja. (АПУСО)
- Korisnik poziva sistem da pretraži. (АПСО)
- Sistem izvršava upit nad bazom uz filtriranje, sortiranje i paginaciju. (СО)
- Sistem prikazuje straničenu listu rezultata. (ИА)

---

## SK18 – Pregled statistike posećenosti (dashboard)

**Naziv SK**
Pregled statistike posećenosti

**Akteri SK**
Administrator, Obezbeđenje

**Učesnici SK**
Korisnik, korisnički interfejs, sistem

**Osnovni scenario SK:**
- Korisnik otvara dashboard. (АПСО)
- Sistem izračunava broj propusnica, ulazaka i odbijenih pokušaja po zgradi i vremenskom periodu. (СО)
- Sistem prikazuje statistiku u vidu brojeva/grafikona. (ИА)
