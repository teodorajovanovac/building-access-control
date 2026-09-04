package com.buildingaccess.service;

import com.buildingaccess.dto.gatepass.GatePassCreateRequest;
import com.buildingaccess.dto.gatepass.GatePassResponse;
import com.buildingaccess.dto.gatepass.GatePassUpdateRequest;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.model.GatePass;
import com.buildingaccess.model.PassStatusHistory;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.GatePassStatus;
import com.buildingaccess.model.enums.GatePassType;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.repository.PassStatusHistoryRepository;
import com.buildingaccess.service.impl.GatePassServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit testovi za GatePassService: kreiranje propusnice, status-guard na izmenu/otkazivanje
 * (SK4/SK5 — dozvoljeno samo dok je ACTIVE) i pristup podacima (vlasnik ili "elevated" uloga).
 */
@ExtendWith(MockitoExtension.class)
class GatePassServiceTest {

    @Mock
    private GatePassRepository gatePassRepository;
    @Mock
    private PassStatusHistoryRepository historyRepository;
    @Mock
    private MailService mailService;

    private GatePassService service;

    private User resident;
    private Apartment apartment;

    @BeforeEach
    void setUp() {
        service = new GatePassServiceImpl(gatePassRepository, historyRepository, mailService);
        Building building = Building.builder().id(1L).name("Zgrada A").address("Adresa 1").build();
        apartment = Apartment.builder().id(10L).number("12").floor(3).building(building).build();
        resident = User.builder().id(50L).firstName("Stanar").lastName("Stanarić").role(Role.RESIDENT)
                .apartment(apartment).build();
    }

    private GatePassCreateRequest createRequest(LocalDateTime from, LocalDateTime to, int maxEntries) {
        return new GatePassCreateRequest("Gost", "0601234567", "gost@example.com", "Poseta", from, to, maxEntries, GatePassType.LIMITED);
    }

    // ---------- create (SK3) ----------

    @Test
    void create_validToBeforeValidFrom_throwsIllegalArgument() {
        LocalDateTime from = LocalDateTime.now().plusDays(2);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> service.create(createRequest(from, to, 1), resident))
                .isInstanceOf(IllegalArgumentException.class);
        verify(gatePassRepository, never()).save(any(GatePass.class));
    }

    @Test
    void create_validToInThePast_throwsIllegalArgument() {
        LocalDateTime from = LocalDateTime.now().minusDays(10);
        LocalDateTime to = LocalDateTime.now().minusDays(5);

        assertThatThrownBy(() -> service.create(createRequest(from, to, 1), resident))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_residentWithoutApartment_throwsIllegalArgument() {
        User residentNoApartment = User.builder().id(51L).role(Role.RESIDENT).apartment(null).build();
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> service.create(createRequest(from, to, 1), residentNoApartment))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_validRequest_savesActiveGatePassWithZeroUsedEntries() {
        when(gatePassRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(gatePassRepository.save(any(GatePass.class))).thenAnswer(inv -> {
            GatePass gp = inv.getArgument(0);
            gp.setId(777L);
            return gp;
        });

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        GatePassResponse response = service.create(createRequest(from, to, 3), resident);

        assertThat(response.status()).isEqualTo(GatePassStatus.ACTIVE);
        assertThat(response.usedEntries()).isEqualTo(0);
        assertThat(response.maxEntries()).isEqualTo(3);
        assertThat(response.code()).startsWith("GP-");
        assertThat(response.apartmentId()).isEqualTo(10L);

        ArgumentCaptor<GatePass> captor = ArgumentCaptor.forClass(GatePass.class);
        verify(gatePassRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(resident);
    }

    @Test
    void create_guestEmailProvided_sendsGuestNotification() {
        when(gatePassRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(gatePassRepository.save(any(GatePass.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        service.create(createRequest(from, to, 1), resident); // createRequest fills guestEmail

        verify(mailService).sendGatePassToGuest(
                anyString(), anyString(), anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void create_noGuestEmail_doesNotSendGuestNotification() {
        when(gatePassRepository.findByCode(anyString())).thenReturn(Optional.empty());
        when(gatePassRepository.save(any(GatePass.class))).thenAnswer(inv -> inv.getArgument(0));
        GatePassCreateRequest requestNoEmail = new GatePassCreateRequest(
                "Gost", "0601234567", null, "Poseta", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, GatePassType.LIMITED);

        service.create(requestNoEmail, resident);

        verify(mailService, never()).sendGatePassToGuest(
                anyString(), anyString(), anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    // ---------- update (SK4) ----------

    private GatePass existingActiveGatePass() {
        return GatePass.builder().id(1L).code("GP-EXIST0001").guestName("Gost")
                .status(GatePassStatus.ACTIVE).usedEntries(1).maxEntries(5)
                .validFrom(LocalDateTime.now().minusDays(1)).validTo(LocalDateTime.now().plusDays(1))
                .type(GatePassType.LIMITED).createdBy(resident).apartment(apartment).build();
    }

    private GatePassUpdateRequest updateRequest(LocalDateTime from, LocalDateTime to, int maxEntries) {
        return new GatePassUpdateRequest("Novi gost", "0611111111", "novi@example.com", "Nova poseta", from, to, maxEntries);
    }

    @Test
    void update_notOwner_throwsAccessDenied() {
        GatePass gatePass = existingActiveGatePass();
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));
        User otherResident = User.builder().id(999L).role(Role.RESIDENT).build();

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        assertThatThrownBy(() -> service.update(1L, updateRequest(from, to, 5), otherResident))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void update_notActiveStatus_throwsInvalidStatus() {
        GatePass gatePass = existingActiveGatePass();
        gatePass.setStatus(GatePassStatus.EXPIRED);
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        assertThatThrownBy(() -> service.update(1L, updateRequest(from, to, 5), resident))
                .isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void update_maxEntriesLowerThanUsedEntries_throwsIllegalArgument() {
        GatePass gatePass = existingActiveGatePass(); // usedEntries = 1
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(1);
        assertThatThrownBy(() -> service.update(1L, updateRequest(from, to, 0), resident))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void update_validRequest_updatesFields() {
        GatePass gatePass = existingActiveGatePass();
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(3);
        GatePassResponse response = service.update(1L, updateRequest(from, to, 5), resident);

        assertThat(response.guestName()).isEqualTo("Novi gost");
        assertThat(response.maxEntries()).isEqualTo(5);
        assertThat(gatePass.getReason()).isEqualTo("Nova poseta");
    }

    // ---------- cancel (SK5) ----------

    @Test
    void cancel_notActiveStatus_throwsInvalidStatus() {
        GatePass gatePass = existingActiveGatePass();
        gatePass.setStatus(GatePassStatus.CANCELED);
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));

        assertThatThrownBy(() -> service.cancel(1L, resident))
                .isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void cancel_notOwner_throwsAccessDenied() {
        GatePass gatePass = existingActiveGatePass();
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));
        User otherResident = User.builder().id(999L).role(Role.RESIDENT).build();

        assertThatThrownBy(() -> service.cancel(1L, otherResident))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancel_activeGatePass_setsCanceledAndRecordsHistory() {
        GatePass gatePass = existingActiveGatePass();
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));

        service.cancel(1L, resident);

        assertThat(gatePass.getStatus()).isEqualTo(GatePassStatus.CANCELED);
        ArgumentCaptor<PassStatusHistory> captor = ArgumentCaptor.forClass(PassStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getPreviousStatus()).isEqualTo(GatePassStatus.ACTIVE);
        assertThat(captor.getValue().getNewStatus()).isEqualTo(GatePassStatus.CANCELED);
        assertThat(captor.getValue().getChangedBy()).isEqualTo(resident);
    }

    // ---------- getById — vlasništvo / povlašćena uloga ----------

    @Test
    void getById_owner_isAllowed() {
        GatePass gatePass = existingActiveGatePass();
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));

        GatePassResponse response = service.getById(1L, resident);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void getById_nonOwnerResident_throwsAccessDenied() {
        GatePass gatePass = existingActiveGatePass();
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));
        User otherResident = User.builder().id(999L).role(Role.RESIDENT).build();

        assertThatThrownBy(() -> service.getById(1L, otherResident))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getById_securityRole_isAllowedEvenIfNotOwner() {
        GatePass gatePass = existingActiveGatePass();
        when(gatePassRepository.findById(1L)).thenReturn(Optional.of(gatePass));
        User security = User.builder().id(999L).role(Role.SECURITY).build();

        GatePassResponse response = service.getById(1L, security);

        assertThat(response.id()).isEqualTo(1L);
    }
}
