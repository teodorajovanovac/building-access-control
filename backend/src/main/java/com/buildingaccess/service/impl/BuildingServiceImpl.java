package com.buildingaccess.service.impl;

import com.buildingaccess.dto.building.BuildingRequest;
import com.buildingaccess.dto.building.BuildingResponse;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.mapper.BuildingMapper;
import com.buildingaccess.model.Apartment;
import com.buildingaccess.model.Building;
import com.buildingaccess.model.enums.Role;
import com.buildingaccess.repository.AccessDenialRepository;
import com.buildingaccess.repository.ApartmentRepository;
import com.buildingaccess.repository.BuildingRepository;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.repository.GatePassRepository;
import com.buildingaccess.repository.UserRepository;
import com.buildingaccess.service.BuildingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final GatePassRepository gatePassRepository;
    private final EntryLogRepository entryLogRepository;
    private final AccessDenialRepository accessDenialRepository;

    public BuildingServiceImpl(BuildingRepository buildingRepository,
                                ApartmentRepository apartmentRepository,
                                UserRepository userRepository,
                                GatePassRepository gatePassRepository,
                                EntryLogRepository entryLogRepository,
                                AccessDenialRepository accessDenialRepository) {
        this.buildingRepository = buildingRepository;
        this.apartmentRepository = apartmentRepository;
        this.userRepository = userRepository;
        this.gatePassRepository = gatePassRepository;
        this.entryLogRepository = entryLogRepository;
        this.accessDenialRepository = accessDenialRepository;
    }

    @Override
    public List<BuildingResponse> getAll() {
        return buildingRepository.findAll().stream().map(BuildingMapper::toResponse).toList();
    }

    @Override
    public BuildingResponse getById(Long id) {
        return BuildingMapper.toResponse(findEntity(id));
    }

    @Override
    public Building findEntity(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zgrada nije pronađena: " + id));
    }

    @Override
    @Transactional
    public BuildingResponse create(BuildingRequest request) {
        Building building = new Building();
        building.setName(request.name());
        building.setAddress(request.address());
        return BuildingMapper.toResponse(buildingRepository.save(building));
    }

    @Override
    @Transactional
    public BuildingResponse update(Long id, BuildingRequest request) {
        Building building = findEntity(id);
        building.setName(request.name());
        building.setAddress(request.address());
        return BuildingMapper.toResponse(building);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Building building = findEntity(id);

        boolean hasResidentsOrPasses = building.getApartments().stream().anyMatch(this::apartmentHasActiveData);
        if (hasResidentsOrPasses) {
            throw new InvalidStatusException(
                    "Zgrada ima stanove sa registrovanim stanarima ili propusnicama — obrišite ih prvo.");
        }
        if (userRepository.existsByRoleAndBuildingId(Role.STAFF, id)) {
            throw new InvalidStatusException("Zgrada ima osoblje — uklonite ga prvo.");
        }
        if (userRepository.existsByRoleAndBuildingId(Role.SECURITY, id)) {
            throw new InvalidStatusException("Zgrada ima dodeljeno obezbeđenje — uklonite ga prvo.");
        }
        if (entryLogRepository.existsByBuildingId(id) || accessDenialRepository.existsByBuildingId(id)) {
            throw new InvalidStatusException("Zgrada ima evidenciju ulazaka ili odbijenih pokušaja — ne može se obrisati.");
        }

        // Stan ne postoji bez zgrade (apartments.building_id je NOT NULL) — pošto veza više nije
        // kaskadna na nivou JPA (v. konceptualni model: obična asocijacija, ne kompozicija),
        // prazne stanove eksplicitno brišemo ovde pre brisanja zgrade.
        apartmentRepository.deleteAll(building.getApartments());
        buildingRepository.delete(building);
    }

    private boolean apartmentHasActiveData(Apartment apartment) {
        return !apartment.getResidents().isEmpty() || gatePassRepository.existsByApartmentId(apartment.getId());
    }
}
