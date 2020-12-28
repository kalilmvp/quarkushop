package com.kmvpsolutions.resources;

import com.kmvpsolutions.domain.dto.CustomerDTO;
import com.kmvpsolutions.domain.dto.OrderItemDTO;
import com.kmvpsolutions.service.CustomerService;
import com.kmvpsolutions.service.OrderItemService;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@ApplicationScoped
@Path("/order-items")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Order Items", description = "All order-items methods")
@Authenticated
public class OrderItemResource {

    @Inject
    OrderItemService orderItemService;

    @GET
    @Path("/order/{id}")
    public List<OrderItemDTO> findAll(@PathParam("id") Long id) {
        return this.orderItemService.findByOrderId(id);
    }

    @GET
    @Path("/{id}")
    public OrderItemDTO findById(@PathParam("id") Long id) {
        return this.orderItemService.findById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public OrderItemDTO create(OrderItemDTO orderItemDTO) {
        return this.orderItemService.create(orderItemDTO);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.orderItemService.delete(id);
    }
}
