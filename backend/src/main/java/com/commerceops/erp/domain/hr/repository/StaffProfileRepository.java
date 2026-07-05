package com.commerceops.erp.domain.hr.repository;

import com.commerceops.erp.domain.hr.entity.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {

    List<StaffProfile> findAllByOrderByIdAsc();
}
