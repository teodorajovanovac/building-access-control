package com.buildingaccess.repository;

import com.buildingaccess.model.GatePass;
import com.buildingaccess.model.enums.GatePassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GatePassRepository extends JpaRepository<GatePass, Long> {

    Optional<GatePass> findByCode(String code);

    Page<GatePass> findByCreatedById(Long userId, Pageable pageable);

    boolean existsByApartmentId(Long apartmentId);

    boolean existsByCreatedById(Long userId);

    long countByApartmentBuildingIdAndCreatedAtBetween(Long buildingId, LocalDateTime from, LocalDateTime to);

    /**
     * SK17 pretraga — svi parametri sem pageable/sort su opcioni (null = "ne filtriraj po ovome"),
     * otud trik ":param IS NULL OR ..." za svaki filter.
     */
    String SEARCH_JPQL = """
            select g from GatePass g
            where (:buildingId is null or g.apartment.building.id = :buildingId)
              and (:apartmentId is null or g.apartment.id = :apartmentId)
              and (:status is null or g.status = :status)
              and (:from is null or g.createdAt >= :from)
              and (:to is null or g.createdAt <= :to)
              and (:text is null or lower(g.guestName) like lower(concat('%', :text, '%'))
                                  or lower(g.code) like lower(concat('%', :text, '%')))
            """;

    @Query(SEARCH_JPQL)
    Page<GatePass> search(@Param("buildingId") Long buildingId, @Param("apartmentId") Long apartmentId,
                           @Param("status") GatePassStatus status, @Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to, @Param("text") String text, Pageable pageable);

    @Query(SEARCH_JPQL)
    List<GatePass> search(@Param("buildingId") Long buildingId, @Param("apartmentId") Long apartmentId,
                           @Param("status") GatePassStatus status, @Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to, @Param("text") String text, Sort sort);
}
