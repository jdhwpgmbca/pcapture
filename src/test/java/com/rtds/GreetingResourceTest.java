package com.rtds;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import org.hamcrest.Matchers;

@QuarkusTest
public class GreetingResourceTest
{
    @Test
    public void testStartEndpoint()
    {
        String token = given()
          .when().post("/capture/start")
          .then()
             .statusCode(200)
                .body( containsString( ":" ) )
                .extract().asString();
    }

    @Test
    public void testStopEndpoint()
    {
        String token = given()
          .when().post("/capture/start")
          .then()
             .statusCode(200)
                .body( containsString( ":" ) )
                .extract().asString();
        
        given().header( "token", token )
          .when().post("/capture/stop")
          .then()
             .statusCode(200);
    }

//    @Test
//    public void testReadEndpoint()
//    {
//        String token = given()
//          .when().post("/capture/start")
//          .then()
//             .statusCode(200)
//                .body( containsString( ":" ) )
//                .extract().asString();
//        
//        given().header( "token", token )
//          .when().post("/capture/stop")
//          .then()
//             .statusCode(200);
//        
//        given().header( "token", token )
//          .when().post("/capture/read")
//          .then()
//             .statusCode(200);
//    }

}