package com.rtds;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PacketCaptureResourceTest {

    private static String token;

    @Test
    @Order(1)
    public void testStartEndpoint() {
        token = given()
                .when().post("/capture")
                .then()
                .statusCode(200)
                .body(containsString(":"))
                .extract().asString();
    }

    @Test
    @Order(2)
    public void testStopEndpoint() {
        given().header("token", token)
                .when().put("/capture")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(3)
    public void testReadEndpoint() {
        given().header("token", token)
                .when().get("/capture")
                .then()
                .header("Content-Disposition", is("attachment;filename=capture.pcapng"))
                .contentType(ContentType.BINARY)
                .statusCode(200);
    }

    @Test
    @Order(4)
    public void testDeleteEndpoint() {
        given().header("token", token)
                .when().delete("/capture")
                .then()
                .statusCode(200);
    }

}
