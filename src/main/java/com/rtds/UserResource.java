/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds;

import com.rtds.svc.UserPreferenceService;
import io.quarkus.security.identity.SecurityIdentity;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author jdh
 */
@Path("/api/user")
public class UserResource
{
    @Inject
    UserPreferenceService userPreferenceService;
    
    @Inject
    SecurityIdentity identity;
    
    @PUT
    @Path("/toggleAllUsers")
    @RolesAllowed("admin")
    public Response toggleAllUsers()
    {
        Optional<String> principal_name = getPrincipalName();
        
        boolean all_users = userPreferenceService.getBooleanUserPreferenceValue( principal_name.get(), "all_users", false );
        
        userPreferenceService.setBooleanPreferenceValue( principal_name.get(), "all_users", !all_users );
        
        return Response.ok().build();
    }
    
    @GET
    @Path("/isAdmin")
    @RolesAllowed("user")
    public Response isAdmin()
    {
        return Response.ok( identity.hasRole( "admin" ) ).build();
    }
    
    private Optional<String> getPrincipalName()
    {
        Optional<SecurityIdentity> sidentity = Optional.ofNullable( identity );
        Optional<String> principal_name = sidentity.map( sid -> sid.getPrincipal() ).map( p -> p.getName() );
        
        return principal_name;
    }
    
}
