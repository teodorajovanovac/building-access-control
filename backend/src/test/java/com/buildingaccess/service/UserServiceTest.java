package com.buildingaccess.service;

import com.buildingaccess.dto.user.UserCreateRequest;
import com.buildingaccess.dto.user.UserResponse;
import com.buildingaccess.dto.user.UserUpdateRequest;
import com.buildingaccess.exception.DuplicateResourceException;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.model.User;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit testovi za UserService: kreiranje po ulozi (SK15) i brisanje sa proverom povezanih podataka. */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private ApartmentService apartmentService;
    @Mock
    private BuildingService buildingService;
    @Mock
    private GatePassRepository gatePassRepository;
    @Mock
    private EntryLogRepository entryLogRepository;
    @Mock
    private AccessDenialRepository accessDenialRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, apartmentService, buildingService,
                gatePassRepository, entryLogRepository, accessDenialRepository);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
    }

    private UserCreateRequest request(Role role, Long apartmentId, Long buildingId) {
        return new UserCreateRequest("Ime", "Prezime", "user@example.com", "lozinka1", role, apartmentId, buildingId, null);
    }

    private UserCreateRequest staffRequest(Long buildingId, String jobTitle) {
        return new UserCreateRequest("Ime", "Prezime", "user@example.com", "lozinka1", Role.STAFF, null, buildingId, jobTitle);
    }

    @Test
    void create_duplicateEmail_throwsDuplicateResourceException() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request(Role.ADMIN, null, null)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_residentWithoutApartmentId_throwsIllegalArgument() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request(Role.RESIDENT, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_securityWithoutBuildingId_throwsIllegalArgument() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request(Role.SECURITY, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_resident_generatesBadgeCodeAndSetsApartment() {
        Apartment apartment = Apartment.builder().id(10L).number("5").build();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(apartmentService.findEntity(10L)).thenReturn(apartment);
        when(userRepository.existsByBadgeCode(anyString())).thenReturn(false);

        UserResponse response = userService.create(request(Role.RESIDENT, 10L, null));

        assertThat(response.role()).isEqualTo(Role.RESIDENT);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getBadgeCode()).startsWith("RES-");
        assertThat(captor.getValue().getApartment()).isEqualTo(apartment);
        assertThat(captor.getValue().getBuilding()).isNull();
    }

    @Test
    void create_security_setsBuildingAndNoBadgeCode() {
        Building building = Building.builder().id(20L).name("Zgrada B").build();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(buildingService.findEntity(20L)).thenReturn(building);

        UserResponse response = userService.create(request(Role.SECURITY, null, 20L));

        assertThat(response.role()).isEqualTo(Role.SECURITY);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getBuilding()).isEqualTo(building);
        assertThat(captor.getValue().getBadgeCode()).isNull();
        assertThat(captor.getValue().getApartment()).isNull();
    }

    @Test
    void create_staffWithoutBuildingId_throwsIllegalArgument() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request(Role.STAFF, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_staff_generatesBadgeCodeAndSetsBuildingAndJobTitle() {
        Building building = Building.builder().id(30L).name("Zgrada C").build();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(buildingService.findEntity(30L)).thenReturn(building);
        when(userRepository.existsByBadgeCode(anyString())).thenReturn(false);

        UserResponse response = userService.create(staffRequest(30L, "Održavanje"));

        assertThat(response.role()).isEqualTo(Role.STAFF);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getBadgeCode()).startsWith("RES-");
        assertThat(captor.getValue().getBuilding()).isEqualTo(building);
        assertThat(captor.getValue().getJobTitle()).isEqualTo("Održavanje");
        assertThat(captor.getValue().getApartment()).isNull();
    }

    @Test
    void create_admin_needsNoApartmentOrBuilding() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        UserResponse response = userService.create(request(Role.ADMIN, null, null));

        assertThat(response.role()).isEqualTo(Role.ADMIN);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getBadgeCode()).isNull();
        assertThat(captor.getValue().getApartment()).isNull();
        assertThat(captor.getValue().getBuilding()).isNull();
    }

    // ---------- delete — blokirano ako postoje povezani podaci ----------

    private User existingUser() {
        return User.builder().id(1L).firstName("A").lastName("B").email("a@b.com").role(Role.RESIDENT).build();
    }

    @Test
    void delete_referencedByGatePass_throwsInvalidStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(gatePassRepository.existsByCreatedById(1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(1L)).isInstanceOf(InvalidStatusException.class);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void delete_referencedAsEntryLogUser_throwsInvalidStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(gatePassRepository.existsByCreatedById(1L)).thenReturn(false);
        when(entryLogRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(1L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_referencedAsEntryLogProcessedBy_throwsInvalidStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(gatePassRepository.existsByCreatedById(1L)).thenReturn(false);
        when(entryLogRepository.existsByUserId(1L)).thenReturn(false);
        when(entryLogRepository.existsByProcessedById(1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(1L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_referencedAsDenialProcessedBy_throwsInvalidStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(gatePassRepository.existsByCreatedById(1L)).thenReturn(false);
        when(entryLogRepository.existsByUserId(1L)).thenReturn(false);
        when(entryLogRepository.existsByProcessedById(1L)).thenReturn(false);
        when(accessDenialRepository.existsByProcessedById(1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(1L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_noReferences_deletesUser() {
        User user = existingUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(gatePassRepository.existsByCreatedById(1L)).thenReturn(false);
        when(entryLogRepository.existsByUserId(1L)).thenReturn(false);
        when(entryLogRepository.existsByProcessedById(1L)).thenReturn(false);
        when(accessDenialRepository.existsByProcessedById(1L)).thenReturn(false);

        userService.delete(1L);

        verify(userRepository).delete(user);
    }

    // ---------- update ----------

    @Test
    void update_residentRole_updatesApartmentWhenProvided() {
        User user = existingUser();
        Apartment newApartment = Apartment.builder().id(99L).number("9").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(apartmentService.findEntity(99L)).thenReturn(newApartment);

        UserUpdateRequest request = new UserUpdateRequest("Novo", "Ime", 99L, null, null);
        UserResponse response = userService.update(1L, request);

        assertThat(response.firstName()).isEqualTo("Novo");
        assertThat(user.getApartment()).isEqualTo(newApartment);
    }

    @Test
    void update_securityRole_ignoresApartmentId() {
        User securityUser = User.builder().id(2L).firstName("S").lastName("Sec").role(Role.SECURITY).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(securityUser));

        UserUpdateRequest request = new UserUpdateRequest("Novo", "Ime", 99L, null, null);
        userService.update(2L, request);

        assertThat(securityUser.getApartment()).isNull();
        verify(apartmentService, never()).findEntity(any());
    }

    @Test
    void update_staffRole_updatesBuildingAndJobTitle() {
        Building newBuilding = Building.builder().id(40L).name("Zgrada D").build();
        User staffUser = User.builder().id(3L).firstName("S").lastName("Taff").role(Role.STAFF).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(staffUser));
        when(buildingService.findEntity(40L)).thenReturn(newBuilding);

        UserUpdateRequest request = new UserUpdateRequest("Novo", "Ime", null, 40L, "Recepcija");
        userService.update(3L, request);

        assertThat(staffUser.getBuilding()).isEqualTo(newBuilding);
        assertThat(staffUser.getJobTitle()).isEqualTo("Recepcija");
        verify(apartmentService, never()).findEntity(any());
    }
}
