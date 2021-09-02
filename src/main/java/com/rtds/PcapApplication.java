/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This resource is deliberately public and insecure. There's really no
 * way to secure it for a JavaScript client anyway. And it doesn't contain
 * any secrets anyway.
 */
@Path("/api/res")
public class PcapApplication
{
    final Logger logger = LoggerFactory.getLogger( PcapApplication.class );
    
    @ConfigProperty( name = "auth.server-url" )
    String authServerUrl;

    @ConfigProperty( name = "auth.realm" )
    String authRealm;
    
    @ConfigProperty( name = "auth.frontend.ssl-required" )
    Optional<String> sslRequired;
    
    @ConfigProperty( name = "auth.frontend.client-id" )
    Optional<String> frontendClientResource;
    
    @ConfigProperty( name = "auth.frontend.client-confidential-port" )
    Optional<Integer> clientConfidentialPort;

    @Inject
    ObjectMapper mapper;

    @GET
    @Path("/configjson")
    public String getConfigJson() throws JsonProcessingException
    {
        Map<String,Object> map = new HashMap<>();
        
        map.put( "realm", authRealm );
        map.put( "auth-server-url", authServerUrl + "/auth" );
        map.put( "ssl-required", sslRequired.isPresent() ? sslRequired.get() : "external" );
        map.put( "resource", frontendClientResource.isPresent() ? frontendClientResource.get() : "frontend-client" );
        map.put( "confidential-port", clientConfidentialPort.isPresent() ? clientConfidentialPort.get() : 8443 );
        
        // This last one isn't configurable because it's that's the nature of
        // the JavaScript client.
        
        map.put( "public-client", true );
        
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString( map );
    }
    
}
