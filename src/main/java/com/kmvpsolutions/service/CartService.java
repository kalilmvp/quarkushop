package com.kmvpsolutions.service;

import com.kmvpsolutions.dao.CartRepository;
import com.kmvpsolutions.dao.CustomerRepository;
import com.kmvpsolutions.domain.Cart;
import com.kmvpsolutions.domain.dto.CartDTO;
import com.kmvpsolutions.domain.enums.CartStatus;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@Transactional
public class CartService {

    @Inject
    CartRepository cartRepository;

    @Inject
    CustomerRepository customerRepository;

    public List<CartDTO> findAll() {
        log.debug("Request to get all carts");

        return this.cartRepository.findAll()
                .stream()
                .map(CartService::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CartDTO> findAllActiveCarts() {
        log.debug("Request to get all active carts");

        return this.cartRepository.findByStatus(CartStatus.NEW)
                .stream()
                .map(CartService::mapToDTO)
                .collect(Collectors.toList());
    }

    private Cart create(Long customerId) {
        if (this.getActiveCart(customerId) == null) {
            var customer = this.customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalStateException("This customer does not exist!!"));

            var cart = new Cart(customer, CartStatus.NEW);

            return this.cartRepository.save(cart);
        } else {
            throw new IllegalStateException("There is already an active cart");
        }
    }

    public CartDTO findById(Long id) {
        log.debug("Request to get cart: {}", id);
        return this.cartRepository.findById(id).map(CartService::mapToDTO).orElse(null);
    }

    public void delete(Long id) {
        log.debug("Request to delete cart: {}", id);
        Cart cart = this.cartRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Cannot find cart with the id " + id));

        cart.setStatus(CartStatus.CANCELED);

        this.cartRepository.save(cart);

    }

    public CartDTO getActiveCart(Long customerId) {
        List<Cart> activeCarts = this.cartRepository.findByStatusAndCustomerId(CartStatus.NEW, customerId);

        if (activeCarts != null) {
            if (activeCarts.size() == 1) {
                return mapToDTO(activeCarts.get(0));
            }

            if (activeCarts.size() > 1) {
                throw new IllegalStateException("Many active carts detected!!");
            }
        }

        return null;
    }

    public CartDTO createDTO(Long customerId) {
        return mapToDTO(this.create(customerId));
    }

    public static CartDTO mapToDTO(Cart cart) {
        return new CartDTO(cart.getId(),
                CustomerService.mapToDTO(cart.getCustomer()),
                cart.getStatus().name());

    }
}
