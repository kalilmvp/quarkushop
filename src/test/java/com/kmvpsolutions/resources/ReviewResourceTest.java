package com.kmvpsolutions.resources;

import com.kmvpsolutions.utils.KeyCloakRealmResource;
import com.kmvpsolutions.utils.TestContainerResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisabledOnNativeImage
@QuarkusTest
@QuarkusTestResource(TestContainerResource.class)
@QuarkusTestResource(KeyCloakRealmResource.class)
public class ReviewResourceTest {

    private static String ADMIN_BEARER_TOKEN;
    private static String TEST_BEARER_TOKEN;

    @BeforeAll
    static void init() {
        ADMIN_BEARER_TOKEN = System.getProperty("quarkus-admin-access-token");
        TEST_BEARER_TOKEN = System.getProperty("quarkus-test-access-token");
    }

    @Test
    void testFindAllByProduct() {
        given()
                .when()
                .get("/reviews/product/1")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(2))
                .body(containsString("id"))
                .body(containsString("title"))
                .body(containsString("rating"))
                .body(containsString("description"));
    }

    @Test
    void testFindById() {
        given()
                .when()
                .get("/reviews/2")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("id"))
                .body(containsString("title"))
                .body(containsString("rating"))
                .body(containsString("description"));
    }

    @Test
    void testCreate() {
        var requestParams = new HashMap<>();
        requestParams.put("description", "Wonderful laptop !");
        requestParams.put("rating", 5);
        requestParams.put("title", "Must have for every developer");

        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .body(requestParams)
                .post("/reviews/product/3")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testDelete() {
        given()
                .when()
                .delete("/reviews/3")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testReviewsDeletedWhenProductIsDeleted() {
        given()
                .when()
                .delete("/products/1")
                .then()
                .statusCode(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void testFindAllByProductWithAdminRole() {
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/reviews/product/1")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(2))
                .body(containsString("id"))
                .body(containsString("title"))
                .body(containsString("rating"))
                .body(containsString("description"));
    }

    @Test
    void testFindByIdWithAdminRole() {
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/reviews/2")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("id"))
                .body(containsString("title"))
                .body(containsString("rating"))
                .body(containsString("description"));
    }

    @Test
    void testCreateWithAdminRole() {
        Integer count = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/reviews/product/3")
                .then()
                .extract()
                .body()
                .path("size()");

        var requestParams = new HashMap<>();
        requestParams.put("description", "Wonderful laptop !");
        requestParams.put("rating", 5);
        requestParams.put("title", "Must have for every developer");

        var reviewId = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(requestParams)
                .post("/reviews/product/3")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("id"))
                .body(containsString("Wonderful laptop !"))
                .extract()
                .jsonPath()
                .get("id");

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/reviews/product/3")
                .then()
                .body("size()", is(count + 1));

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/reviews/" + reviewId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteWithAdminRole() {
        Integer count = given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/reviews/product/2")
                .then()
                .extract()
                .body()
                .path("size()");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/reviews/3")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/reviews/product/2")
                .then()
                .body("size()", is(count - 1));
    }

    @Test
    void testReviewsDeletedWhenProductIsDeletedWithAdminRole() {
        var requestParams = new HashMap<>();
        requestParams.put("name", "Dell G5");
        requestParams.put("description", "Best gaming laptop from Dell");
        requestParams.put("price", 1490);
        requestParams.put("status", "AVAILABLE");
        requestParams.put("salesCounter", 0);
        requestParams.put("categoryId", 2);

        var response = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(requestParams)
                .post("/products")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getMap("$");

        assertNotNull(response.get("id"));

        var newProductId = response.get("id");

        requestParams = new HashMap<>();
        requestParams.put("description", "Wonderful laptop !");
        requestParams.put("rating", 5);
        requestParams.put("title", "Must have for every developer");

        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(requestParams)
                .post("/reviews/product/" + newProductId)
                .then()
                .statusCode(OK.getStatusCode());

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/products/" + newProductId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .get("/reviews/product/" + newProductId)
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(0));
    }

    @Test
    void testFindAllByProductWithUserRole() {
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/reviews/product/1")
                .then()
                .statusCode(OK.getStatusCode())
                .body("size()", is(2))
                .body(containsString("id"))
                .body(containsString("title"))
                .body(containsString("rating"))
                .body(containsString("description"));
    }

    @Test
    void testFindByIdWithUserRole() {
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/reviews/2")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("id"))
                .body(containsString("title"))
                .body(containsString("rating"))
                .body(containsString("description"));
    }

    @Test
    void testCreateWithUserRole() {
        Integer count = given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/reviews/product/3")
                .then()
                .extract()
                .body()
                .path("size()");

        var requestParams = new HashMap<>();
        requestParams.put("description", "Wonderful laptop !");
        requestParams.put("rating", 5);
        requestParams.put("title", "Must have for every developer");

        Long reviewId = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .body(requestParams)
                .post("/reviews/product/3")
                .then()
                .statusCode(OK.getStatusCode())
                .body(containsString("id"))
                .body(containsString("Wonderful laptop !"))
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .get("/reviews/product/3")
                .then()
                .body("size()", is(count + 1));

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/reviews/" + reviewId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testDeleteWithUserRole() {
        var requestParams = new HashMap<>();
        requestParams.put("description", "Wonderful laptop !");
        requestParams.put("rating", 5);
        requestParams.put("title", "Must have for every developer");

        Long reviewId = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .body(requestParams)
                .post("/reviews/product/3")
                .then()
                .statusCode(OK.getStatusCode())
                .extract()
                .jsonPath()
                .getLong("id");

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .delete("/reviews/" + reviewId)
                .then()
                .statusCode(FORBIDDEN.getStatusCode());

        given().when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ADMIN_BEARER_TOKEN)
                .delete("/reviews/" + reviewId)
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void testReviewsDeletedWhenProductIsDeletedWithUserRole() {
        var requestParams = new HashMap<>();
        requestParams.put("name", "Dell G5");
        requestParams.put("description", "Best gaming laptop from Dell");
        requestParams.put("price", 1490);
        requestParams.put("status", "AVAILABLE");
        requestParams.put("salesCounter", 0);
        requestParams.put("categoryId", 2);

        given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_BEARER_TOKEN)
                .body(requestParams)
                .post("/products")
                .then()
                .statusCode(FORBIDDEN.getStatusCode());
    }
}