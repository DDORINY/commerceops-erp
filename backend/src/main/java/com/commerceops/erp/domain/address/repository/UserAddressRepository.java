package com.commerceops.erp.domain.address.repository;

import com.commerceops.erp.domain.address.entity.UserAddress;
import com.commerceops.erp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
 List<UserAddress> findByUserOrderByIsDefaultDescUpdatedAtDesc(User user);
 Optional<UserAddress> findByIdAndUser(Long id, User user);
 Optional<UserAddress> findFirstByUserAndIsDefaultTrue(User user);
 Optional<UserAddress> findFirstByUserOrderByUpdatedAtDesc(User user);
 long countByUser(User user);
}
