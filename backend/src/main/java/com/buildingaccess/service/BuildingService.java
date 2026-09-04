package com.buildingaccess.service;

import com.buildingaccess.dto.building.BuildingRequest;
import com.buildingaccess.dto.building.BuildingResponse;
import com.buildingaccess.model.Building;

import java.util.List;

public interface BuildingService {

    List<BuildingResponse> getAll();

    BuildingResponse getById(Long id);

    /** Javno jer je pozivaju i drugi servisi (ApartmentService, UserService, StatisticsService). */
    Building findEntity(Long id);

    BuildingResponse create(BuildingRequest request);

    BuildingResponse update(Long id, BuildingRequest request);

    void delete(Long id);
}
