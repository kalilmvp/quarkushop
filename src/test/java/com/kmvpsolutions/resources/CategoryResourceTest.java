package com.kmvpsolutions.resources;

import com.kmvpsolutions.utils.TestContainerResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

import static io.restassured.RestAssured.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(TestContainerResource.class)
public class CategoryResourceTest {

    @Test
    void testFindAll() {
        get("/categories")
                .then()
                    .statusCode(OK.getStatusCode())
                    .body("size()", greaterThan(0));
    }

    @Test
    void testFindById() {
        get("/categories/2")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("description"))
                .body(containsString("Computers and Laptops"));
    }

    @Test
    void testProductsOfSpecificCategory() {
        get("/categories/2/products")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("description"));
    }

    @Test
    void testDoesNotFindProductsOfSpecificCategory() {
        get("/categories/10/products")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", equalTo(0));
    }
    @Test
    void testDeleteFailBecauseThereIsProductAssociated() {
        delete("/categories/1")
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .body(containsString(INTERNAL_SERVER_ERROR.getReasonPhrase()))
                .body(containsString("There is a product with this category associated"));
    }

    @Test
    void testCreate() {
        var requestParams = new HashMap<>();
        requestParams.put("name", "Category test 01");
        requestParams.put("description", "Description for the category 01");
        requestParams.put("email", "kalilmvp@gmail.com");

        // create the new category
        var response =
                given()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .body(requestParams)
                    .post("/categories")
                        .then()
                        .statusCode(OK.getStatusCode())
                        .extract()
                        .jsonPath()
                        .getMap("$");

        assertThat(response.get("id")).isNotNull();
        assertThat(response).containsEntry("name", "Category test 01");
        assertThat(response).containsEntry("description", "Description for the category 01");
        assertThat(response.get("products").equals(0L));
    }
}
