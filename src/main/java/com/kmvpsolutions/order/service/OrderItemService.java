package com.kmvpsolutions.order.service;

import com.kmvpsolutions.order.dao.OrderItemRepository;
import com.kmvpsolutions.order.dao.OrderRepository;
import com.kmvpsolutions.product.dao.ProductRepository;
import com.kmvpsolutions.domain.OrderItem;
import com.kmvpsolutions.commons.dto.OrderItemDTO;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@ApplicationScoped
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


        var orderItem = this.orderItemRepository.save(
                new OrderItem(
                        orderItemDTO.getQuantity(),
                        orderItemDTO.getProductId(),
                        order
                )
        );

        //TODO later will be replace by a rest call
        var product = this.productRepository.getOne(orderItemDTO.getProductId());

        order.setPrice(order.getPrice().add(product.getPrice()));

        this.orderRepository.save(order);

        return mapToDTO(orderItem);
    }

    public void delete(Long id) {
        log.debug("Request ot delete order item {}", id);

        var orderItem = this.orderItemRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("The order item does not exist")
        );

        var order = orderItem.getOrder();

        //TODO later will be replaced by a rest call
        var product = this.productRepository.getOne(orderItem.getProductId());

        order.setPrice(order.getPrice().subtract(product.getPrice()));

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
                orderItem.getProductId(),
                orderItem.getOrder().getId()
        );
    }
}
