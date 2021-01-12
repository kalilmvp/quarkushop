package com.kmvpsolutions.order.dao;

import com.kmvpsolutions.domain.enums.CartStatus;
import com.kmvpsolutions.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByStatus(CartStatus status);
    List<Cart> findByStatusAndCustomer(CartStatus status, Long customer);
}
