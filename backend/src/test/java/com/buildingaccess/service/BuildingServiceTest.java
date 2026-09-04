package com.buildingaccess.service;

import com.buildingaccess.dto.building.BuildingRequest;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.repository.ApartmentRepository;
import com.buildingaccess.repository.BuildingRepository;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.repository.UserRepository;
import com.buildingaccess.service.impl.BuildingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit testovi za BuildingService.delete: SK13 — brisanje blokirano ako zgrada ima zavisne podatke. */
@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;
    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GatePassRepository gatePassRepository;
    @Mock
    private EntryLogRepository entryLogRepository;
    @Mock
    private AccessDenialRepository accessDenialRepository;

    private BuildingService buildingService;

    @BeforeEach
    void setUp() {
        buildingService = new BuildingServiceImpl(buildingRepository, apartmentRepository, userRepository,
                gatePassRepository, entryLogRepository, accessDenialRepository);
    }

    private Building buildingWith(List<Apartment> apartments) {
        Building building = new Building();
        building.setId(1L);
        building.setName("Zgrada A");
        building.setAddress("Adresa 1");
        building.setApartments(apartments);
        return building;
    }

    @Test
    void delete_apartmentHasResidents_throwsInvalidStatus() {
        User resident = new User();
        resident.setId(1L);
        resident.setRole(Role.RESIDENT);
        Apartment apartment = new Apartment();
        apartment.setId(10L);
        apartment.setNumber("1");
        apartment.setResidents(List.of(resident));
        Building building = buildingWith(List.of(apartment));
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));

        assertThatThrownBy(() -> buildingService.delete(1L)).isInstanceOf(InvalidStatusException.class);
        verify(buildingRepository, org.mockito.Mockito.never()).delete(building);
    }

    @Test
    void delete_apartmentHasGatePasses_throwsInvalidStatus() {
        Apartment apartment = new Apartment();
        apartment.setId(10L);
        apartment.setNumber("1");
        apartment.setResidents(new ArrayList<>());
        Building building = buildingWith(List.of(apartment));
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(gatePassRepository.existsByApartmentId(10L)).thenReturn(true);

        assertThatThrownBy(() -> buildingService.delete(1L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_buildingHasStaff_throwsInvalidStatus() {
        Building building = buildingWith(List.of());
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(userRepository.existsByRoleAndBuildingId(Role.STAFF, 1L)).thenReturn(true);

        assertThatThrownBy(() -> buildingService.delete(1L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_buildingHasAssignedSecurity_throwsInvalidStatus() {
        Building building = buildingWith(List.of());
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        lenient().when(userRepository.existsByRoleAndBuildingId(Role.STAFF, 1L)).thenReturn(false);
        when(userRepository.existsByRoleAndBuildingId(Role.SECURITY, 1L)).thenReturn(true);

        assertThatThrownBy(() -> buildingService.delete(1L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_hasEntryLogs_throwsInvalidStatus() {
        Building building = buildingWith(List.of());
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(entryLogRepository.existsByBuildingId(1L)).thenReturn(true);

        assertThatThrownBy(() -> buildingService.delete(1L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_hasAccessDenials_throwsInvalidStatus() {
        Building building = buildingWith(List.of());
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        lenient().when(entryLogRepository.existsByBuildingId(1L)).thenReturn(false);
        when(accessDenialRepository.existsByBuildingId(1L)).thenReturn(true);

        assertThatThrownBy(() -> buildingService.delete(1L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_noDependentData_deletesBuilding() {
        Building building = buildingWith(List.of());
        when(buildingRepository.findById(1L)).thenReturn(Optional.of(building));
        when(entryLogRepository.existsByBuildingId(1L)).thenReturn(false);
        when(accessDenialRepository.existsByBuildingId(1L)).thenReturn(false);

        buildingService.delete(1L);

        verify(buildingRepository).delete(building);
    }

    @Test
    void create_savesBuildingWithGivenFields() {
        when(buildingRepository.save(org.mockito.ArgumentMatchers.any(Building.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var response = buildingService.create(new BuildingRequest("Zgrada C", "Adresa 3"));

        assertThat(response.name()).isEqualTo("Zgrada C");
        assertThat(response.address()).isEqualTo("Adresa 3");
    }
}
