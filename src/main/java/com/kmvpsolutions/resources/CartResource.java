package com.kmvpsolutions.resources;

import com.kmvpsolutions.domain.dto.CartDTO;
import com.kmvpsolutions.service.CartService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@ApplicationScoped
@Path("/carts")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Cart", description = "All cart methods")
public class CartResource {

    @Inject
    CartService cartService;

    @GET
    public List<CartDTO> findAll() {
        return this.cartService.findAll();
    }

    @GET
    @Path("/active")
    public List<CartDTO> findAllActiveCarts() {
        return this.cartService.findAllActiveCarts();
    }

    @GET
    @Path("/customer/{id}")
    public CartDTO getActiveCartFromCustomer(@PathParam("id") Long customerId) {
        return this.cartService.getActiveCart(customerId);
    }

    @GET
    @Path("/{id}")
    public CartDTO findById(@PathParam("id") Long id) {
        return this.cartService.findById(id);
    }

    @POST
    @Path("/customer/{id}")
    public CartDTO create(@PathParam("id") Long customerId) {
        return this.cartService.createDTO(customerId);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.cartService.delete(id);
    }
}
