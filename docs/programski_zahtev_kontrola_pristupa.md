# Programski zahtev — Sistem za kontrolu pristupa i evidenciju ulaska u zgradu (Building Access Control System)

> Finalna verzija, u formatu koji se koristi za ovaj predmet (Napredne Java tehnologije). Ovaj dokument je osnova za izradu aplikacije — nije poslat mentoru (nema zakazanog sastanka), već služi da uskladimo šta se tačno gradi pre nego što izrada počne.

**Opis sistema**

Potrebno je razviti veb aplikaciju koja omogućava kontrolu pristupa i evidenciju ulaska lica u stambene zgrade, uključujući izdavanje elektronskih propusnica za posetioce. Aplikacija mora biti realizovana kao višeslojna (client-server) aplikacija, pri čemu su klijentski i serverski deo potpuno odvojeni.

**Tehnologije**

Backend: Java, Spring Boot, Spring Data JPA, Spring Security, REST Web Services, MySQL.
Frontend: React.

**Opis domena**

Kompleks stambenih zgrada sadrži više zgrada, a svaka zgrada sadrži više stanova. Svaki stan ima jednog ili više registrovanih stanara, a svaka zgrada ima radnike obezbeđenja koji rade na prijemnom holu, kao i osoblje zgrade (npr. održavanje) koje redovno ulazi bez sopstvenog korisničkog naloga. Svaki stanar pri registraciji, i svaki član osoblja pri unosu od strane menadžera, dobija sopstveni trajni kod za ulazak i odgovarajući QR kod, koji koristi za svaki svoj dolazak. Kada stanar očekuje posetu, kreira propusnicu unosom osnovnih podataka o poseti — imena gosta, razloga dolaska, perioda važenja i dozvoljenog broja ulazaka — nakon čega sistem generiše jedinstven kod propusnice i odgovarajući QR kod, koje stanar odmah dobija na uvid i sam prosleđuje gostu. Gost se tim kodom ili QR-om, po dolasku u zgradu, legitimiše obezbeđenju umesto usmenog objašnjenja razloga dolaska. Radnik obezbeđenja za svaki dolazak — gosta, stanara ili osoblja — unosi ili skenira odgovarajući kod (propusnice, ličnog koda stanara ili koda osoblja), bez ručnog pretraživanja liste korisnika. Sistem prepoznaje o kom tipu koda je reč: za propusnicu proverava njenu važnost i, ukoliko je validna, odobrava ulazak i uvećava broj iskorišćenih ulazaka (kada propusnica dostigne dozvoljen broj ulazaka ili istekne rok važenja, automatski postaje neaktivna); za lični kod stanara ili osoblja, sistem automatski naizmenično beleži ulazak i izlazak — prvo skeniranje u nizu evidentira dolazak, sledeće evidentira odlazak. Ukoliko uneti kod nije validan, ili obezbeđenje iz drugog razloga odluči da odbije ulazak, taj pokušaj se evidentira uz naznaku razloga odbijanja. Menadžer ima uvid u sve zgrade kojima upravlja, kompletnu evidenciju ulazaka, propusnica i odbijenih pokušaja, uz mogućnost pretrage, filtriranja i sortiranja, kao i pregled statistike posećenosti po zgradama i danima.

**Korisničke uloge**

Sistem podržava tri korisničke uloge.

1. Stanar (Resident)

Stanar može da:
- registruje korisnički nalog i prijavi se na sistem;
- dobije sopstveni trajni kod i QR kod za ulazak, generisan automatski pri registraciji;
- kreira propusnicu za gosta (ime, razlog, period važenja, broj dozvoljenih ulazaka) i dobije generisan kod i QR kod;
- izmeni ili produži propusnicu dok je aktivna, odnosno otkaže je;
- pregleda status i istoriju svojih propusnica, uključujući istekle i iskorišćene;
- pregleda evidenciju sopstvenih ulazaka i evidenciju ulazaka povezanih sa svojim stanom.

2. Obezbeđenje (Security)

Obezbeđenje može da:
- unosom ili skeniranjem koda pronađe i obradi bilo koji dolazak — propusnicu gosta, lični kod stanara ili kod osoblja zgrade — bez ručnog pretraživanja liste korisnika;
- odobri ulazak gosta na osnovu validne propusnice;
- ulazak/izlazak stanara i osoblja sistem beleži automatski naizmenično na osnovu njihovog ličnog koda (skeniranje = dolazak, sledeće skeniranje = odlazak);
- po potrebi ručno pronađe stanara ili osoblje iz liste (rezervni način, npr. ako lice nema svoj kod pri ruci) i evidentira ulazak;
- odbije ulazak — automatski, kada kod nije validan, ili ručno, uz uneti razlog — pri čemu se odbijeni pokušaji evidentiraju;
- pregleda dnevnu evidenciju ulazaka i odbijenih pokušaja za zgradu u kojoj radi.

3. Menadžer/Administrator (Admin)

Menadžer može da:
- upravlja zgradama, stanovima i korisnicima (CRUD), uključujući dodelu zgrade radniku obezbeđenja;
- upravlja osobljem zgrade (dodavanje/uklanjanje člana osoblja sa generisanjem ličnog koda za ulazak, bez potrebe za korisničkim nalogom/lozinkom);
- pregleda, pretražuje, filtrira i sortira sve propusnice, evidenciju ulazaka i odbijenih pokušaja, uz paginaciju;
- pregleda statistiku posećenosti po zgradama i vremenskim periodima.

**Aplikacija mora da:**
- koristi troslojnu arhitekturu, sa potpuno odvojenim klijentskim i serverskim delom;
- koristi REST servis za komunikaciju između klijenta i servera;
- bude realizovana korišćenjem Spring Data JPA / Hibernate tehnologije;
- koristi odgovarajuće HTTP metode (GET, POST, PUT, DELETE);
- podržava validaciju korisničkih unosa;
- obrađuje greške na odgovarajući način;
- koristi DTO objekte za komunikaciju između klijenta i servera;
- podržava CRUD operacije nad ključnim entitetima (zgrade, stanovi, korisnici, propusnice, osoblje zgrade).

**Poželjno je da aplikacija ima implementirane i neke od sledećih funkcionalnosti:**
- autentikacija i autorizacija korišćenjem JWT tokena, sa razdvojenim ulogama (RESIDENT, SECURITY, ADMIN);
- paginacija, pretraga i sortiranje liste propusnica i evidencije ulaska;
- generisanje i prikaz QR koda za propusnicu, kao i za lični kod stanara i osoblja, uz mogućnost skeniranja kamerom uređaja;
- automatsko naizmenično evidentiranje ulaska/izlaska stanara i osoblja na osnovu skeniranja njihovog ličnog koda;
- javna stranica propusnice (bez prijave) sa neosetljivim podacima, radi lakšeg deljenja gostu;
- evidencija odbijenih pokušaja ulaska, sa razlogom odbijanja (automatskim ili ručno unetim);
- čuvanje istorije izmena statusa propusnice;
- pregled lica trenutno prisutnih u zgradi;
- statistički prikaz (dashboard sa brojem ulazaka, propusnica i odbijenih pokušaja po zgradi/danu);
- e-mail notifikacija stanaru pri korišćenju propusnice, i opciono slanje koda propusnice gostu na e-mail;
- responzivan korisnički interfejs.

---

## Napomena — obrazloženje ključnih odluka (nije deo teksta za mentora, radni podsetnik)

- **Odbijeni pokušaji ulaska (AccessDenial/OdbijenPokusaj)** — evidentiraju se OBA slučaja u istoj tabeli: (1) automatski, kada obezbeđenje unese/skenira kod koji nije validan (ne postoji, istekao, iskorišćen, otkazan) — sistem sâm upisuje zapis sa odgovarajućim tipom razloga; (2) ručno, kada obezbeđenje ima ispred sebe osobu (sa validnom propusnicom ili bez nje) i iz sopstvene procene odluči da je ne pusti — tada MORA da unese tekstualni razlog. Razlikuju se poljem `reasonType` (NEVALIDAN_KOD / ISTEKLA / ISKORISCENA / OTKAZANA / RUCNO_ODBIJANJE) i time da li je `reasonNote` (slobodan tekst) obavezan.
- **Struktura paketa (backend):** `controller / service / repository / model (entity) / dto / mapper / config` — isto kao kod eFacility/RentaCar (dokazano prihvaćena struktura za predmet).
- **Lični kod za ulazak (stanar/osoblje) — finalna odluka:** da bi ulazak stanara i osoblja bio podjednako brz kao skeniranje propusnice gosta (a ne sporo ručno pretraživanje liste od strane obezbeđenja), svaki `User` sa ulogom RESIDENT dobija sopstveni trajni `badgeCode` (+ QR) pri registraciji, a svaki član osoblja zgrade dobija zapis u novom, lakom entitetu `StaffBadge` (ime, uloga/opis, zgrada, kod) koji admin kreira — bez potrebe za nalogom/lozinkom. Obezbeđenje tako ima JEDNU jedinstvenu radnju za sve dolaske: unese/skenira kod, a sistem sam prepoznaje da li je reč o propusnici, ličnom kodu stanara ili kodu osoblja, i da li je u pitanju ulazak ili izlazak (automatsko naizmenično prepoznavanje na osnovu poslednjeg zapisa). Ručno pretraživanje liste ostaje samo kao rezervna opcija.
- **Ključni entiteti:** Building, Apartment, User (sa badgeCode), StaffBadge, GatePass, EntryLog, AccessDenial, PassStatusHistory — detaljno opisani u pratećem konceptualnom modelu (`konceptualni-model-kontrola-pristupa.puml`).
- Puna istorija odluka i poređenje sa referentnim projektima (eFacility, RentaCar) čuva se u internom radnom dokumentu `predlog_kontrola_pristupa.md`.
