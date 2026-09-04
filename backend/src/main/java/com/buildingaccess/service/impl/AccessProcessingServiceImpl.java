package com.buildingaccess.service.impl;

import com.buildingaccess.dto.access.ManualDenyRequest;
import com.buildingaccess.dto.access.PersonSearchResponse;
import com.buildingaccess.dto.access.ScanOutcome;
import com.buildingaccess.dto.access.ScanResultResponse;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.model.AccessDenial;
import com.buildingaccess.model.EntryLog;
import com.buildingaccess.model.GatePass;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.DenialReasonType;
import com.buildingaccess.model.enums.GatePassStatus;
import com.buildingaccess.model.enums.PersonType;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.repository.UserRepository;
import com.buildingaccess.service.AccessProcessingService;
import com.buildingaccess.service.GatePassService;
import com.buildingaccess.service.MailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Srce aplikacije (SK8-SK11): jedan scan endpoint za obezbeđenje prepoznaje da li je uneti/skenirani
 * kod propusnica gosta, lični bedž stanara ili bedž osoblja, i primenjuje odgovarajuću logiku —
 * uključujući automatsko naizmenično evidentiranje ulaska/izlaska za stanara i osoblje.
 */
@Service
public class AccessProcessingServiceImpl implements AccessProcessingService {

    private final GatePassRepository gatePassRepository;
    private final UserRepository userRepository;
    private final EntryLogRepository entryLogRepository;
    private final AccessDenialRepository accessDenialRepository;
    private final GatePassService gatePassService;
    private final MailService mailService;

    public AccessProcessingServiceImpl(GatePassRepository gatePassRepository,
                                        UserRepository userRepository,
                                        EntryLogRepository entryLogRepository,
                                        AccessDenialRepository accessDenialRepository,
                                        GatePassService gatePassService,
                                        MailService mailService) {
        this.gatePassRepository = gatePassRepository;
        this.userRepository = userRepository;
        this.entryLogRepository = entryLogRepository;
        this.accessDenialRepository = accessDenialRepository;
        this.gatePassService = gatePassService;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public ScanResultResponse processScan(String code, User security) {
        requireAssignedBuilding(security);

        Optional<GatePass> gatePassOpt = gatePassRepository.findByCode(code);
        if (gatePassOpt.isPresent()) {
            return processGatePass(gatePassOpt.get(), security);
        }

        Optional<User> personOpt = userRepository.findByBadgeCode(code)
                .filter(u -> u.getRole() == Role.RESIDENT || u.getRole() == Role.STAFF);
        if (personOpt.isPresent()) {
            User person = personOpt.get();
            PersonType type = person.getRole() == Role.RESIDENT ? PersonType.RESIDENT : PersonType.STAFF;
            Optional<EntryLog> last = entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(person.getId());
            return toggleEntry(type, fullName(person), last, e -> e.setUser(person), security, false);
        }

        AccessDenial denial = new AccessDenial();
        denial.setEnteredCode(code);
        denial.setAttemptTime(LocalDateTime.now());
        denial.setReasonType(DenialReasonType.INVALID_CODE);
        denial.setBuilding(security.getBuilding());
        denial.setProcessedBy(security);
        accessDenialRepository.save(denial);
        return new ScanResultResponse(ScanOutcome.DENIED, "Kod nije prepoznat.", null, null, code, null, null);
    }

    /** SK10 — ručna pretraga stanara/osoblja svoje zgrade (ime ili broj stana). */
    @Override
    public List<PersonSearchResponse> searchPeople(String query, User security) {
        requireAssignedBuilding(security);
        Long buildingId = security.getBuilding().getId();

        Stream<PersonSearchResponse> residents = userRepository.searchResidentsInBuilding(buildingId, query).stream()
                .map(u -> new PersonSearchResponse(
                        u.getId(), PersonType.RESIDENT, fullName(u), u.getApartment().getNumber(), null,
                        isCurrentlyInByUser(u.getId())));

        Stream<PersonSearchResponse> staff = userRepository.searchStaffInBuilding(buildingId, query).stream()
                .map(u -> new PersonSearchResponse(
                        u.getId(), PersonType.STAFF, fullName(u), null, u.getJobTitle(),
                        isCurrentlyInByUser(u.getId())));

        return Stream.concat(residents, staff).toList();
    }

    /** SK10 — evidentiranje dolaska ručno pronađene osobe, ista naizmenična logika kao SK9. */
    @Override
    @Transactional
    public ScanResultResponse processManual(PersonType type, Long id, User security) {
        requireAssignedBuilding(security);
        return switch (type) {
            case RESIDENT -> {
                User resident = userRepository.findById(id)
                        .filter(u -> u.getRole() == Role.RESIDENT)
                        .orElseThrow(() -> new ResourceNotFoundException("Stanar nije pronađen: " + id));
                Optional<EntryLog> last = entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(resident.getId());
                yield toggleEntry(PersonType.RESIDENT, fullName(resident), last, e -> e.setUser(resident), security, true);
            }
            case STAFF -> {
                User staff = userRepository.findById(id)
                        .filter(u -> u.getRole() == Role.STAFF)
                        .orElseThrow(() -> new ResourceNotFoundException("Osoblje nije pronađeno: " + id));
                Optional<EntryLog> last = entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(staff.getId());
                yield toggleEntry(PersonType.STAFF, fullName(staff), last, e -> e.setUser(staff), security, true);
            }
            case GUEST -> throw new IllegalArgumentException("Ručni unos nije podržan za goste — koristi se kod/QR propusnice.");
        };
    }

    /** SK11 — ručno odbijanje ulaska, razlog je uvek obavezan. */
    @Override
    @Transactional
    public ScanResultResponse manualDeny(ManualDenyRequest request, User security) {
        requireAssignedBuilding(security);
        if (request.reasonNote() == null || request.reasonNote().isBlank()) {
            throw new IllegalArgumentException("Razlog odbijanja je obavezan");
        }

        GatePass gatePass = null;
        if (request.code() != null && !request.code().isBlank()) {
            gatePass = gatePassRepository.findByCode(request.code()).orElse(null);
        }

        String personName = request.personName() != null
                ? request.personName()
                : (gatePass != null ? gatePass.getGuestName() : null);

        AccessDenial denial = new AccessDenial();
        denial.setEnteredCode(request.code());
        denial.setPersonName(personName);
        denial.setAttemptTime(LocalDateTime.now());
        denial.setReasonType(DenialReasonType.MANUAL_DENIAL);
        denial.setReasonNote(request.reasonNote());
        denial.setGatePass(gatePass);
        denial.setBuilding(security.getBuilding());
        denial.setProcessedBy(security);
        accessDenialRepository.save(denial);

        return new ScanResultResponse(ScanOutcome.DENIED, "Ulazak ručno odbijen.", null, personName, request.code(), null, null);
    }

    // --- interna logika ---

    private ScanResultResponse processGatePass(GatePass gatePass, User security) {
        LocalDateTime now = LocalDateTime.now();

        if (gatePass.getStatus() == GatePassStatus.ACTIVE && now.isAfter(gatePass.getValidTo())) {
            gatePassService.changeStatus(gatePass, GatePassStatus.EXPIRED, null);
        }

        boolean approvable = gatePass.getStatus() == GatePassStatus.ACTIVE
                && gatePass.getUsedEntries() < gatePass.getMaxEntries();

        if (approvable) {
            EntryLog entryLog = new EntryLog();
            entryLog.setPersonType(PersonType.GUEST);
            entryLog.setPersonName(gatePass.getGuestName());
            entryLog.setEntryTime(now);
            entryLog.setBuilding(gatePass.getApartment().getBuilding());
            entryLog.setProcessedBy(security);
            entryLog.setGatePass(gatePass);
            entryLogRepository.save(entryLog);

            gatePass.setUsedEntries(gatePass.getUsedEntries() + 1);
            if (gatePass.getUsedEntries() >= gatePass.getMaxEntries()) {
                gatePassService.changeStatus(gatePass, GatePassStatus.USED_UP, null);
            }

            notifyResidentGatePassUsed(gatePass, now);

            return new ScanResultResponse(ScanOutcome.GUEST_ENTRY_APPROVED, "Ulazak odobren!",
                    PersonType.GUEST, gatePass.getGuestName(), gatePass.getCode(), now, null);
        }

        DenialReasonType reason = switch (gatePass.getStatus()) {
            case EXPIRED -> DenialReasonType.EXPIRED;
            case USED_UP -> DenialReasonType.USED_UP;
            case CANCELED -> DenialReasonType.CANCELED;
            case ACTIVE -> DenialReasonType.USED_UP; // ACTIVE ali dostignut maxEntries (retka konkurentna situacija)
        };

        AccessDenial denial = new AccessDenial();
        denial.setEnteredCode(gatePass.getCode());
        denial.setPersonName(gatePass.getGuestName());
        denial.setAttemptTime(now);
        denial.setReasonType(reason);
        denial.setGatePass(gatePass);
        denial.setBuilding(gatePass.getApartment().getBuilding());
        denial.setProcessedBy(security);
        accessDenialRepository.save(denial);

        return new ScanResultResponse(ScanOutcome.DENIED, denialMessage(reason),
                PersonType.GUEST, gatePass.getGuestName(), gatePass.getCode(), null, null);
    }

    /**
     * SK-poželjno: obaveštava stanara mejlom da je njegov gost upravo ušao (posle upisa EntryLog-a).
     * "Fire and forget" efekat — MailService sam apsorbuje sve greške (nekonfigurisan mejl, mrežni
     * problem itd.) i nikad ne baca izuzetak, tako da ovo ne sme uticati na povratni rezultat skeniranja.
     */
    private void notifyResidentGatePassUsed(GatePass gatePass, LocalDateTime entryTime) {
        User resident = gatePass.getCreatedBy();
        if (resident == null || resident.getEmail() == null || resident.getEmail().isBlank()) {
            return;
        }
        mailService.sendGatePassUsedNotification(
                resident.getEmail(), resident.getFirstName(), gatePass.getGuestName(), gatePass.getReason(), entryTime);
    }

    /** Deljena naizmenična ulazak/izlazak logika za RESIDENT i STAFF (SK9, koristi je i SK10). */
    private ScanResultResponse toggleEntry(PersonType type, String personName, Optional<EntryLog> lastLogOpt,
                                            Consumer<EntryLog> linkPerson, User security, boolean manual) {
        LocalDateTime now = LocalDateTime.now();

        if (lastLogOpt.isEmpty() || lastLogOpt.get().getExitTime() != null) {
            EntryLog entryLog = new EntryLog();
            entryLog.setPersonType(type);
            entryLog.setPersonName(personName);
            entryLog.setEntryTime(now);
            entryLog.setManualEntry(manual);
            entryLog.setNote(manual ? "Ručni unos" : null);
            entryLog.setBuilding(security.getBuilding());
            entryLog.setProcessedBy(security);
            linkPerson.accept(entryLog);
            entryLogRepository.save(entryLog);
            return new ScanResultResponse(ScanOutcome.ENTRY_RECORDED, "Ulazak evidentiran!", type, personName, null, now, null);
        }

        EntryLog last = lastLogOpt.get();
        last.setExitTime(now);
        return new ScanResultResponse(ScanOutcome.EXIT_RECORDED, "Izlazak evidentiran!", type, personName, null, last.getEntryTime(), now);
    }

    private boolean isCurrentlyInByUser(Long userId) {
        return entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(userId)
                .map(e -> e.getExitTime() == null)
                .orElse(false);
    }

    private void requireAssignedBuilding(User security) {
        if (security.getBuilding() == null) {
            throw new IllegalStateException("Obezbeđenje nema dodeljenu zgradu — obratite se administratoru.");
        }
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private String denialMessage(DenialReasonType reason) {
        return switch (reason) {
            case EXPIRED -> "Propusnica je istekla.";
            case USED_UP -> "Propusnica je iskorišćena.";
            case CANCELED -> "Propusnica je otkazana.";
            case INVALID_CODE -> "Kod nije prepoznat.";
            case MANUAL_DENIAL -> "Ulazak ručno odbijen.";
        };
    }
}
