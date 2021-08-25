/*
 *   Copyright (c) 2021, RTDS Technologies Inc.
 *
 *   Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *   LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *   GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 *   DAMAGE.
 */
package com.rtds;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder( MethodOrderer.OrderAnnotation.class )
public class PacketCaptureResourceTest
{
    private static String id;

    @Test
    @Order( 1 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testStartEndpoint()
    {
        id = given()
                .when().post( "/api/capture/all" )
                .then()
                .statusCode( 200 )
                .body( containsString( "-" ) )
                .extract().asString();

        assertNotNull( id );
    }

    @Test
    @Order( 2 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testStopEndpoint()
    {
        given()
                .pathParam( "id", id )
                .when().put( "/api/capture/{id}" )
                .then()
                .statusCode( 200 );
    }

    @Test
    @Order( 3 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testListEndpoint()
    {
        given()
                .when().get( "/api/capture" )
                .then()
                .statusCode( 200 );
    }

    @Test
    @Order( 4 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testReadEndpoint()
    {
        given()
                .pathParam( "id", id )
                .when().get( "/api/capture/{id}" )
                .then()
                .header( "Content-Disposition", is( "attachment;filename=capture.pcapng" ) )
                .contentType( ContentType.BINARY )
                .statusCode( 200 );
    }

    @Test
    @Order( 5 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testDeleteEndpoint()
    {
        given()
                .pathParam( "id", id )
                .when().delete( "/api/capture/{id}" )
                .then()
                .statusCode( 200 );
    }

    @Test
    @Order( 6 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testStartGooseEndpoint()
    {
        id = given()
                .when().post( "/api/capture/goose" )
                .then()
                .statusCode( 200 )
                .body( containsString( "-" ) )
                .extract().asString();

        assertNotNull( id );
    }

    @Test
    @Order( 7 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testStopEndpoint2()
    {
        given()
                .pathParam( "id", id )
                .when().put( "/api/capture/{id}" )
                .then()
                .statusCode( 200 );
    }

    @Test
    @Order( 8 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testDeleteEndpoint2()
    {
        given()
                .pathParam( "id", id )
                .when().delete( "/api/capture/{id}" )
                .then()
                .statusCode( 200 );
    }

    @Test
    @Order( 9 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testStartGSEEndpoint()
    {
        id = given()
                .when().post( "/api/capture/gse" )
                .then()
                .statusCode( 200 )
                .body( containsString( "-" ) )
                .extract().asString();

        assertNotNull( id );
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testStopEndpoint3()
    {
        given()
                .pathParam( "id", id )
                .when().put( "/api/capture/{id}" )
                .then()
                .statusCode( 200 );
    }

    @Test
    @Order( 11 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testDeleteEndpoint3()
    {
        given()
                .pathParam( "id", id )
                .when().delete( "/api/capture/{id}" )
                .then()
                .statusCode( 200 );
    }

    @Test
    @Order( 12 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testStartSVEndpoint()
    {
        id = given()
                .when().post( "/api/capture/sv" )
                .then()
                .statusCode( 200 )
                .body( containsString( "-" ) )
                .extract().asString();

        assertNotNull( id );
    }

    @Test
    @Order( 13 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testStopEndpoint4()
    {
        given()
                .pathParam( "id", id )
                .when().put( "/api/capture/{id}" )
                .then()
                .statusCode( 200 );
    }

    @Test
    @Order( 14 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testDeleteEndpoint4()
    {
        given()
                .pathParam( "id", id )
                .when().delete( "/api/capture/{id}" )
                .then()
                .statusCode( 200 );
    }

}
