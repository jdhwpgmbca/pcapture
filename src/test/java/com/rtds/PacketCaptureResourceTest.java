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
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testStartEndpointAsFilterAdmin()
    {
        given()
                .when().post( "/api/capture/all" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 2 )
    @TestSecurity( user = "alice", roles = "admin" )
    public void testStartAllEndpointAsAdmin()
    {
        id = given()
                .when().post( "/api/capture/all" )
                .then()
                .statusCode( 200 ) // OK
                .body( containsString( "-" ) )
                .extract().asString();

        assertNotNull( id );
    }

    @Test
    @Order( 3 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testStopAllEndpointAsFilterAdmin()
    {
        given()
                .pathParam( "id", id )
                .when().put( "/api/capture/{id}" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 4 )
    @TestSecurity( user = "alice", roles = "admin" )
    public void testStopAllEndpointAsAdmin()
    {
        given()
                .pathParam( "id", id )
                .when().put( "/api/capture/{id}" )
                .then()
                .statusCode( 200 ); // OK
    }

    @Test
    @Order( 5 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testListEndpointAsFilterAdmin()
    {
        given()
                .when().get( "/api/capture" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 6 )
    @TestSecurity( user = "alice", roles = "admin" )
    public void testListEndpointAsAdmin()
    {
        given()
                .when().get( "/api/capture" )
                .then()
                .statusCode( 200 ); // OK
    }

    @Test
    @Order( 6 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testListEndpointAsUser()
    {
        given()
                .when().get( "/api/capture" )
                .then()
                .statusCode( 200 ); // OK
    }

    @Test
    @Order( 6 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testReadEndpointAsFilterAdmin()
    {
        given()
                .pathParam( "id", id )
                .when().get( "/api/capture/{id}" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 6 )
    @TestSecurity( user = "alice", roles = "admin" )
    public void testReadEndpointAsAdmin()
    {
        given()
                .pathParam( "id", id )
                .when().get( "/api/capture/{id}" )
                .then()
                .header( "Content-Disposition", is( "attachment;filename=capture.pcapng" ) )
                .contentType( ContentType.BINARY )
                .statusCode( 200 ); // OK
    }

    @Test
    @Order( 6 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testReadEndpointAsUser()
    {
        given()
                .pathParam( "id", id )
                .when().get( "/api/capture/{id}" )
                .then()
                .header( "Content-Disposition", is( "attachment;filename=capture.pcapng" ) )
                .contentType( ContentType.BINARY )
                .statusCode( 200 ); // OK
    }

    @Test
    @Order( 7 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testDeleteAllEndpointAsFilterAdmin()
    {
        given()
                .pathParam( "id", id )
                .when().delete( "/api/capture/{id}" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 8 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testDeleteAllEndpointAsAdmin()
    {
        given()
                .pathParam( "id", id )
                .when().delete( "/api/capture/{id}" )
                .then()
                .statusCode( 200 ); // OK
    }

//    @Test
//    @Order( 9 )
//    @TestSecurity( user = "alice", roles = "user" )
//    public void testStartEndpointAsUser_typeIsBlank()
//    {
//        given()
//                .when().post( "/api/capture/" )
//                .then()
//                .statusCode( 405 ); // METHOD NOT ALLOWED
//    }
//
//    @Test
//    @Order( 9 )
//    @TestSecurity( user = "alice", roles = "user" )
//    public void testStartEndpointAsUser_typeIsTooLong()
//    {
//        given()
//                .when().post( "/api/capture/badrequest1" )
//                .then()
//                .statusCode( 400 ); // BAD REQUEST
//    }

}
