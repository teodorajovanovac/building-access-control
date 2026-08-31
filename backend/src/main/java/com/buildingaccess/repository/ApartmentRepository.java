package com.buildingaccess.repository;

import com.buildingaccess.model.Apartment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findByBuildingId(Long buildingId);

    Page<Apartment> findByBuildingId(Long buildingId, Pageable pageable);
}
