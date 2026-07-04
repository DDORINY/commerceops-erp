package com.commerceops.erp.domain.audit.entity;

import com.commerceops.erp.domain.audit.enums.AuditActionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actorId;

    @Column(nullable = false, length = 100)
    private String actorEmail;

    @Column(nullable = false, length = 50)
    private String actorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditActionType actionType;

    @Column(nullable = false, length = 50)
    private String targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(length = 50)
    private String beforeStatus;

    @Column(length = 50)
    private String afterStatus;

    @Column(length = 500)
    private String summary;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
