package com.buildingaccess.service;

import com.buildingaccess.dto.access.ManualDenyRequest;
import com.buildingaccess.dto.access.ScanOutcome;
import com.buildingaccess.dto.access.ScanResultResponse;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.model.AccessDenial;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.model.EntryLog;
import com.buildingaccess.model.GatePass;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.DenialReasonType;
import com.buildingaccess.model.enums.GatePassStatus;
import com.buildingaccess.model.enums.GatePassType;
import com.buildingaccess.model.enums.PersonType;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit testovi za srce aplikacije (SK8-SK11): AccessProcessingService.processScan prepoznaje
 * propusnicu / lični bedž stanara / bedž osoblja i primenjuje odgovarajuću poslovnu logiku.
 * Svi repozitorijumi i GatePassService su mokovani — nema Spring konteksta, nema baze.
 */
@ExtendWith(MockitoExtension.class)
class AccessProcessingServiceTest {

    @Mock
    private GatePassRepository gatePassRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EntryLogRepository entryLogRepository;
    @Mock
    private AccessDenialRepository accessDenialRepository;
    @Mock
    private GatePassService gatePassService;

    private AccessProcessingService service;

    private Building building;
    private User security;
    private Apartment apartment;

    @BeforeEach
    void setUp() {
        service = new AccessProcessingService(gatePassRepository, userRepository,
                entryLogRepository, accessDenialRepository, gatePassService);

        building = Building.builder().id(1L).name("Zgrada A").address("Adresa 1").build();
        apartment = Apartment.builder().id(10L).number("12").floor(3).building(building).build();
        security = User.builder().id(100L).firstName("Pera").lastName("Perić").role(Role.SECURITY)
                .building(building).build();

        // changeStatus u pravoj implementaciji menja status na propusnici i beleži istoriju;
        // simuliramo isto ponašanje da bismo mogli da proverimo posledičnu (approvable) logiku.
        // lenient() jer je ovaj stub relevantan samo za testove koji zaista prolaze kroz granu
        // propusnice (processGatePass) — ostali (bedž stanara/osoblja, ručni slučajevi) ga ne koriste.
        lenient().doAnswer(invocation -> {
            GatePass gp = invocation.getArgument(0);
            GatePassStatus newStatus = invocation.getArgument(1);
            gp.setStatus(newStatus);
            return null;
        }).when(gatePassService).changeStatus(any(GatePass.class), any(GatePassStatus.class), any());
    }

    private GatePass activeGatePass(int usedEntries, int maxEntries, LocalDateTime validTo) {
        return GatePass.builder()
                .id(500L)
                .code("GP-TEST1234")
                .guestName("Gost Gostić")
                .status(GatePassStatus.ACTIVE)
                .usedEntries(usedEntries)
                .maxEntries(maxEntries)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(validTo)
                .type(GatePassType.LIMITED)
                .apartment(apartment)
                .build();
    }

    // ---------- SK8 — propusnica gosta ----------

    @Test
    void processScan_validGatePass_approvesEntryAndIncrementsUsedEntries() {
        GatePass gatePass = activeGatePass(0, 2, LocalDateTime.now().plusDays(1));
        when(gatePassRepository.findByCode("GP-TEST1234")).thenReturn(Optional.of(gatePass));

        ScanResultResponse result = service.processScan("GP-TEST1234", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.GUEST_ENTRY_APPROVED);
        assertThat(gatePass.getUsedEntries()).isEqualTo(1);
        assertThat(gatePass.getStatus()).isEqualTo(GatePassStatus.ACTIVE);
        verify(entryLogRepository).save(any(EntryLog.class));
        verify(gatePassService, never()).changeStatus(any(), eq(GatePassStatus.USED_UP), any());
    }

    @Test
    void processScan_gatePassReachesMaxEntriesOnThisScan_transitionsToUsedUp() {
        // usedEntries == maxEntries - 1: ovaj scan ga dovodi tačno do limita.
        GatePass gatePass = activeGatePass(1, 2, LocalDateTime.now().plusDays(1));
        when(gatePassRepository.findByCode("GP-TEST1234")).thenReturn(Optional.of(gatePass));

        ScanResultResponse result = service.processScan("GP-TEST1234", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.GUEST_ENTRY_APPROVED);
        assertThat(gatePass.getUsedEntries()).isEqualTo(2);
        verify(gatePassService).changeStatus(gatePass, GatePassStatus.USED_UP, null);
    }

    @Test
    void processScan_gatePassExactlyAtValidTo_isNotYetExpired() {
        // "now.isAfter(validTo)" — kada je validTo tačno sada (ili tek prošlo), granica je bitna.
        // Koristimo validTo malo u budućnosti da izbegnemo flaky test zbog stvarnog vremena izvršavanja.
        GatePass gatePass = activeGatePass(0, 1, LocalDateTime.now().plusNanos(500_000_000));
        when(gatePassRepository.findByCode("GP-TEST1234")).thenReturn(Optional.of(gatePass));

        ScanResultResponse result = service.processScan("GP-TEST1234", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.GUEST_ENTRY_APPROVED);
        verify(gatePassService, never()).changeStatus(any(), eq(GatePassStatus.EXPIRED), any());
    }

    @Test
    void processScan_gatePassPastValidTo_autoExpiresAndDenies() {
        GatePass gatePass = activeGatePass(0, 5, LocalDateTime.now().minusMinutes(1));
        when(gatePassRepository.findByCode("GP-TEST1234")).thenReturn(Optional.of(gatePass));

        ScanResultResponse result = service.processScan("GP-TEST1234", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.DENIED);
        assertThat(gatePass.getStatus()).isEqualTo(GatePassStatus.EXPIRED);
        verify(gatePassService).changeStatus(gatePass, GatePassStatus.EXPIRED, null);

        ArgumentCaptor<AccessDenial> captor = ArgumentCaptor.forClass(AccessDenial.class);
        verify(accessDenialRepository).save(captor.capture());
        assertThat(captor.getValue().getReasonType()).isEqualTo(DenialReasonType.EXPIRED);
        verify(entryLogRepository, never()).save(any());
    }

    @Test
    void processScan_gatePassAlreadyCanceled_isDenied() {
        GatePass gatePass = activeGatePass(0, 5, LocalDateTime.now().plusDays(1));
        gatePass.setStatus(GatePassStatus.CANCELED);
        when(gatePassRepository.findByCode("GP-TEST1234")).thenReturn(Optional.of(gatePass));

        ScanResultResponse result = service.processScan("GP-TEST1234", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.DENIED);
        ArgumentCaptor<AccessDenial> captor = ArgumentCaptor.forClass(AccessDenial.class);
        verify(accessDenialRepository).save(captor.capture());
        assertThat(captor.getValue().getReasonType()).isEqualTo(DenialReasonType.CANCELED);
        // Otkazana propusnica se ne dira dodatno — nema nepotrebnog poziva changeStatus.
        verify(gatePassService, never()).changeStatus(any(), any(), any());
    }

    @Test
    void processScan_gatePassAlreadyUsedUp_isDenied() {
        GatePass gatePass = activeGatePass(3, 3, LocalDateTime.now().plusDays(1));
        gatePass.setStatus(GatePassStatus.USED_UP);
        when(gatePassRepository.findByCode("GP-TEST1234")).thenReturn(Optional.of(gatePass));

        ScanResultResponse result = service.processScan("GP-TEST1234", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.DENIED);
        ArgumentCaptor<AccessDenial> captor = ArgumentCaptor.forClass(AccessDenial.class);
        verify(accessDenialRepository).save(captor.capture());
        assertThat(captor.getValue().getReasonType()).isEqualTo(DenialReasonType.USED_UP);
    }

    @Test
    void processScan_activeGatePassButAlreadyAtLimit_deniedAsUsedUp() {
        // Redak konkurentni slučaj iz komentara u servisu: status je i dalje ACTIVE,
        // ali je usedEntries >= maxEntries (npr. dva simultana skeniranja).
        GatePass gatePass = activeGatePass(2, 2, LocalDateTime.now().plusDays(1));
        when(gatePassRepository.findByCode("GP-TEST1234")).thenReturn(Optional.of(gatePass));

        ScanResultResponse result = service.processScan("GP-TEST1234", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.DENIED);
        ArgumentCaptor<AccessDenial> captor = ArgumentCaptor.forClass(AccessDenial.class);
        verify(accessDenialRepository).save(captor.capture());
        assertThat(captor.getValue().getReasonType()).isEqualTo(DenialReasonType.USED_UP);
    }

    // ---------- SK9 — lični bedž stanara/osoblja (automatsko naizmenično ulaz/izlaz) ----------

    @Test
    void processScan_residentBadge_noPriorLog_recordsEntry() {
        User resident = User.builder().id(20L).firstName("Ana").lastName("Anić").role(Role.RESIDENT)
                .badgeCode("RES-AAAA1111").apartment(apartment).build();
        when(gatePassRepository.findByCode("RES-AAAA1111")).thenReturn(Optional.empty());
        when(userRepository.findByBadgeCode("RES-AAAA1111")).thenReturn(Optional.of(resident));
        when(entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(20L)).thenReturn(Optional.empty());

        ScanResultResponse result = service.processScan("RES-AAAA1111", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.ENTRY_RECORDED);
        assertThat(result.personType()).isEqualTo(PersonType.RESIDENT);
        assertThat(result.personName()).isEqualTo("Ana Anić");

        ArgumentCaptor<EntryLog> captor = ArgumentCaptor.forClass(EntryLog.class);
        verify(entryLogRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(resident);
        assertThat(captor.getValue().getExitTime()).isNull();
        assertThat(captor.getValue().isManualEntry()).isFalse();
    }

    @Test
    void processScan_residentBadge_secondConsecutiveScan_recordsExit() {
        User resident = User.builder().id(20L).firstName("Ana").lastName("Anić").role(Role.RESIDENT)
                .badgeCode("RES-AAAA1111").apartment(apartment).build();
        EntryLog openLog = EntryLog.builder().id(900L).personType(PersonType.RESIDENT)
                .entryTime(LocalDateTime.now().minusHours(2)).exitTime(null).user(resident).build();

        when(gatePassRepository.findByCode("RES-AAAA1111")).thenReturn(Optional.empty());
        when(userRepository.findByBadgeCode("RES-AAAA1111")).thenReturn(Optional.of(resident));
        when(entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(20L)).thenReturn(Optional.of(openLog));

        ScanResultResponse result = service.processScan("RES-AAAA1111", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.EXIT_RECORDED);
        assertThat(openLog.getExitTime()).isNotNull();
        // Postojeći zapis se samo ažurira (dirty checking) — ne pravi se novi.
        verify(entryLogRepository, never()).save(any());
    }

    @Test
    void processScan_staffBadge_noPriorLog_recordsEntry() {
        User staff = User.builder().id(30L).firstName("Marko").lastName("Održavanje").role(Role.STAFF)
                .jobTitle("Održavanje").badgeCode("STF-B1B1B1B1").building(building).build();
        when(gatePassRepository.findByCode("STF-B1B1B1B1")).thenReturn(Optional.empty());
        when(userRepository.findByBadgeCode("STF-B1B1B1B1")).thenReturn(Optional.of(staff));
        when(entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(30L)).thenReturn(Optional.empty());

        ScanResultResponse entry = service.processScan("STF-B1B1B1B1", security);

        assertThat(entry.outcome()).isEqualTo(ScanOutcome.ENTRY_RECORDED);
        assertThat(entry.personType()).isEqualTo(PersonType.STAFF);
        assertThat(entry.personName()).isEqualTo("Marko Održavanje");

        ArgumentCaptor<EntryLog> captor = ArgumentCaptor.forClass(EntryLog.class);
        verify(entryLogRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(staff);
        assertThat(captor.getValue().getPersonType()).isEqualTo(PersonType.STAFF);
    }

    @Test
    void processScan_staffBadge_secondConsecutiveScan_recordsExit() {
        User staff = User.builder().id(30L).firstName("Marko").lastName("Održavanje").role(Role.STAFF)
                .jobTitle("Održavanje").badgeCode("STF-B1B1B1B1").building(building).build();
        EntryLog openLog = EntryLog.builder().id(901L).personType(PersonType.STAFF)
                .entryTime(LocalDateTime.now().minusHours(1)).exitTime(null).user(staff).build();

        when(gatePassRepository.findByCode("STF-B1B1B1B1")).thenReturn(Optional.empty());
        when(userRepository.findByBadgeCode("STF-B1B1B1B1")).thenReturn(Optional.of(staff));
        when(entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(30L)).thenReturn(Optional.of(openLog));

        ScanResultResponse exit = service.processScan("STF-B1B1B1B1", security);

        assertThat(exit.outcome()).isEqualTo(ScanOutcome.EXIT_RECORDED);
        assertThat(openLog.getExitTime()).isNotNull();
        verify(entryLogRepository, never()).save(any());
    }

    // ---------- SK11 — nevalidan kod ----------

    @Test
    void processScan_unrecognizedCode_isDeniedWithInvalidCodeReason() {
        when(gatePassRepository.findByCode("NEPOSTOJI")).thenReturn(Optional.empty());
        when(userRepository.findByBadgeCode("NEPOSTOJI")).thenReturn(Optional.empty());

        ScanResultResponse result = service.processScan("NEPOSTOJI", security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.DENIED);
        ArgumentCaptor<AccessDenial> captor = ArgumentCaptor.forClass(AccessDenial.class);
        verify(accessDenialRepository).save(captor.capture());
        assertThat(captor.getValue().getReasonType()).isEqualTo(DenialReasonType.INVALID_CODE);
        assertThat(captor.getValue().getEnteredCode()).isEqualTo("NEPOSTOJI");
    }

    @Test
    void processScan_securityWithoutAssignedBuilding_throwsIllegalState() {
        User securityNoBuilding = User.builder().id(101L).role(Role.SECURITY).building(null).build();

        assertThatThrownBy(() -> service.processScan("ANY-CODE", securityNoBuilding))
                .isInstanceOf(IllegalStateException.class);
    }

    // ---------- SK10 — ručna pretraga/evidencija ----------

    @Test
    void processManual_resident_recordsEntryWithManualFlag() {
        User resident = User.builder().id(21L).firstName("Iva").lastName("Ivić").role(Role.RESIDENT)
                .apartment(apartment).build();
        when(userRepository.findById(21L)).thenReturn(Optional.of(resident));
        when(entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(21L)).thenReturn(Optional.empty());

        ScanResultResponse result = service.processManual(PersonType.RESIDENT, 21L, security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.ENTRY_RECORDED);
        ArgumentCaptor<EntryLog> captor = ArgumentCaptor.forClass(EntryLog.class);
        verify(entryLogRepository).save(captor.capture());
        assertThat(captor.getValue().isManualEntry()).isTrue();
        assertThat(captor.getValue().getNote()).isEqualTo("Ručni unos");
    }

    @Test
    void processManual_residentNotFound_throwsResourceNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processManual(PersonType.RESIDENT, 999L, security))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processManual_staffNotFound_throwsResourceNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processManual(PersonType.STAFF, 999L, security))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processManual_staff_recordsEntryWithManualFlag() {
        User staff = User.builder().id(31L).firstName("Nina").lastName("Čistačica").role(Role.STAFF)
                .jobTitle("Čišćenje").building(building).build();
        when(userRepository.findById(31L)).thenReturn(Optional.of(staff));
        when(entryLogRepository.findFirstByUserIdOrderByEntryTimeDesc(31L)).thenReturn(Optional.empty());

        ScanResultResponse result = service.processManual(PersonType.STAFF, 31L, security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.ENTRY_RECORDED);
        assertThat(result.personType()).isEqualTo(PersonType.STAFF);
        ArgumentCaptor<EntryLog> captor = ArgumentCaptor.forClass(EntryLog.class);
        verify(entryLogRepository).save(captor.capture());
        assertThat(captor.getValue().isManualEntry()).isTrue();
        assertThat(captor.getValue().getUser()).isEqualTo(staff);
    }

    @Test
    void processManual_guestType_isNotSupported() {
        assertThatThrownBy(() -> service.processManual(PersonType.GUEST, 1L, security))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- SK11 — ručno odbijanje ----------

    @Test
    void manualDeny_blankReason_throwsIllegalArgument() {
        ManualDenyRequest request = new ManualDenyRequest("GP-CODE", "Neko", "   ");

        assertThatThrownBy(() -> service.manualDeny(request, security))
                .isInstanceOf(IllegalArgumentException.class);
        verify(accessDenialRepository, never()).save(any());
    }

    @Test
    void manualDeny_nullReason_throwsIllegalArgument() {
        ManualDenyRequest request = new ManualDenyRequest(null, null, null);

        assertThatThrownBy(() -> service.manualDeny(request, security))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void manualDeny_withValidReason_savesDenialWithManualReasonType() {
        // code je null pa se gatePassRepository uopšte ne poziva (grana za pretragu propusnice se preskače).
        ManualDenyRequest request = new ManualDenyRequest(null, "Sumnjivo lice", "Nema najavu, obezbeđenje odbija ulaz");

        ScanResultResponse result = service.manualDeny(request, security);

        assertThat(result.outcome()).isEqualTo(ScanOutcome.DENIED);
        ArgumentCaptor<AccessDenial> captor = ArgumentCaptor.forClass(AccessDenial.class);
        verify(accessDenialRepository).save(captor.capture());
        assertThat(captor.getValue().getReasonType()).isEqualTo(DenialReasonType.MANUAL_DENIAL);
        assertThat(captor.getValue().getReasonNote()).isEqualTo("Nema najavu, obezbeđenje odbija ulaz");
        assertThat(captor.getValue().getPersonName()).isEqualTo("Sumnjivo lice");
    }

    @Test
    void manualDeny_withCodeButNoPersonName_fallsBackToGatePassGuestName() {
        GatePass gatePass = activeGatePass(0, 1, LocalDateTime.now().plusDays(1));
        when(gatePassRepository.findByCode("GP-TEST1234")).thenReturn(Optional.of(gatePass));
        ManualDenyRequest request = new ManualDenyRequest("GP-TEST1234", null, "Ipak ne ulazi, procena obezbeđenja");

        ScanResultResponse result = service.manualDeny(request, security);

        assertThat(result.personName()).isEqualTo("Gost Gostić");
        ArgumentCaptor<AccessDenial> captor = ArgumentCaptor.forClass(AccessDenial.class);
        verify(accessDenialRepository).save(captor.capture());
        assertThat(captor.getValue().getGatePass()).isEqualTo(gatePass);
    }
}
