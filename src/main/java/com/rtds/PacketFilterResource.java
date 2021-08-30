/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds;

import com.rtds.event.FilterEvent;
import com.rtds.jpa.CaptureType;
import com.rtds.svc.CaptureTypeService;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.*;

/**
 *
 * @author jdh
 */
@Path( "/api/filter" )
public class PacketFilterResource
{
    @Inject
    Event<FilterEvent> filterEvent;

    @Inject
    CaptureTypeService captureTypeService;

    @POST
    @RolesAllowed( "filter_admin" )
    public void addFilter( CaptureType type )
    {
        if( type.getUrlSuffix().length() > 10 )
        {
            throw new IllegalArgumentException( "url_suffix length must be 10 characters or less." );
        }
        
        captureTypeService.createOrUpdateCaptureType( type );
        filterEvent.fire( new FilterEvent( "Filter Added" ) );
    }

    @DELETE
    @Path( "/{url_suffix}" )
    @RolesAllowed( "filter_admin" )
    public void deleteFilter( @PathParam( "url_suffix" ) String url_suffix )
    {
        if( url_suffix.length() > 10 )
        {
            throw new IllegalArgumentException( "url_suffix length must be 10 characters or less." );
        }
        
        captureTypeService.deleteCaptureType( url_suffix );
        filterEvent.fire( new FilterEvent( "Filter Removed" ) );
    }

    @GET
    @RolesAllowed( { "user", "admin", "filter_admin" } )
    public List<CaptureType> getFilters()
    {
        return captureTypeService.list();
    }

}
