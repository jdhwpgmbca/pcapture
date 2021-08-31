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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder( MethodOrderer.OrderAnnotation.class )
public class PacketFilterResourceTest
{
    private static String id;

    @Test
    @Order( 1 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testAddFilterAsUser()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": \"testlabel\", \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 1 )
    @TestSecurity( user = "alice", roles = "admin" )
    public void testAddFilterAsAdmin()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": \"testlabel\", \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 2 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilterAsFilterAdmin()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": \"testlabel\", \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 204 ); // NO CONTENT
    }

    @Test
    @Order( 3 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testDeleteFilterAsUser()
    {
        given()
                .pathParam( "url_suffix", "testsuffix" )
                .when().delete( "/api/filter/{url_suffix}" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 3 )
    @TestSecurity( user = "alice", roles = "admin" )
    public void testDeleteMissingFilterAsAdmin()
    {
        given()
                .pathParam( "url_suffix", "testsuffix2" )
                .when().delete( "/api/filter/{url_suffix}" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 4 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testDeleteMissingFilterAsFilterAdmin()
    {
        given()
                .pathParam( "url_suffix", "testsuffix2" )
                .when().delete( "/api/filter/{url_suffix}" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

    @Test
    @Order( 5 )
    @TestSecurity( user = "alice", roles = "admin" )
    public void testDeleteFilterAsAdmin()
    {
        given()
                .pathParam( "url_suffix", "testsuffix" )
                .when().delete( "/api/filter/{url_suffix}" )
                .then()
                .statusCode( 403 ); // FORBIDDEN
    }

    @Test
    @Order( 6 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testDeleteFilterAsFilterAdmin()
    {
        given()
                .pathParam( "url_suffix", "testsuffix" )
                .when().delete( "/api/filter/{url_suffix}" )
                .then()
                .statusCode( 204 ); // NO CONTENT
    }

    @Test
    @Order( 7 )
    @TestSecurity( user = "alice", roles = "admin" )
    public void testGetFiltersAsAdmin()
    {
        given()
                .when().get( "/api/filter" )
                .then()
                .statusCode( 200 ); // OK
    }

    @Test
    @Order( 8 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testGetFiltersAsFilterAdmin()
    {
        given()
                .when().get( "/api/filter" )
                .then()
                .statusCode( 200 ); // OK
    }

    @Test
    @Order( 9 )
    @TestSecurity( user = "alice", roles = "user" )
    public void testGetFiltersAsUser()
    {
        given()
                .when().get( "/api/filter" )
                .then()
                .statusCode( 200 ); // OK
    }
    
    // The rest of the tests are testing parameter validation
    
    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_urlSuffixBlank()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"\", \"label\": \"testlabel\", \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_urlSuffixNull()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": null, \"label\": \"testlabel\", \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_urlSuffixTooLong()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix1\", \"label\": \"testlabel\", \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_labelBlank()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": \"\", \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_labelNull()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": null, \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_labelTooLong()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": \"testlabel012345678901234567890123456789012345678901\", \"captureFilter\": \"testCaptureFilter\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_captureFilterBlank()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": \"testlabel\", \"captureFilter\": \"\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 204 ); // NO CONTENT
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_captureFilterNull()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": \"testlabel\", \"captureFilter\": null }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 204 ); // NO CONTENT
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testAddFilter_captureFilterTooLong()
    {
        given()
                .contentType( ContentType.JSON )
                .and()
                .body( "{ \"urlSuffix\": \"testsuffix\", \"label\": \"testlabel\", \"captureFilter\": \"test5678901234567890123456789012345678901234567890test5678901234567890123456789012345678901234567890test5678901234567890123456789012345678901234567890test5678901234567890123456789012345678901234567890test5678901234567890123456789012345678901234567890123451\" }" )
                .when().post( "/api/filter" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testDeleteFilterAsFilterAdmin_urlSuffixBlank()
    {
        given()
                .pathParam( "url_suffix", "" )
                .when().delete( "/api/filter/{url_suffix}" )
                .then()
                .statusCode( 405 ); // METHOD NOT ALLOWED (It's blocked by RESTEasy before it gets to validators.)
    }

    @Test
    @Order( 10 )
    @TestSecurity( user = "alice", roles = "filter_admin" )
    public void testDeleteFilterAsFilterAdmin_urlSuffixTooLong()
    {
        given()
                .pathParam( "url_suffix", "testsuffix1" )
                .when().delete( "/api/filter/{url_suffix}" )
                .then()
                .statusCode( 400 ); // BAD REQUEST
    }

}
