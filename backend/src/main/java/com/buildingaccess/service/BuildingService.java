package com.buildingaccess.service;

import com.buildingaccess.dto.building.BuildingRequest;
import com.buildingaccess.dto.building.BuildingResponse;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.mapper.BuildingMapper;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.repository.BuildingRepository;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.repository.StaffBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final StaffBadgeRepository staffBadgeRepository;
    private final GatePassRepository gatePassRepository;
    private final EntryLogRepository entryLogRepository;
    private final AccessDenialRepository accessDenialRepository;

    public List<BuildingResponse> getAll() {
        return buildingRepository.findAll().stream().map(BuildingMapper::toResponse).toList();
    }

    public BuildingResponse getById(Long id) {
        return BuildingMapper.toResponse(findEntity(id));
    }

    Building findEntity(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zgrada nije pronađena: " + id));
    }

    @Transactional
    public BuildingResponse create(BuildingRequest request) {
        Building building = Building.builder()
                .name(request.name())
                .address(request.address())
                .build();
        return BuildingMapper.toResponse(buildingRepository.save(building));
    }

    @Transactional
    public BuildingResponse update(Long id, BuildingRequest request) {
        Building building = findEntity(id);
        building.setName(request.name());
        building.setAddress(request.address());
        return BuildingMapper.toResponse(building);
    }

    @Transactional
    public void delete(Long id) {
        Building building = findEntity(id);

        boolean hasResidentsOrPasses = building.getApartments().stream().anyMatch(this::apartmentHasActiveData);
        if (hasResidentsOrPasses) {
            throw new InvalidStatusException(
                    "Zgrada ima stanove sa registrovanim stanarima ili propusnicama — obrišite ih prvo.");
        }
        if (!building.getStaffBadges().isEmpty()) {
            throw new InvalidStatusException("Zgrada ima osoblje — uklonite ga prvo.");
        }
        if (entryLogRepository.existsByBuildingId(id) || accessDenialRepository.existsByBuildingId(id)) {
            throw new InvalidStatusException("Zgrada ima evidenciju ulazaka ili odbijenih pokušaja — ne može se obrisati.");
        }

        buildingRepository.delete(building);
    }

    private boolean apartmentHasActiveData(Apartment apartment) {
        return !apartment.getResidents().isEmpty() || gatePassRepository.existsByApartmentId(apartment.getId());
    }
}
