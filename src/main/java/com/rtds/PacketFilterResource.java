/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds;

import com.rtds.jpa.CaptureType;
import com.rtds.svc.CaptureTypeService;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;

/**
 *
 * @author jdh
 */
@Path("/api/filter")
public class PacketFilterResource
{
    @Inject
    CaptureTypeService captureTypeService;
    
    @POST
    @RolesAllowed("admin")
    public void addFilter( CaptureType type )
    {
        captureTypeService.createOrUpdateCaptureType( type );
    }
    
    @DELETE
    @Path("/{label}")
    @RolesAllowed("admin")
    public void deleteFilter( @PathParam("label") String label )
    {
        captureTypeService.deleteCaptureType( label );
    }
    
    @GET
    @RolesAllowed("user")
    public List<CaptureType> getFilters()
    {
        return captureTypeService.list();
    }
    
}
