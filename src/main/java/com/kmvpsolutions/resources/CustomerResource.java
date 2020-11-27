package com.kmvpsolutions.resources;

import com.kmvpsolutions.domain.dto.CategoryDTO;
import com.kmvpsolutions.domain.dto.CustomerDTO;
import com.kmvpsolutions.domain.dto.ProductDTO;
import com.kmvpsolutions.service.CategoryService;
import com.kmvpsolutions.service.CustomerService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@ApplicationScoped
@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Customer", description = "All customer methods")
public class CustomerResource {

    @Inject
    CustomerService customerService;

    @GET
    public List<CustomerDTO> findAll() {
        return this.customerService.findAll();
    }

    @GET
    @Path("/{id}")
    public CustomerDTO findById(@PathParam("id") Long id) {
        return this.customerService.findById(id);
    }

    @GET
    @Path("/active")
    public List<CustomerDTO> findAllActive() {
        return this.customerService.findAllActive();
    }

    @GET
    @Path("/inactive")
    public List<CustomerDTO> findAllInactive() {
        return this.customerService.findAllInactive();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CustomerDTO create(CustomerDTO customerDTO) {
        return this.customerService.create(customerDTO);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Long id) {
        this.customerService.delete(id);
    }
}
