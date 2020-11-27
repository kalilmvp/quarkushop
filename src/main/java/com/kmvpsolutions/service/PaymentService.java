package com.kmvpsolutions.service;

import com.kmvpsolutions.dao.CartRepository;
import com.kmvpsolutions.dao.OrderRepository;
import com.kmvpsolutions.dao.PaymentRepository;
import com.kmvpsolutions.domain.Cart;
import com.kmvpsolutions.domain.Order;
import com.kmvpsolutions.domain.Payment;
import com.kmvpsolutions.domain.dto.OrderDTO;
import com.kmvpsolutions.domain.dto.OrderItemDTO;
import com.kmvpsolutions.domain.dto.PaymentDTO;
import com.kmvpsolutions.domain.enums.OrderStatus;
import com.kmvpsolutions.domain.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@Transactional
public class PaymentService {

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    OrderRepository orderRepository;

    public List<PaymentDTO> findAll() {
        return this.paymentRepository.findAll()
                .stream()
                .map(payment -> findById(payment.getId()))
                .collect(Collectors.toList());
    }

    public PaymentDTO findById(Long id) {
        log.debug("Request to get payment by id {}", id);

        Order order = this.orderRepository.findByPaymentId(id).orElseThrow(() ->
                new IllegalStateException("The order does not exist"));

        return this.paymentRepository.findById(id)
                .map(payment -> mapToDTO(payment, order.getId())).orElse(null);
    }

    public List<PaymentDTO> findByPriceRange(Double max) {
        return this.paymentRepository.findAllByAmountBetween(
                BigDecimal.ZERO,
                BigDecimal.valueOf(max))
            .stream()
            .map(payment -> mapToDTO(payment,
                    this.findOrderByPaymentId(payment.getId()).getId()))
            .collect(Collectors.toList());
    }

    public PaymentDTO create(PaymentDTO paymentDTO) {
        log.debug("Request to create payment {}", paymentDTO);

        Order order = this.orderRepository.findById(paymentDTO.getOrderId()).orElseThrow(() ->
                new IllegalStateException("The order does not exist"));
        order.setStatus(OrderStatus.PAID);

        Payment paymentSaved = this.paymentRepository.saveAndFlush(new Payment(
                paymentDTO.getPaypalPaymentId(),
                PaymentStatus.valueOf(paymentDTO.getStatus()),
                order.getPrice()
        ));

        this.orderRepository.saveAndFlush(order);

        return mapToDTO(paymentSaved, order.getId());
    }

    public void delete(Long id) {
        log.debug("Request to delete payment {}", id);

        this.paymentRepository.deleteById(id);
    }

    private Order findOrderByPaymentId(Long paymentId) {
        return this.orderRepository.findByPaymentId(paymentId).orElseThrow(() ->
                new IllegalStateException("No order exists for payment id " + paymentId));
    }

    private static PaymentDTO mapToDTO(Payment payment, Long orderId) {
        if (payment != null) {
            return new PaymentDTO(
                    payment.getId(),
                    payment.getPaypalPaymentId(),
                    payment.getStatus().name(),
                    orderId
            );
        }
        return null;
    }
}
