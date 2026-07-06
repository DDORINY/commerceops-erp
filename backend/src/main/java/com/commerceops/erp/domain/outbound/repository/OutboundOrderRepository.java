package com.commerceops.erp.domain.outbound.repository;

import com.commerceops.erp.domain.outbound.entity.OutboundOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OutboundOrderRepository extends JpaRepository<OutboundOrder, Long>, JpaSpecificationExecutor<OutboundOrder> {

    boolean existsByOutboundNumber(String outboundNumber);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {"order", "order.user", "warehouse", "items", "items.orderItem", "items.sku", "items.product"})
    Optional<OutboundOrder> findWithItemsById(Long id);

    @Query("SELECT oo FROM OutboundOrder oo WHERE oo.id = :id")
    Optional<OutboundOrder> findForUpdate(@Param("id") Long id);
}
