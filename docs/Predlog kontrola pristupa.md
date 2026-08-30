# Predlog — Sistem za kontrolu pristupa/ulaska u zgradu (Beograd na vodi)

> Radni naziv: **Building Access Control System**. Nastao analizom oba referentna projekta (eFacilityTicketing — Vukašinov, i RentaCar), analize NJT grupe, i istraživanja realnih visitor-management sistema (Gate Sentry, MyGate). Prilagođeno NAŠOJ temi — kontrola pristupa/ulaska (ne prijava kvarova, kao Vukašin).
>
> **Status: FINALIZOVANO (v2 — dodat lični bedž-kod za stanare/osoblje). Formalni dokumenti su napravljeni i ažurirani — `programski_zahtev_kontrola_pristupa.md` i `konceptualni-model-kontrola-pristupa.puml` (+ png/svg) — poslati korisnici kao fajlovi, spremni da se predaju Claude Code-u za izradu aplikacije. Ovaj dokument ostaje kao istorija odluka i obrazloženja, ne šalje se dalje.**

## Zašto ova tema, a ne tema kvarova (Vukašinov eFacilityTicketing)

Vukašin je uradio "kvarovi u zgradi" temu; njegovi dokumenti/kod/UML služe isključivo kao UZOR (arhitektura, format dokumentacije, greške koje treba izbeći), NE kao osnova koja se kopira ili proširuje. Kontrola pristupa deli infrastrukturne entitete (zgrada, stan, korisnik) sa njegovim projektom, ali poslovna logika je potpuno druga priča — dovoljno različito da bude prepoznatljiv sopstveni projekat.

## Ključna izmena u odnosu na v1 — lični bedž-kod za brz ulazak stanara/osoblja

**Problem koji je korisnica uočila:** u prvoj verziji, obezbeđenje je za ulazak stanara/osoblja moralo ručno da bira osobu iz liste — presporo za ljude koji prolaze svakodnevno, po više puta dnevno.

**Istraženo kako realni sistemi to rešavaju** (MyGate): čak i ti sistemi izbegavaju ručno pretraživanje kao glavni tok — osoblje/stanar ili sami skeniraju svoj QR, ili ga obezbeđenje skenira; ručna lista je samo rezervna opcija.

**Rešenje (potvrđeno sa korisnicom):**
- Svaki `User` (stanar) dobija sopstveni trajni `badgeCode` + QR pri registraciji, vidljiv u profilu.
- Osoblje zgrade (spremačica, majstor i sl.) NEMA korisnički nalog — admin ih unosi kao lagan zapis u novom entitetu `StaffBadge` (ime, opis uloge, zgrada, kod), bez lozinke/prijave.
- Obezbeđenje ima JEDNU jedinstvenu radnju za sve dolaske (gost, stanar, osoblje): unese/skenira kod. Sistem sam prepoznaje tip koda (propusnica / lični kod stanara / kod osoblja).
- Ulazak/izlazak stanara i osoblja se beleži AUTOMATSKI NAIZMENIČNO na osnovu poslednjeg zapisa za taj kod (prvo skeniranje = ulazak, sledeće = izlazak) — obezbeđenje ne bira dugme.
- Ručno pretraživanje liste ostaje kao rezervna opcija (npr. zaboravljen telefon/kartica).

## Entiteti (finalno v2)

- **Building** (Zgrada), **Apartment** (Stan)
- **User** (Korisnik: RESIDENT/SECURITY/ADMIN) — sada sa `badgeCode`
- **StaffBadge** (Osoblje) — NOVO: id, fullName, jobTitle, buildingId, badgeCode, active
- **GatePass** (Propusnica): kod, QR, gost, razlog, period važenja, limit ulazaka, tip (SINGLE/LIMITED/RECURRING), status
- **EntryLog** (EvidencijaUlaska): personType GUEST/RESIDENT/STAFF; gatePassId / userId / staffBadgeId (odgovarajuće nullable); entry/exit toggle logika za RESIDENT/STAFF
- **AccessDenial** (OdbijenPokušaj): automatski (nevalidan kod) + ručno (svesna odluka obezbeđenja, obavezan razlog) — oba u istoj tabeli
- **PassStatusHistory** (IstorijaPropusnice): audit trag statusa propusnice

## Korisničke uloge (finalno v2)

1. **Stanar (RESIDENT)** — nalog; ima lični badgeCode+QR; kreira/izmeni/produži/otkaže propusnice; vidi kod+QR+javni link odmah; pregleda status i istoriju propusnica i sopstvenih ulazaka.
2. **Obezbeđenje (SECURITY)** — nalog; vezan za jednu (trenutnu) zgradu; JEDNA radnja za sve dolaske (unos/skeniranje koda) — sistem prepoznaje tip i ulazak/izlazak automatski; ručna pretraga kao fallback; odbija ulazak (automatski/ručno, sa razlogom); vidi dnevnu evidenciju i odbijene pokušaje za svoju zgradu.
3. **Menadžer/Administrator (ADMIN)** — CRUD nad zgradama/stanovima/korisnicima/osobljem; pregled/pretraga/filtriranje/sortiranje/paginacija svega; statistika; izvoz (bonus).

Gosti NEMAJU nalog. Osoblje NEMA nalog (samo bedž). Samo stanar, obezbeđenje i admin imaju pravi korisnički nalog sa lozinkom.

## Propusnica i bezbednost — detaljna logika (nepromenjeno iz v1, i dalje važi)

- Kod + QR, oba; obezbeđenje bira ručni unos ili kameru.
- Deljenje gostu: kod+QR+javni link u appu (osnovno) + e-mail (bonus).
- Izmena/produženje dok je ACTIVE; nepromenljiva posle završnog statusa.
- Odbijeni pokušaji — obavezan deo modela, automatski + ručno, ista tabela `AccessDenial`.

## Dodatne funkcionalnosti i van obima — nepromenjeno iz v1 (v. prethodne verzije ovog dokumenta u istoriji projekta ako zatreba detalj)

"Ko je trenutno u zgradi", RECURRING propusnice, e-mail notifikacije — uključeno. Foto-capture, realtime push/SMS, offline sync, payroll — namerno van obima.

## Naučene lekcije primenjene od početka

BCrypt+JWT, prave PUT/DELETE rute, backend validacija, transakciona logika u service sloju, centralizovan GlobalExceptionHandler, server-side pretraga/sortiranje/paginacija, status-guard logika.

## Odluke — hronologija (29.08.2026)

1–9: v. prethodnu verziju (obim evidencije, obavezno/poželjno, kod+QR, obezbeđenje-zgrada veza, gosti bez naloga, deljenje propusnice, izmena propusnice, odbijanje sa razlogom, odbijeni pokušaji uvek beleženi).

10. **(v2, dodatno)** Stanari dobijaju lični trajni badgeCode+QR pri registraciji, generisan automatski.
11. Osoblje zgrade nema nalog — samo lagan zapis (StaffBadge) sa bedž-kodom koji kreira admin.
12. Ulazak/izlazak preko ličnog bedža se beleži automatski naizmenično (bez biranja dugmeta od strane obezbeđenja).

## Finalni fajlovi (napravljeni, poslati korisnici, ažurirani na v2)

- `programski_zahtev_kontrola_pristupa.md` — Programski zahtev u formatu predmeta.
- `konceptualni-model-kontrola-pristupa.puml` (+ .png/.svg) — konceptualni UML model.

### Izvori (istraživanje realnih sistema, 29.08.2026)

- [Visitor Management System with Mobile Passes — Gate Sentry](https://gatesentry.com/blog/visitor-management-system-with-mobile-passes/)
- [The Complete Guide to Visitor Management for Gated Communities — Gate Sentry](https://gatesentry.com/blog/visitor-management-gated-communities-guide/)
- [Mygate Visitor Management: Smart Gate Security for Societies](https://mygate.com/visitor-management/)