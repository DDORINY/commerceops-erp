package com.commerceops.erp.domain.cart.repository;

import com.commerceops.erp.domain.cart.entity.Cart;
import com.commerceops.erp.domain.product.entity.Product;
import com.commerceops.erp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.user = :user ORDER BY c.createdAt ASC")
    List<Cart> findAllByUserWithProduct(@Param("user") User user);

    Optional<Cart> findByUserAndProduct(User user, Product product);

    @Query("SELECT c FROM Cart c WHERE c.user = :user AND c.product = :product AND " +
           "((:selectedOptions IS NULL AND c.selectedOptions IS NULL) OR c.selectedOptions = :selectedOptions)")
    Optional<Cart> findByUserAndProductAndSelectedOptions(
            @Param("user") User user,
            @Param("product") Product product,
            @Param("selectedOptions") String selectedOptions);

    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.id IN :ids AND c.user = :user")
    List<Cart> findAllByIdInAndUserWithProduct(@Param("ids") List<Long> ids, @Param("user") User user);
}
