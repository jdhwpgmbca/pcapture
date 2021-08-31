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
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.*;

/**
 *
 * @author jdh
 */
@Path( "/api/filter" )
public class PacketFilterResource
{
    @Inject
    CaptureTypeService captureTypeService;

    @POST
    @RolesAllowed( "filter_admin" )
    public void addFilter( @NotNull @Valid CaptureType type )
    {
        captureTypeService.createOrUpdateCaptureType( type );
    }

    @DELETE
    @Path( "/{url_suffix}" )
    @RolesAllowed( "filter_admin" )
    public void deleteFilter( @NotBlank @Size(min=1,max=10) @PathParam("url_suffix") String url_suffix )
    {
        captureTypeService.deleteCaptureType( url_suffix );
    }

    @GET
    @RolesAllowed( { "user", "admin", "filter_admin" } )
    public List<CaptureType> getFilters()
    {
        return captureTypeService.list();
    }

}
