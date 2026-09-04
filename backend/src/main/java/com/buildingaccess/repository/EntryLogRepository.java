package com.buildingaccess.repository;

import com.buildingaccess.model.EntryLog;
import com.buildingaccess.model.enums.PersonType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntryLogRepository extends JpaRepository<EntryLog, Long> {

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

    /** SK17 pretraga — svi filteri opcioni (null = "ne filtriraj po ovome"). */
    String SEARCH_JPQL = """
            select e from EntryLog e
            where (:buildingId is null or e.building.id = :buildingId)
              and (:personType is null or e.personType = :personType)
              and (:from is null or e.entryTime >= :from)
              and (:to is null or e.entryTime <= :to)
              and (:text is null or lower(e.personName) like lower(concat('%', :text, '%')))
            """;

    @Query(SEARCH_JPQL)
    Page<EntryLog> search(@Param("buildingId") Long buildingId, @Param("personType") PersonType personType,
                           @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                           @Param("text") String text, Pageable pageable);

    @Query(SEARCH_JPQL)
    List<EntryLog> search(@Param("buildingId") Long buildingId, @Param("personType") PersonType personType,
                           @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                           @Param("text") String text, Sort sort);
}
