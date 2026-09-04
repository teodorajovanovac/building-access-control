package com.buildingaccess.repository;

import com.buildingaccess.model.AccessDenial;
import com.buildingaccess.model.enums.DenialReasonType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessDenialRepository extends JpaRepository<AccessDenial, Long> {

    List<AccessDenial> findByBuildingIdAndAttemptTimeBetweenOrderByAttemptTimeDesc(
            Long buildingId, LocalDateTime from, LocalDateTime to);

    long countByBuildingIdAndAttemptTimeBetween(Long buildingId, LocalDateTime from, LocalDateTime to);

    boolean existsByBuildingId(Long buildingId);

    boolean existsByProcessedById(Long userId);

    /** SK17 pretraga — svi filteri opcioni (null = "ne filtriraj po ovome"). */
    String SEARCH_JPQL = """
            select d from AccessDenial d
            where (:buildingId is null or d.building.id = :buildingId)
              and (:reasonType is null or d.reasonType = :reasonType)
              and (:from is null or d.attemptTime >= :from)
              and (:to is null or d.attemptTime <= :to)
              and (:text is null or lower(d.personName) like lower(concat('%', :text, '%'))
                                  or lower(d.enteredCode) like lower(concat('%', :text, '%')))
            """;

    @Query(SEARCH_JPQL)
    Page<AccessDenial> search(@Param("buildingId") Long buildingId, @Param("reasonType") DenialReasonType reasonType,
                               @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                               @Param("text") String text, Pageable pageable);

    @Query(SEARCH_JPQL)
    List<AccessDenial> search(@Param("buildingId") Long buildingId, @Param("reasonType") DenialReasonType reasonType,
                               @Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                               @Param("text") String text, Sort sort);
}
