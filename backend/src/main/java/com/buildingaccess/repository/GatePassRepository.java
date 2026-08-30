package com.buildingaccess.repository;

import com.buildingaccess.model.GatePass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GatePassRepository extends JpaRepository<GatePass, Long>, JpaSpecificationExecutor<GatePass> {

    Optional<GatePass> findByCode(String code);

    Page<GatePass> findByCreatedById(Long userId, Pageable pageable);

    boolean existsByApartmentId(Long apartmentId);

    boolean existsByCreatedById(Long userId);

    long countByApartmentBuildingIdAndCreatedAtBetween(Long buildingId, LocalDateTime from, LocalDateTime to);
}
