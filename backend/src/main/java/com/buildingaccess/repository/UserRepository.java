package com.buildingaccess.repository;

import com.buildingaccess.model.User;
import com.buildingaccess.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByBadgeCode(String badgeCode);

    Optional<User> findByBadgeCode(String badgeCode);

    Optional<User> findByBadgeCodeAndRole(String badgeCode, Role role);

    List<User> findByRole(Role role);

    boolean existsByRole(Role role);

    boolean existsByRoleAndBuildingId(Role role, Long buildingId);

    List<User> findByRoleAndBuildingId(Role role, Long buildingId);

    List<User> findByApartmentId(Long apartmentId);

    @Query("""
            select u from User u
            where u.role = com.buildingaccess.model.enums.Role.RESIDENT
              and u.apartment.building.id = :buildingId
              and (
                    lower(concat(u.firstName, ' ', u.lastName)) like lower(concat('%', :query, '%'))
                    or lower(u.apartment.number) like lower(concat('%', :query, '%'))
                  )
            order by u.lastName, u.firstName
            """)
    List<User> searchResidentsInBuilding(@Param("buildingId") Long buildingId, @Param("query") String query);

    /** SK10 — osoblje (STAFF) ima building popunjen direktno (za razliku od RESIDENT preko apartment.building). */
    @Query("""
            select u from User u
            where u.role = com.buildingaccess.model.enums.Role.STAFF
              and u.building.id = :buildingId
              and lower(concat(u.firstName, ' ', u.lastName)) like lower(concat('%', :query, '%'))
            order by u.lastName, u.firstName
            """)
    List<User> searchStaffInBuilding(@Param("buildingId") Long buildingId, @Param("query") String query);
}
