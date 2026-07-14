package com.commerceops.erp.domain.order.repository;
import com.commerceops.erp.domain.order.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface OrderAddressRepository extends JpaRepository<OrderAddress,Long> { Optional<OrderAddress> findByOrder(Order order); }
