package com.buildingaccess.repository;

import com.buildingaccess.model.StaffBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffBadgeRepository extends JpaRepository<StaffBadge, Long> {

    Optional<StaffBadge> findByBadgeCodeAndActiveTrue(String badgeCode);

    boolean existsByBadgeCode(String badgeCode);

    List<StaffBadge> findByBuildingId(Long buildingId);

    @Query("""
            select s from StaffBadge s
            where s.building.id = :buildingId
              and s.active = true
              and lower(s.fullName) like lower(concat('%', :query, '%'))
            order by s.fullName
            """)
    List<StaffBadge> searchActiveStaffInBuilding(@Param("buildingId") Long buildingId, @Param("query") String query);
}
