package com.commerceops.erp.domain.hr.entity;

import com.commerceops.erp.domain.hr.enums.EmploymentStatus;
import com.commerceops.erp.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_profiles")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class StaffProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(name = "employee_no", length = 50, unique = true)
    private String employeeNo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "joined_at")
    private LocalDate joinedAt;

    @Column(name = "left_at")
    private LocalDate leftAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public static StaffProfile create(User user, Department department, Position position,
                                      String employeeNo, EmploymentStatus employmentStatus,
                                      LocalDate joinedAt, Boolean active) {
        return StaffProfile.builder()
                .user(user)
                .department(department)
                .position(position)
                .employeeNo(normalizeBlank(employeeNo))
                .employmentStatus(employmentStatus != null ? employmentStatus : EmploymentStatus.ACTIVE)
                .joinedAt(joinedAt)
                .active(active == null || active)
                .build();
    }

    public void update(Department department, Position position, String employeeNo,
                       EmploymentStatus employmentStatus, LocalDate joinedAt,
                       LocalDate leftAt, Boolean active) {
        this.department = department;
        this.position = position;
        this.employeeNo = normalizeBlank(employeeNo);
        if (employmentStatus != null) {
            this.employmentStatus = employmentStatus;
        }
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        if (active != null) {
            this.active = active;
        }
        applyResignedLeftAt();
    }

    public void changeEmploymentStatus(EmploymentStatus employmentStatus, LocalDate leftAt) {
        this.employmentStatus = employmentStatus;
        this.leftAt = leftAt;
        applyResignedLeftAt();
    }

    public void changeActive(Boolean active) {
        this.active = active;
    }

    private void applyResignedLeftAt() {
        if (employmentStatus == EmploymentStatus.RESIGNED && leftAt == null) {
            leftAt = LocalDate.now();
        }
        if (employmentStatus != EmploymentStatus.RESIGNED) {
            leftAt = null;
        }
    }

    private static String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
