package com.commerceops.erp.domain.notification.repository;

import com.commerceops.erp.domain.notification.entity.Notification;
import com.commerceops.erp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Notification> findByReadAtIsNull(Pageable pageable);

    long countByUserAndReadAtIsNull(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.readAt IS NULL")
    int markAllReadByUser(@Param("user") User user);
}
