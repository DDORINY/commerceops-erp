package com.commerceops.erp.domain.hr.repository;

import com.commerceops.erp.domain.hr.entity.StaffProfile;
import com.commerceops.erp.domain.hr.enums.EmploymentStatus;
import com.commerceops.erp.domain.user.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {

    @EntityGraph(attributePaths = {"user", "department", "position"})
    List<StaffProfile> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"user", "department", "position"})
    Optional<StaffProfile> findById(Long id);

    boolean existsByEmployeeNo(String employeeNo);

    boolean existsByEmployeeNoAndIdNot(String employeeNo, Long id);

    @EntityGraph(attributePaths = {"user", "department", "position"})
    @Query(
            value = "SELECT s FROM StaffProfile s " +
                    "JOIN s.user u " +
                    "LEFT JOIN s.department d " +
                    "LEFT JOIN s.position p " +
                    "WHERE (:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword% OR s.employeeNo LIKE %:keyword%) " +
                    "AND (:departmentId IS NULL OR d.id = :departmentId) " +
                    "AND (:positionId IS NULL OR p.id = :positionId) " +
                    "AND (:employmentStatus IS NULL OR s.employmentStatus = :employmentStatus) " +
                    "AND (:active IS NULL OR s.active = :active) " +
                    "AND (:role IS NULL OR u.role = :role)",
            countQuery = "SELECT COUNT(s) FROM StaffProfile s " +
                    "JOIN s.user u " +
                    "LEFT JOIN s.department d " +
                    "LEFT JOIN s.position p " +
                    "WHERE (:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword% OR s.employeeNo LIKE %:keyword%) " +
                    "AND (:departmentId IS NULL OR d.id = :departmentId) " +
                    "AND (:positionId IS NULL OR p.id = :positionId) " +
                    "AND (:employmentStatus IS NULL OR s.employmentStatus = :employmentStatus) " +
                    "AND (:active IS NULL OR s.active = :active) " +
                    "AND (:role IS NULL OR u.role = :role)"
    )
    Page<StaffProfile> findAllForAdmin(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("positionId") Long positionId,
            @Param("employmentStatus") EmploymentStatus employmentStatus,
            @Param("active") Boolean active,
            @Param("role") UserRole role,
            Pageable pageable
    );
}
