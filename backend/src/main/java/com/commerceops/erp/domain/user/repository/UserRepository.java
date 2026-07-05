package com.commerceops.erp.domain.user.repository;

import com.commerceops.erp.domain.user.entity.User;
import com.commerceops.erp.domain.user.enums.UserRole;
import com.commerceops.erp.domain.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRoleAndStatus(UserRole role, UserStatus status);

    @Query(
        value = "SELECT u FROM User u " +
                "WHERE (:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword%)",
        countQuery = "SELECT COUNT(u) FROM User u " +
                     "WHERE (:keyword IS NULL OR u.name LIKE %:keyword% OR u.email LIKE %:keyword%)"
    )
    Page<User> findAllForAdmin(@Param("keyword") String keyword, Pageable pageable);
}
