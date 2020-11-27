package com.kmvpsolutions.service;

import com.kmvpsolutions.dao.OrderItemRepository;
import com.kmvpsolutions.dao.OrderRepository;
import com.kmvpsolutions.dao.ProductRepository;
import com.kmvpsolutions.dao.ReviewRepository;
import com.kmvpsolutions.domain.Order;
import com.kmvpsolutions.domain.OrderItem;
import com.kmvpsolutions.domain.Product;
import com.kmvpsolutions.domain.Review;
import com.kmvpsolutions.domain.dto.OrderItemDTO;
import com.kmvpsolutions.domain.dto.ReviewDTO;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
@Transactional
public class OrderItemService {

    @Inject
    OrderItemRepository orderItemRepository;

    @Inject
    OrderRepository orderRepository;

    @Inject
    ProductRepository productRepository;

    public OrderItemDTO findById(Long id) {
        log.debug("Request to get OrderItem : {}", id);
        return this.orderItemRepository.findById(id)
                .map(OrderItemService::mapToDTO).orElse(null);
    }

    public OrderItemDTO create(OrderItemDTO orderItemDTO) {
        log.debug("Request to create an Order Item {}", orderItemDTO);

        var order = this.orderRepository.findById(orderItemDTO.getOrderId()).orElseThrow(() ->
                new IllegalStateException("The order does not exist"));

        var product = this.productRepository.findById(orderItemDTO.getProductId()).orElseThrow(() ->
                new IllegalStateException("The product does not exist"));

        var orderItem = this.orderItemRepository.save(
                new OrderItem(
                        orderItemDTO.getQuantity(),
                        product,
                        order
                )
        );

        order.setPrice(order.getPrice().add(orderItem.getProduct().getPrice()));

        this.orderRepository.save(order);

        return mapToDTO(orderItem);
    }

    public void delete(Long id) {
        log.debug("Request ot delete order item {}", id);

        var orderItem = this.orderItemRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("The order item does not exist")
        );

        var order = orderItem.getOrder();

        order.setPrice(order.getPrice().subtract(orderItem.getProduct().getPrice()));

        this.orderItemRepository.deleteById(id);

        order.getOrderItems().remove(orderItem);

        this.orderRepository.save(order);
    }

    public List<OrderItemDTO> findByOrderId(Long orderId) {
        log.debug("Request to get order items from Order id {}", orderId);
        return this.orderItemRepository.findAllByOrderId(orderId)
                .stream()
                .map(OrderItemService::mapToDTO)
                .collect(Collectors.toList());
    }

    public static OrderItemDTO mapToDTO(OrderItem orderItem) {
        return new OrderItemDTO(
                orderItem.getId(),
                orderItem.getQuantity(),
                orderItem.getProduct().getId(),
                orderItem.getOrder().getId()
        );
    }
}
