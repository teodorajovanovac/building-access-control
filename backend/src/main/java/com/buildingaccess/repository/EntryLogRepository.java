package com.buildingaccess.repository;

import com.buildingaccess.model.EntryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntryLogRepository extends JpaRepository<EntryLog, Long>, JpaSpecificationExecutor<EntryLog> {

    Optional<EntryLog> findFirstByUserIdOrderByEntryTimeDesc(Long userId);

    List<EntryLog> findByBuildingIdAndExitTimeIsNullOrderByEntryTimeAsc(Long buildingId);

    List<EntryLog> findByBuildingIdAndEntryTimeBetweenOrderByEntryTimeDesc(
            Long buildingId, LocalDateTime from, LocalDateTime to);

    @Query("""
            select e from EntryLog e
            where e.user.apartment.id = :apartmentId
               or e.gatePass.apartment.id = :apartmentId
            order by e.entryTime desc
            """)
    List<EntryLog> findAllForApartment(@Param("apartmentId") Long apartmentId);

    long countByBuildingIdAndEntryTimeBetween(Long buildingId, LocalDateTime from, LocalDateTime to);

    boolean existsByBuildingId(Long buildingId);

    long countByBuildingId(Long buildingId);

    boolean existsByUserId(Long userId);

    boolean existsByProcessedById(Long userId);

    long countByBuildingIdAndExitTimeIsNull(Long buildingId);
}
