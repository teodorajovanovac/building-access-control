package com.buildingaccess.service.impl;

import com.buildingaccess.dto.apartment.ApartmentRequest;
import com.buildingaccess.dto.apartment.ApartmentResponse;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.mapper.ApartmentMapper;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.repository.ApartmentRepository;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.service.ApartmentService;
import com.buildingaccess.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final GatePassRepository gatePassRepository;
    private final BuildingService buildingService;

    /** Puna (nepaginirana) lista — koristi je forma za registraciju stanara (SK2), gde je potreban ceo izbor. */
    @Override
    public List<ApartmentResponse> getByBuilding(Long buildingId) {
        return apartmentRepository.findByBuildingId(buildingId).stream().map(ApartmentMapper::toResponse).toList();
    }

    /** Paginirana lista za admin CRUD prikaz (SK14). */
    @Override
    public Page<ApartmentResponse> search(Long buildingId, Pageable pageable) {
        return apartmentRepository.findByBuildingId(buildingId, pageable).map(ApartmentMapper::toResponse);
    }

    @Override
    public ApartmentResponse getById(Long id) {
        return ApartmentMapper.toResponse(findEntity(id));
    }

    @Override
    public Apartment findEntity(Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stan nije pronađen: " + id));
    }

    @Override
    @Transactional
    public ApartmentResponse create(ApartmentRequest request) {
        Building building = buildingService.findEntity(request.buildingId());
        Apartment apartment = Apartment.builder()
                .number(request.number())
                .floor(request.floor())
                .building(building)
                .build();
        return ApartmentMapper.toResponse(apartmentRepository.save(apartment));
    }

    @Override
    @Transactional
    public ApartmentResponse update(Long id, ApartmentRequest request) {
        Apartment apartment = findEntity(id);
        Building building = buildingService.findEntity(request.buildingId());
        apartment.setNumber(request.number());
        apartment.setFloor(request.floor());
        apartment.setBuilding(building);
        return ApartmentMapper.toResponse(apartment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Apartment apartment = findEntity(id);
        if (!apartment.getResidents().isEmpty() || gatePassRepository.existsByApartmentId(id)) {
            throw new InvalidStatusException(
                    "Stan ima registrovane stanare ili propusnice — ne može se obrisati.");
        }
        apartmentRepository.delete(apartment);
    }
}
