package com.buildingaccess.service;

import com.buildingaccess.dto.apartment.ApartmentRequest;
import com.buildingaccess.dto.apartment.ApartmentResponse;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.ApartmentRepository;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.service.impl.ApartmentServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit testovi za ApartmentService.delete: SK14 — brisanje blokirano ako stan ima stanare ili propusnice. */
@ExtendWith(MockitoExtension.class)
class ApartmentServiceTest {

    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private GatePassRepository gatePassRepository;
    @Mock
    private BuildingService buildingService;

    private ApartmentService apartmentService;

    @BeforeEach
    void setUp() {
        apartmentService = new ApartmentServiceImpl(apartmentRepository, gatePassRepository, buildingService);
    }

    @Test
    void delete_hasResidents_throwsInvalidStatus() {
        Apartment apartment = Apartment.builder().id(10L).number("1")
                .residents(List.of(User.builder().id(1L).role(Role.RESIDENT).build())).build();
        when(apartmentRepository.findById(10L)).thenReturn(Optional.of(apartment));

        assertThatThrownBy(() -> apartmentService.delete(10L)).isInstanceOf(InvalidStatusException.class);
        verify(apartmentRepository, org.mockito.Mockito.never()).delete(apartment);
    }

    @Test
    void delete_hasGatePasses_throwsInvalidStatus() {
        Apartment apartment = Apartment.builder().id(10L).number("1").residents(new ArrayList<>()).build();
        when(apartmentRepository.findById(10L)).thenReturn(Optional.of(apartment));
        when(gatePassRepository.existsByApartmentId(10L)).thenReturn(true);

        assertThatThrownBy(() -> apartmentService.delete(10L)).isInstanceOf(InvalidStatusException.class);
    }

    @Test
    void delete_noResidentsOrPasses_deletesApartment() {
        Apartment apartment = Apartment.builder().id(10L).number("1").residents(new ArrayList<>()).build();
        when(apartmentRepository.findById(10L)).thenReturn(Optional.of(apartment));
        when(gatePassRepository.existsByApartmentId(10L)).thenReturn(false);

        apartmentService.delete(10L);

        verify(apartmentRepository).delete(apartment);
    }

    @Test
    void create_setsBuildingFromBuildingService() {
        Building building = Building.builder().id(1L).name("Zgrada A").build();
        when(buildingService.findEntity(1L)).thenReturn(building);
        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(inv -> inv.getArgument(0));

        ApartmentResponse response = apartmentService.create(new ApartmentRequest("101", 1, 1L));

        assertThat(response.number()).isEqualTo("101");
        assertThat(response.buildingId()).isEqualTo(1L);
    }
}
