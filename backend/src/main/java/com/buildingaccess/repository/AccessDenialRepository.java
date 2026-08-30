package com.buildingaccess.repository;

import com.buildingaccess.model.AccessDenial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessDenialRepository extends JpaRepository<AccessDenial, Long>, JpaSpecificationExecutor<AccessDenial> {

    List<AccessDenial> findByBuildingIdAndAttemptTimeBetweenOrderByAttemptTimeDesc(
            Long buildingId, LocalDateTime from, LocalDateTime to);

    long countByBuildingIdAndAttemptTimeBetween(Long buildingId, LocalDateTime from, LocalDateTime to);

    boolean existsByBuildingId(Long buildingId);

    boolean existsByProcessedById(Long userId);
}
