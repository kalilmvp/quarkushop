package com.kmvpsolutions.resources;

import com.kmvpsolutions.domain.enums.CartStatus;
import com.kmvpsolutions.utils.TestContainerResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.sql.SQLException;
import java.util.HashMap;

import static io.restassured.RestAssured.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.*;

@DisabledOnNativeImage
@QuarkusTest
@QuarkusTestResource(TestContainerResource.class)
public class CartResourceTest {

    private static final String INSERT_WRONG_CART_IN_DB =
            "insert into carts values (999, current_timestamp, current_timestamp, 'NEW', 3)";
    private static final String DELETE_WRONG_CART_IN_DB =
            "delete from carts where id = 999";
    @Inject
    DataSource datasource;

    @Test
    void testFindAll() {
        get("/carts")
                .then()
                    .statusCode(OK.getStatusCode())
                    .body("size()", greaterThan(0));
    }

    @Test
    void testFindAllActiveCarts() {
        get("/carts/active")
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    void testFindAllInactiveCarts() {
        get("/carts/inactive")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void testGetActiveCartForCustomer() {
        get("/carts/customer/3")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Peter"));
    }

    @Test
    void testFindById() {
        get("/carts/3")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("status"))
                .body(containsString("NEW"));

        get("/carts/100")
                .then()
                .statusCode(NO_CONTENT.getStatusCode())
                .body(emptyOrNullString());
    }

    @Test
    void testDelete() {
        get("/carts/active")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Jason"))
                .body(containsString("NEW"));

        delete("/carts/1")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        get("/carts/1")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("Jason"))
                .body(containsString("CANCELED"));
    }

    @Test
    void testGetActiveCartForCustomerWhenThereAreTwoCartsInDB() {
        this.executeSQL(INSERT_WRONG_CART_IN_DB);

        get("/carts/customer/3")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .body(containsString(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                .body(containsString("Many active carts detected!!"));

        this.executeSQL(DELETE_WRONG_CART_IN_DB);
    }

    @Test
    void testCreateCart() {
        var requestParams = new HashMap<>();
        requestParams.put("firstName", "Kalil");
        requestParams.put("lastName", "Peixoto");
        requestParams.put("email", "kalilmvp@gmail.com");

        // create a new customer to create the cart
        var customerId =
                given()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .body(requestParams)
                    .post("/customers")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .jsonPath()
                        .getInt("id");

        // create the cart for the customer
        var response = post("/carts/customer/" + customerId)
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getMap("$");

        assertThat(response.get("id")).isNotNull();
        assertThat(response).containsEntry("status", CartStatus.NEW.name());

        delete("/carts/" + response.get("id")).then().statusCode(NO_CONTENT.getStatusCode());
        delete("/customers/" + customerId).then().statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testFailCreateCartWhileHavingAlreadyAnActiveCart() {
        var requestParams = new HashMap<>();
        requestParams.put("firstName", "Kalil");
        requestParams.put("lastName", "Peixoto");
        requestParams.put("email", "kalilmvp@gmail.com");

        // create a new customer to create the cart
        var customerId =
                given()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .body(requestParams)
                        .post("/customers")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .jsonPath()
                        .getInt("id");

        // create the cart for the customer
        var cartId = post("/carts/customer/" + customerId)
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getLong("id");

        post("/carts/customer/" + customerId)
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .body(containsString(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                .body(containsString("There is already an active cart"));



        assertThat(cartId).isNotNull();

        delete("/carts/" + cartId).then().statusCode(NO_CONTENT.getStatusCode());
        delete("/customers/" + customerId).then().statusCode(NO_CONTENT.getStatusCode());

    }

    private void executeSQL(String query) {
        try (var connection = this.datasource.getConnection()) {
            connection.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
