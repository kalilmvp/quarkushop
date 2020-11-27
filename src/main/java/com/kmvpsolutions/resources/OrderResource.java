package com.kmvpsolutions.resources;

import com.kmvpsolutions.domain.dto.OrderDTO;
import com.kmvpsolutions.service.OrderService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@ApplicationScoped
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Order", description = "All orders methods")
public class OrderResource {

    @Inject
    OrderService orderService;

    @GET
    public List<OrderDTO> findAll() {
        return this.orderService.findAll();
    }

    @GET
    @Path("/customer/{id}")
    public List<OrderDTO> findAllByUser(@PathParam("id") Long id) {
        return this.orderService.findAllByUser(id);
    }

    @GET
    @Path("/{id}")
    public OrderDTO findById(@PathParam("id") Long id) {
        return this.orderService.findById(id);
    }

    @GET
    @Path("/exists/{id}")
    public boolean existsById(@PathParam("id") Long id) {
        return this.orderService.existsById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public OrderDTO create(OrderDTO orderDTO) {
        return this.orderService.create(orderDTO);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.orderService.delete(id);
    }
}
