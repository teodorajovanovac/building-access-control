# Teorijski pojmovi za odbranu — Building Access Control System

> Svrha ovog fajla: za svaki pojam koji profesor obično pita, prvo kratko OBJAŠNJENJE teorije
> (bez koda), pa tačno MESTO u našem projektu gde se to vidi (fajl + linija), da ga možeš
> otvoriti i pokazati uživo. Uči teoriju prvo — kod je samo dokaz da je primenjena.

---

## 1. Inversion of Control (IoC) — "kontrola je obrnuta"

**Teorija:** Normalno, tvoj kod kontroliše kada se neki objekat pravi — pozoveš `new NestoService()`
kad ti zatreba. Kod IoC-a, tu kontrolu prepuštaš **frameworku** (kontejneru): on pravi objekte i
daje ti ih kad zatreba, umesto da ih ti ručno praviš i povezuješ. "Inverzija" je u tome što
framework poziva tvoj kod (a ne obrnuto).

**Gde kod nas:** Ceo Spring Boot. Kad se `BuildingAccessControlApplication` pokrene, Spring pravi
`ApplicationContext` (IoC kontejner). On skenira sve klase označene sa `@Service`, `@Controller`,
`@Repository`, `@Component`, pravi njihove instance ("bean-ove") i sam ih međusobno poveže. Nigde
u našem kodu ne pišemo `new GatePassServiceImpl(...)` — Spring to radi.

---

## 2. Dependency Injection (DI) — tehnika kojom se IoC ostvaruje

**Teorija:** Umesto da klasa SAMA pravi svoje zavisnosti unutar sebe (`new XRepository()`), one joj
se "ubrizgaju" spolja — najčešće kroz konstruktor. Klasa samo kaže "meni treba nešto ovog tipa", a
neko drugi (kontejner) joj to dostavi. **Bitno: DI nema nikakve veze sa Lombok-om.** DI je Spring-ov
mehanizam; Lombok je samo alat koji ume da ti UŠTEDI kucanje jednog običnog konstruktora. Mi smo u
međuvremenu uklonili Lombok-ovu anotaciju `@RequiredArgsConstructor` i pišemo konstruktor ručno —
DI i dalje radi identično, jer DI ne zavisi od toga ko je konstruktor napisao (ti ili Lombok), nego
samo od toga DA konstruktor postoji i KO ga poziva.

**Gde kod nas:** Svaka servisna/kontroler klasa. Primer —
`backend/src/main/java/com/buildingaccess/service/impl/GatePassServiceImpl.java`:
```java
@Service
public class GatePassServiceImpl implements GatePassService {
    private final GatePassRepository gatePassRepository;
    private final PassStatusHistoryRepository historyRepository;
    private final MailService mailService;

    public GatePassServiceImpl(GatePassRepository gatePassRepository,
                                PassStatusHistoryRepository historyRepository,
                                MailService mailService) {
        this.gatePassRepository = gatePassRepository;
        this.historyRepository = historyRepository;
        this.mailService = mailService;
    }
    ...
```
**Gde je tačno DI ovde:** Ti nigde u projektu ne pišeš `new GatePassServiceImpl(nesto, nesto2, nesto3)`.
Spring, kad pravi ovaj bean, VIDI da konstruktor traži tri parametra (`GatePassRepository`,
`PassStatusHistoryRepository`, `MailService`), pronađe u svom IoC kontejneru po jedan bean tog tipa
(koje je i sam ranije napravio, jer su i oni označeni sa `@Repository`/`@Service`), i SAM pozove taj
konstruktor sa njima kao argumentima. Ti si samo REKAO/LA (kroz tip parametra) šta ti treba — ne
kako da se to nabavi. To "davanje spolja" jeste DI. Napomena — od Spring-a 4.3 na dalje, kad klasa
ima TAČNO JEDAN konstruktor, ne treba mu čak ni `@Autowired` iznad — Spring ga automatski prepozna i
koristi (zato ga ni kolega nigde ne piše).

---

## 3. SOLID principi

### D — Dependency Inversion Principle (najjasniji primer kod nas)

**Teorija:** Klase visokog nivoa (poslovna logika) ne treba da zavise direktno od klasa niskog
nivoa (konkretne implementacije) — obe treba da zavise od **apstrakcije** (interfejsa).

**Gde kod nas:** Svih 9 servisa ima interfejs (`service/XService.java`) i implementaciju
(`service/impl/XServiceImpl.java`). Primer: `AccessProcessingServiceImpl` u konstruktoru zavisi od
tipa `GatePassService` (interfejs iz `service/GatePassService.java`), NE od konkretne klase
`GatePassServiceImpl`. On ne zna niti mu je bitno koja je konkretna implementacija — samo zna da
postoji nešto što ume `changeStatus(...)`. Spring u pozadini poveže interfejs sa pravom
implementacijom.

### S — Single Responsibility Principle

**Teorija:** Svaka klasa treba da ima tačno jedan razlog da se menja — jednu jasnu odgovornost.

**Gde kod nas:** Za pojam "propusnica" imamo 4 odvojene klase, svaka sa jednim poslom:
- `controller/GatePassController.java` — samo HTTP (rute, statusi)
- `service/impl/GatePassServiceImpl.java` — poslovna pravila (status-guard, generisanje koda)
- `mapper/GatePassMapper.java` — pretvaranje entitet ↔ DTO
- `repository/GatePassRepository.java` — pristup bazi

### I — Interface Segregation Principle

**Teorija:** Klijent ne treba da zavisi od metoda koje mu ne trebaju — bolje više malih, fokusiranih
interfejsa nego jedan "debeo".

**Gde kod nas:** Prirodno zadovoljeno — svaki servisni interfejs pokriva samo svoj domen
(`GatePassService` ima samo metode o propusnicama, ne i o korisnicima ili zgradama).

### O — Open/Closed i L — Liskov Substitution (iskreno: slabije vidljivi)

**Teorija (O):** Kod treba da bude otvoren za proširenje, zatvoren za izmenu — nova funkcionalnost
se dodaje NOVIM kodom, a ne menjanjem postojećeg.
**Teorija (L):** Ako imaš tip B koji nasleđuje/implementira A, B mora moći da se koristi svuda gde
se očekuje A, bez iznenađenja.

**Gde kod nas:** Iskreno — ova dva su najslabije demonstrirana, i to je normalno za ovakav CRUD
projekat. Svaki naš interfejs ima tačno JEDNU implementaciju, pa se Liskov ne "testira" u praksi
(nema zamene implementacija u letu). Ako profesor pita baš za ova dva, iskrenije je reći da nisu
naglašeno prisutna nego izmišljati primer.

---

## 4. Dizajn obrasci (design patterns) koje prepoznaješ u kodu

### Singleton

**Teorija:** Garantuje da postoji tačno JEDNA instanca neke klase u celoj aplikaciji.

**Gde kod nas:** Svaki Spring bean (`@Service`, `@Repository`, `@Component`) je po difoltu
**singleton scope** — Spring napravi samo jednu instancu `GatePassServiceImpl`-a i tu istu instancu
daje svima kojima treba, kroz celo trajanje aplikacije.

### Builder

**Teorija:** Umesto konstruktora sa gomilom parametara, praviš objekat korak-po-korak preko
lančanih poziva (`.polje(vrednost).polje2(vrednost2)...build()`), čitljivije i bezbednije (jasno je
koje polje dobija koju vrednost).

**Gde kod nas:** Lombok `@Builder` na SVIM entitetima, npr.
`backend/src/main/java/com/buildingaccess/model/User.java:34`. Koristi se svuda pri kreiranju,
npr. u `GatePassServiceImpl.create(...)`:
```java
GatePass gatePass = GatePass.builder()
        .code(generateUniqueCode())
        .guestName(request.guestName())
        .status(GatePassStatus.ACTIVE)
        .build();
```

---

## 5. Slojevita arhitektura (Layered Architecture) i DTO obrazac

**Teorija:** Aplikacija se deli na slojeve sa jasnom odgovornošću — kontroler (HTTP) → servis
(poslovna logika) → repozitorijum (baza). Svaki sloj priča samo sa susednim. DTO (Data Transfer
Object) je poseban objekat koji se šalje klijentu — NIKAD se ne vraća entitet (JPA klasa) direktno.

**Zašto DTO a ne entitet direktno (čest pitanje):**
1. Entitet nosi polja koja ne smeš da izložiš (npr. `password` iz `User`).
2. Entitet ima lenje (`LAZY`) veze ka drugim entitetima — direktna serijalizacija u JSON može da
   pukne ili napravi beskonačnu petlju (Apartment → Building → Apartment → ...).
3. DTO ti daje slobodu da menjaš bazu bez menjanja API "ugovora" sa frontend-om.

**Gde kod nas:** Paket `dto/` (odvojen po feature-u: `dto/gatepass`, `dto/user`...), i `mapper/`
paket koji ručno prevodi entitet u DTO i obrnuto (npr. `GatePassMapper.toResponse(GatePass entity)`).

---

## 6. JPA / Hibernate — ORM osnovni pojmovi

**Teorija — šta je ORM:** Object-Relational Mapping — automatski prevodi Java objekte u redove
relacione baze i obrnuto, umesto da ručno pišeš SQL za svaku operaciju. **JPA** je specifikacija
(interfejs/skup pravila), **Hibernate** je konkretna implementacija koju Spring Boot koristi ispod
haube.

**Veze između entiteta (`@OneToMany`, `@ManyToOne`):**
`backend/src/main/java/com/buildingaccess/model/User.java:62` —
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "apartment_id")
private Apartment apartment;
```
Jedan `User` (stanar) pripada jednom `Apartment`-u (`@ManyToOne`), a jedan `Apartment` ima više
stanara (`@OneToMany` na `Apartment` strani).

**LAZY vs EAGER učitavanje (čest pitanje):**
- `LAZY` — povezani objekat se učitava iz baze TEK kad ga stvarno pozoveš (`user.getApartment()`).
  Podrazumevano i preporučeno za `@ManyToOne`/`@OneToMany` da se izbegne nepotrebno učitavanje
  cele mreže povezanih objekata odjednom.
- `EAGER` — učitava se odmah zajedno sa glavnim objektom, uvek, i kad ti ne treba.
- Kod nas su SVE veze eksplicitno `FetchType.LAZY` (grep kroz `model/` paket) — svesna odluka da
  se ne učitava više podataka nego što je potrebno.

**Kaskadno brisanje (`cascade`) — pazi, ovo smo menjali:**
`backend/src/main/java/com/buildingaccess/model/GatePass.java:80-85` — `EntryLog` i
`PassStatusHistory` NEMAJU `cascade` na vezi sa `GatePass` (namerno uklonjeno) — brisanje
propusnice ne bi trebalo automatski da obriše istoriju/evidenciju (to je "revizijski trag", ne
sme tiho da nestane). `Building` → `Apartment` isto nema cascade — stanovi se brišu eksplicitno u
`BuildingServiceImpl.delete(...)`, tek pošto se proveri da nemaju stanare/propusnice.

---

## 7. `@Transactional` — upravljanje transakcijama

**Teorija:** Transakcija je grupa operacija nad bazom koje se posmatraju kao JEDNA celina — ili
prođu SVE, ili se poništi SVE ("sve ili ništa", ACID princip *atomicity*). Primer zašto je bitno:
ako se propusnica odobri (upiše `EntryLog`) ali onda pukne uvećavanje brojača ulazaka, ne smeš da
ostaneš sa upisanim ulaskom a bez uvećanog brojača — ili oboje uspe, ili se oboje poništi.

**Gde kod nas:** 16 mesta u kodu (`grep -rn "@Transactional"`), npr.
`AccessProcessingServiceImpl.processScan(...)` — cela obrada skeniranja (provera propusnice, upis
`EntryLog`-a, uvećavanje `usedEntries`, eventualna promena statusa) je jedna transakcija.

---

## 8. REST principi

**Teorija:** REST (Representational State Transfer) je stil dizajniranja API-ja preko HTTP-a:
- Svaki resurs ima svoj URL (`/api/admin/buildings/{id}`)
- Koriste se prava HTTP glagoli prema nameni: `GET` (čitanje), `POST` (kreiranje), `PUT` (izmena),
  `DELETE` (brisanje) — ne sve preko `GET`/`POST` kao ranije
- **Stateless** — server ne pamti stanje klijenta između zahteva; svaki zahtev nosi sve što je
  potrebno (kod nas: JWT token u `Authorization` header-u)
- Status kodovi nose značenje (200 OK, 201 Created, 400 Bad Request, 401/403, 404 Not Found)

**Gde kod nas:** Svaki kontroler, npr. `ApartmentController.java` — `@GetMapping`, `@PostMapping`,
`@PutMapping`, `@DeleteMapping` na istoj putanji `/api/admin/apartments`.

---

## 9. Autentikacija/autorizacija — JWT i Spring Security

**Teorija:**
- **Autentikacija** — "ko si ti" (login, provera lozinke).
- **Autorizacija** — "šta smeš" (da li tvoja uloga sme da pozove ovaj endpoint).
- **JWT (JSON Web Token)** — token koji server izda pri login-u, sadrži potpisane podatke (ko je
  korisnik, koja mu je uloga), klijent ga šalje uz svaki sledeći zahtev. Server ne mora da pamti
  sesiju — sve što mu treba je u samom tokenu (=stateless, tačka 8).

**Gde kod nas:**
- `config/SecurityConfig.java:73` — `.sessionCreationPolicy(SessionCreationPolicy.STATELESS)`
- `security/JwtService.java` — pravi i proverava token
- `security/JwtAuthenticationFilter.java` — na svakom zahtevu čita `Authorization: Bearer <token>`,
  proverava ga, i ako je ispravan, "prijavljuje" korisnika za taj jedan zahtev
- Autorizacija po ulozi: `@PreAuthorize("hasAnyRole('ADMIN','SECURITY')")` (npr. u
  `StatisticsController`), i rute po prefiksu u `SecurityConfig` (`/api/admin/**` samo ADMIN, itd.)

---

## 10. Centralizovano rukovanje greškama

**Teorija:** Umesto da svaki kontroler pojedinačno hvata izuzetke i ručno pravi HTTP odgovor za
grešku, jedna klasa na nivou cele aplikacije presreće SVE izuzetke i pretvara ih u ujednačen JSON
odgovor sa odgovarajućim status kodom.

**Gde kod nas:** `exception/GlobalExceptionHandler.java:15` — `@RestControllerAdvice`, sa
zasebnim `@ExceptionHandler` metodama za svaki tip greške:
- `MethodArgumentNotValidException` (validacija forme) → 400
- `ResourceNotFoundException` (npr. propusnica ne postoji) → 404
- `DuplicateResourceException`/`InvalidStatusException` (npr. email već postoji, propusnica se ne
  može menjati) → 409
- `AccessDeniedException` → 403
- generički `Exception` (sve ostalo, kao mreža za sigurnost) → 500

---

## 11. Validacija unosa (Bean Validation)

**Teorija:** Umesto da ručno pišeš `if (ime == null || ime.isBlank()) throw ...` za svako polje,
koristiš gotove anotacije na DTO poljima, a framework sam proveri pri prijemu zahteva.

**Gde kod nas:** `dto/gatepass/GatePassCreateRequest.java` —
```java
@NotBlank String guestName,
@Email String guestEmail,
@Min(1) int maxEntries,
```
plus `@Valid` na parametru u kontroleru (npr. `GatePassController.create(@Valid @RequestBody ...)`)
da se validacija stvarno pokrene. Ako nešto ne prođe, `GlobalExceptionHandler` (tačka 10) to
pretvori u čitljivu 400 grešku.

---

## 12. Pun tok jednog zahteva kroz sistem — primer: kreiranje propusnice (SK3)

**Svrha:** ovo je jedan konkretan primer koji spaja tačke 5–9 (arhitektura, JPA, transakcije, REST,
JWT) kroz JEDAN stvaran zahtev, od klika na dugme do JSON odgovora. Prati fajl + liniju u svakom
koraku i probaj da ga sama ispričaš naglas pre nego što gledaš kod.

> **Napomena ako imaš stariju verziju ovog opisa:** ako negde vidiš `@RequiredArgsConstructor`,
> `GatePass.builder()...build()` ili `JpaSpecificationExecutor` — to je ZASTARELO. Lombok
> (`@RequiredArgsConstructor`, `@Builder`) je uklonjen iz servisa/kontrolera (istorija: commit
> `b74d13f`, ostao je samo na entitetima za `@Getter/@Setter`), a pretraga propusnica više ne
> koristi `Specification` lambda-e nego običan `@Query` JPQL (commit `ccf027d`). Ispod je TRENUTNO
> stanje koda, provereno red-po-red.

### Korak 1 — klik na dugme u React formi

`frontend/src/pages/resident/GatePassFormPage.jsx:74`
```jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  setError('');
  setSaving(true);
  try {
    const payload = {
      guestName: form.guestName,
      guestPhone: form.guestPhone || null,
      guestEmail: form.guestEmail || null,
      reason: form.reason,
      validFrom: form.validFrom,
      validTo: form.validTo,
      maxEntries: Number(form.maxEntries),
    };
    const created = await createGatePass({ ...payload, type: form.type });
    navigate(`/resident/gatepasses/${created.id}`, {
      state: { message: 'Propusnica je uspešno kreirana!' },
    });
  } catch (err) {
    setError(extractErrorMessage(err));
  } finally {
    setSaving(false);
  }
};
```
Forma je kontrolisana React state-om (`form`); `handleSubmit` samo sastavlja `payload` i zove
`createGatePass` — nema nikakve poslovne logike na frontendu (period važenja, generisanje koda...
sve to radi backend).

### Korak 2 — HTTP zahtev preko axios-a, sa JWT tokenom u header-u

`frontend/src/api/gatepasses.js:14`
```js
export const createGatePass = (data) => apiClient.post(RESIDENT_BASE, data).then((r) => r.data);
// RESIDENT_BASE = '/api/resident/gatepasses'
```
Token se NE prosleđuje ručno ovde — dodaje ga axios *interceptor* na svaki zahtev automatski,
`frontend/src/api/client.js:9`:
```js
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('bac_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```
Token je sačuvan u `localStorage` pri loginu (`bac_token`) i otud se čita za svaki naredni zahtev —
zato se u koraku 1 nigde ne pominje autentikacija, ona je "nevidljiva" infrastruktura.

### Korak 3 — `JwtAuthenticationFilter` presreće zahtev (pre bilo kog kontrolera)

`backend/src/main/java/com/buildingaccess/security/JwtAuthenticationFilter.java:31`
```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader(HEADER);
    if (authHeader == null || !authHeader.startsWith(PREFIX)) {
        filterChain.doFilter(request, response);
        return;
    }
    String token = authHeader.substring(PREFIX.length());
    try {
        String email = jwtService.extractEmail(token);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
    } catch (Exception ignored) {
        // nevalidan/istekao token — ostaje neautentifikovan, dalje odlučuje AuthenticationEntryPoint
    }
    filterChain.doFilter(request, response);
}
```
Ovaj filter nasleđuje `OncePerRequestFilter` i registrovan je u `SecurityConfig` sa
`.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)` — izvršava
se za **bukvalno svaki** HTTP zahtev, pre nego što Spring uopšte razmišlja o tome koji kontroler
treba da ga obradi.

### Korak 4 — `JwtService` proverava potpis tokena i čita email

`backend/src/main/java/com/buildingaccess/security/JwtService.java:42`
```java
public String extractEmail(String token) {
    return parseClaims(token).getSubject();
}

public boolean isTokenValid(String token, UserDetails userDetails) {
    try {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}

private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
}
```
`parseSignedClaims` sam proveri HMAC potpis (sa tajnim ključem iz `app.jwt.secret`) — ako je token
izmenjen/falsifikovan, baca izuzetak PRE nego što se bilo šta pročita iz njega. Email je u JWT-u kao
`subject` (postavljen pri loginu u `generateToken(user)` sa `.subject(user.getEmail())`).

### Korak 5 — `UserDetailsServiceImpl` učitava pravog korisnika iz baze

`backend/src/main/java/com/buildingaccess/security/UserDetailsServiceImpl.java:19`
```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen: " + email));
}
```
Bitno: ovo vraća **direktno naš `User` entitet**, ne neki poseban "UserDetails wrapper" — jer
`User implements UserDetails` (`model/User.java:33`). Zato u koraku 8 `@AuthenticationPrincipal`
može da se tipizira kao `User`, sa svim poljima (`getApartment()`, `getBadgeCode()`...), a ne samo
sa email/lozinka kako `UserDetails` inače izgleda.

### Korak 6 — `SecurityConfig` proverava da li ova uloga sme na ovu rutu

`backend/src/main/java/com/buildingaccess/config/SecurityConfig.java:80`
```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/public/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/security/**").hasRole("SECURITY")
        .requestMatchers("/api/resident/**").hasRole("RESIDENT")
        .anyRequest().authenticated()
)
```
`hasRole("RESIDENT")` u pozadini traži authority `"ROLE_RESIDENT"` — tačno ono što
`User.getAuthorities()` vraća (`model/User.java:75`: `new SimpleGrantedAuthority("ROLE_" + role.name())`).
Ako autentikacija iz koraka 3–5 nije uspela (nema tokena, istekao je, ili je uloga pogrešna), zahtev
se ovde zaustavlja — vraća se 401 ili 403 (obrađeno u `.exceptionHandling(...)` u istom fajlu) i
**kontroler se nikad ne pozove**.

### Korak 7 — `DispatcherServlet` rutira ka kontroleru; `@Valid` proverava telo zahteva

`backend/src/main/java/com/buildingaccess/controller/GatePassController.java`
```java
@RestController
@RequestMapping("/api/resident/gatepasses")
public class GatePassController {

    private final GatePassService gatePassService;

    public GatePassController(GatePassService gatePassService) {
        this.gatePassService = gatePassService;
    }

    @PostMapping
    public ResponseEntity<GatePassResponse> create(@Valid @RequestBody GatePassCreateRequest request,
                                                     @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gatePassService.create(request, currentUser));
    }
}
```
`@RequestBody` deserijalizuje JSON u `GatePassCreateRequest` (Java `record`,
`dto/gatepass/GatePassCreateRequest.java:11`):
```java
public record GatePassCreateRequest(
        @NotBlank String guestName,
        String guestPhone,
        @Email String guestEmail,
        @NotBlank String reason,
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validTo,
        @Min(1) int maxEntries,
        @NotNull GatePassType type
) {}
```
`@Valid` kaže Spring-u da PRE ulaska u telo metode `create(...)` proveri sve ove anotacije. Ako npr.
`guestName` stigne prazan, metoda `create` se **uopšte ne izvrši** — baca se
`MethodArgumentNotValidException`, koji hvata `GlobalExceptionHandler` (tačka 10) i vraća 400 sa
spiskom polja koja nisu prošla.

### Korak 8 — `@AuthenticationPrincipal` ubacuje ulogovanog stanara

Spring MVC ima ugrađen "argument resolver" koji, kad vidi parametar obeležen sa
`@AuthenticationPrincipal`, uzme `Authentication` iz `SecurityContextHolder` (upisan u koraku 3) i
prosledi njegov `getPrincipal()` kao vrednost parametra. Pošto je principal ovde naš pravi `User`
objekat (korak 5), `currentUser` u kontroleru je odmah spreman za korišćenje, bez ijednog dodatnog
upita ka bazi u samom kontroleru.

### Korak 9 — kontroler deleguje, servis radi poslovnu logiku (`@Transactional`)

`backend/src/main/java/com/buildingaccess/service/impl/GatePassServiceImpl.java:100`
```java
@Override
@Transactional
public GatePassResponse create(GatePassCreateRequest request, User resident) {
    validatePeriod(request.validFrom(), request.validTo());
    if (resident.getApartment() == null) {
        throw new IllegalArgumentException("Stanar nema dodeljen stan");
    }

    GatePass gatePass = new GatePass();
    gatePass.setCode(generateUniqueCode());
    gatePass.setGuestName(request.guestName());
    gatePass.setGuestPhone(request.guestPhone());
    gatePass.setGuestEmail(request.guestEmail());
    gatePass.setReason(request.reason());
    gatePass.setValidFrom(request.validFrom());
    gatePass.setValidTo(request.validTo());
    gatePass.setMaxEntries(request.maxEntries());
    gatePass.setUsedEntries(0);
    gatePass.setType(request.type());
    gatePass.setStatus(GatePassStatus.ACTIVE);
    gatePass.setCreatedAt(LocalDateTime.now());
    gatePass.setCreatedBy(resident);
    gatePass.setApartment(resident.getApartment());

    GatePass saved = gatePassRepository.save(gatePass);

    if (saved.getGuestEmail() != null && !saved.getGuestEmail().isBlank()) {
        mailService.sendGatePassToGuest(saved.getGuestEmail(), saved.getGuestName(), saved.getCode(),
                saved.getReason(), saved.getValidFrom(), saved.getValidTo());
    }

    return GatePassMapper.toResponse(saved);
}
```
(Nema `.builder()` — entiteti se prave sa `new` + setterima otkad je Lombok `@Builder` uklonjen sa
servisnog koda.) `generateUniqueCode()` koristi `CodeGeneratorUtil`
(`backend/src/main/java/com/buildingaccess/util/CodeGeneratorUtil.java:14`):
```java
public static String generate(String prefix, int randomLength) {
    StringBuilder sb = new StringBuilder(prefix).append('-');
    for (int i = 0; i < randomLength; i++) {
        sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }
    return sb.toString();
}
```
i petlja u servisu (`do { code = ...; } while (gatePassRepository.findByCode(code).isPresent());`)
garantuje da kod ne postoji već u bazi pre nego što se iskoristi.

`@Transactional` znači: `gatePassRepository.save(...)` je unutar jedne transakcije — ako bilo šta
posle (u ovoj metodi) baci izuzetak, Spring automatski radi ROLLBACK i propusnica se NE upisuje u
bazu ni delimično.

**Slanje mejla gostu je "best effort"** — `mailService.sendGatePassToGuest(...)` je pozvano unutar
iste transakcije, ali `MailServiceImpl` interno guta sve greške (loš SMTP, nema interneta...) i nikad
ne baca izuzetak napolje, tako da kreiranje propusnice ne sme da pukne samo zato što mejl nije
otišao (LSP ugovor iz `MailService` interfejsa — vidi tačku 3).

### Korak 10 — `GatePassMapper` pretvara entitet u DTO (i generiše QR usput)

`backend/src/main/java/com/buildingaccess/mapper/GatePassMapper.java:15`
```java
public static GatePassResponse toResponse(GatePass gatePass) {
    return new GatePassResponse(
            gatePass.getId(),
            gatePass.getCode(),
            QrCodeUtil.generateBase64Png(gatePass.getCode()),
            gatePass.getGuestName(),
            gatePass.getGuestPhone(),
            gatePass.getGuestEmail(),
            gatePass.getReason(),
            gatePass.getValidFrom(),
            gatePass.getValidTo(),
            gatePass.getMaxEntries(),
            gatePass.getUsedEntries(),
            gatePass.getType(),
            gatePass.getStatus(),
            gatePass.getCreatedAt(),
            gatePass.getApartment().getId(),
            gatePass.getApartment().getNumber(),
            gatePass.getApartment().getBuilding().getName(),
            gatePass.getCreatedBy().getId()
    );
}
```
`QrCodeUtil.generateBase64Png(...)` (ZXing biblioteka) pretvara sam kod propusnice u PNG sliku
kodiranu kao base64 string — QR se NE čuva u bazi, generiše se iznova iz koda svaki put kad se DTO
pravi (kod → QR je uvek isti, pa nema potrebe za čuvanjem). Frontend odmah dobije `qrCodeBase64` i
prikaže `<img src="data:image/png;base64,...">` bez ikakvog dodatnog zahteva.

### Korak 11 — odgovor se vraća klijentu

Kontroler (korak 7) je već upakovao rezultat: `ResponseEntity.status(HttpStatus.CREATED).body(...)`
→ HTTP `201 Created` sa `GatePassResponse` JSON telom. Na frontendu, `await createGatePass(...)` se
razrešava, i `handleSubmit` (korak 1) radi `navigate(...)` na stranicu novokreirane propusnice.

### Alternativni tok — nešto pukne

Ako npr. `validTo` bude pre `validFrom`, `validatePeriod(...)` u koraku 9 baci
`IllegalArgumentException`. Taj izuzetak probija kroz servis i kontroler (niko ga tamo ne hvata) do
`backend/src/main/java/com/buildingaccess/exception/GlobalExceptionHandler.java:27`:
```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError.of(400, "Bad Request", ex.getMessage(), request.getRequestURI()));
}
```
Klijent dobije čist `400` JSON sa porukom (`extractErrorMessage` u `api/client.js` je pročita i
prikaže korisniku) — nikad "goli" stack trace ili 500 grešku za očekivanu poslovnu grešku.

---

## Brzi podsetnik — redosled učenja

1. Prvo pojmovi 1-3 (IoC, DI, SOLID/DIP) — najverovatnije prvo pitanje, i direktno se nadovezuju.
2. Zatim 5-6 (slojevita arhitektura, DTO, JPA/ORM) — objašnjava STRUKTURU projekta.
3. Zatim 7-9 (transakcije, REST, JWT) — objašnjava KAKO radi jedan zahtev od početka do kraja.
4. Na kraju 4, 10, 11 (dizajn obrasci, greške, validacija) — detalji koje dodaješ ako pitaju dublje.
5. Tačka 12 spaja SVE ovo u jedan konkretan primer, korak po korak, sa stvarnim kodom — nauči je
   poslednju, kad ti je sve ostalo već jasno pojedinačno, i uvežbaj da je ispričaš bez gledanja.

Kad umeš da za svaki pojam kažeš teoriju SVOJIM rečima i onda otvoriš tačan fajl — spremna si.
