package com.buildingaccess.service;

import com.buildingaccess.dto.staff.StaffBadgeRequest;
import com.buildingaccess.dto.staff.StaffBadgeResponse;
import com.buildingaccess.dto.staff.StaffBadgeUpdateRequest;
import com.buildingaccess.exception.InvalidStatusException;
import com.buildingaccess.exception.ResourceNotFoundException;
import com.buildingaccess.mapper.StaffBadgeMapper;
import com.buildingaccess.model.Building;
import com.buildingaccess.model.StaffBadge;
import com.buildingaccess.repository.EntryLogRepository;
import com.buildingaccess.repository.StaffBadgeRepository;
import com.buildingaccess.util.CodeGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffBadgeService {

    private final StaffBadgeRepository staffBadgeRepository;
    private final EntryLogRepository entryLogRepository;
    private final BuildingService buildingService;

    public List<StaffBadgeResponse> getByBuilding(Long buildingId) {
        return staffBadgeRepository.findByBuildingId(buildingId).stream().map(StaffBadgeMapper::toResponse).toList();
    }

    public StaffBadgeResponse getById(Long id) {
        return StaffBadgeMapper.toResponse(findEntity(id));
    }

    StaffBadge findEntity(Long id) {
        return staffBadgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Osoblje nije pronađeno: " + id));
    }

    @Transactional
    public StaffBadgeResponse create(StaffBadgeRequest request) {
        Building building = buildingService.findEntity(request.buildingId());
        StaffBadge staffBadge = StaffBadge.builder()
                .fullName(request.fullName())
                .jobTitle(request.jobTitle())
                .badgeCode(generateUniqueBadgeCode())
                .active(true)
                .building(building)
                .build();
        return StaffBadgeMapper.toResponse(staffBadgeRepository.save(staffBadge));
    }

    @Transactional
    public StaffBadgeResponse update(Long id, StaffBadgeUpdateRequest request) {
        StaffBadge staffBadge = findEntity(id);
        staffBadge.setFullName(request.fullName());
        staffBadge.setJobTitle(request.jobTitle());
        staffBadge.setActive(request.active());
        return StaffBadgeMapper.toResponse(staffBadge);
    }

    @Transactional
    public void delete(Long id) {
        if (entryLogRepository.existsByStaffBadgeId(id)) {
            throw new InvalidStatusException("Osoblje ima evidenciju ulazaka — deaktivirajte umesto brisanja.");
        }
        staffBadgeRepository.delete(findEntity(id));
    }

    private String generateUniqueBadgeCode() {
        String code;
        do {
            code = CodeGeneratorUtil.generate("STF", 8);
        } while (staffBadgeRepository.existsByBadgeCode(code));
        return code;
    }
}
