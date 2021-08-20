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

import com.rtds.view.DumpcapProcessDefaultView;
import com.rtds.jpa.DumpcapProcess;
import com.rtds.svc.DumpcapDbService;
import com.rtds.svc.UserPreferenceService;
import io.quarkus.security.identity.SecurityIdentity;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/capture" )
public class PacketCaptureResource
{
    private static final Logger logger = LoggerFactory.getLogger( PacketCaptureResource.class );
    
    @ConfigProperty(name = "start-capture-script")
    String startCaptureScript;
    
    @Inject
    DumpcapDbService dumpcapDbService;
    
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
    
    @POST
    @Path("/all")
    @Produces( MediaType.TEXT_PLAIN )
    @RolesAllowed("user")
    public Response startGenericCapture() throws IOException, GeneralSecurityException
    {
        return startCapture( null, "All" );
    }
    
    @POST
    @Path("/goose")
    @Produces( MediaType.TEXT_PLAIN )
    @RolesAllowed("user")
    public Response startGooseCapture() throws IOException, GeneralSecurityException
    {
        return startCapture( "ether proto 0x99B8", "Goose" );
    }
    
    @POST
    @Path("/gse")
    @Produces( MediaType.TEXT_PLAIN )
    @RolesAllowed("user")
    public Response startGSECapture() throws IOException, GeneralSecurityException
    {
        return startCapture( "ether proto 0x99B9", "GSE" );
    }
    
    @POST
    @Path("/sv")
    @Produces( MediaType.TEXT_PLAIN )
    @RolesAllowed("user")
    public Response startSvCapture() throws IOException, GeneralSecurityException
    {
        return startCapture( "ether proto 0x88BA", "SV" );
    }
    
    public Response startCapture( String filter, String type ) throws IOException, GeneralSecurityException
    {
        java.nio.file.Path path = java.nio.file.Files.createTempFile( "wireshark-capture-", ".pcapng" );
        
        // This now works on Windows and Ubuntu Linux!
        
        ProcessBuilder pb;
        
        if( System.getProperty( "os.name" ).toLowerCase().startsWith( "win" ) )
        {
            if( filter != null )
            {
                // Hack for PowerShell quoting: I needed to surround the filter in single quotes, otherwise the
                // filter would be interpreted as separate arguments! This does not appear to be an issue when
                // running shell scripts on Linux.

                StringBuilder f = new StringBuilder( "'" );

                f.append( filter );
                f.append( "'" );

                pb = new ProcessBuilder( "powershell.exe",  "-ExecutionPolicy", "RemoteSigned", "-Command", startCaptureScript, f.toString(), path.toString() );
            }
            else
            {
                pb = new ProcessBuilder( "powershell.exe",  "-ExecutionPolicy", "RemoteSigned", "-Command", startCaptureScript, path.toString() );
            }
        }
        else
        {
            if( filter != null )
            {
                pb = new ProcessBuilder( startCaptureScript, path.toString(), filter );
            }
            else
            {
                pb = new ProcessBuilder( startCaptureScript, path.toString() );
            }
        }
        
        pb.redirectErrorStream( true );
        
        // Start the process
        
        Process proc = pb.start();
        
        long pid;
        
        // Read the process ID from the output of the script. It will be used
        // later to stop the process.
        
        try( InputStream in = proc.getInputStream() )
        {
            String length_string = new String( in.readAllBytes(), "UTF8" );
            
            pid = Long.parseLong( length_string.trim() );
        }
        
        Optional<SecurityIdentity> sidentity = Optional.ofNullable( identity );
        Optional<String> principal_name = sidentity.map( sid -> sid.getPrincipal() ).map( p -> p.getName() );
        
        // Store the process information, file path and the name of the user in
        // the database. This will be used later to lookup the process ID, and
        // to remove the capture file.
        
        UUID dbid = dumpcapDbService.createDumpcapProcess(pid, path.toString(), type, principal_name );
        
        return Response.ok( dbid ).build();
    }
    
    @PUT
    @Path("/{id}")
    @Produces( MediaType.TEXT_PLAIN )
    @RolesAllowed("user")
    @NoCache
    public Response stopCapture( @PathParam("id") String id ) throws IOException, GeneralSecurityException
    {
        if( id == null )
        {
            return Response.status( Response.Status.BAD_REQUEST ).build();
        }
        
        DumpcapProcess proc = dumpcapDbService.find( UUID.fromString( id ), getPrincipalName() );
        
        if( proc.getPathName() != null )
        {
            File file = new File( proc.getPathName() );
            
            if( file.exists() )
            {
                Optional<ProcessHandle> ph = ProcessHandle.of( proc.getPid() );
                
                ph.ifPresent( handle -> {
                    handle.destroy();
                    dumpcapDbService.stopDumpcapProcess( UUID.fromString( id ), getPrincipalName() );
                } );
            }
        }

        return Response.ok().build();
    }
    
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @RolesAllowed("user")
    @NoCache
    public Response list()
    {
        List<DumpcapProcessDefaultView> capture_ids = dumpcapDbService.list( getPrincipalName() );
        
        return Response.ok( capture_ids ).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed("user")
    @NoCache
    public Response readCapture( @PathParam("id") String id ) throws IOException, InterruptedException, GeneralSecurityException
    {
        if( id == null )
        {
            return Response.status( Response.Status.BAD_REQUEST ).build();
        }
        
        DumpcapProcess proc = dumpcapDbService.find( UUID.fromString( id ), getPrincipalName() );
        
        File file = new File( proc.getPathName() );
        
        BufferedInputStream bin = new BufferedInputStream( new FileInputStream( file ) );
        
        ResponseBuilder rb = Response.ok( bin );
        
        rb.header( "Content-Disposition", "attachment;filename=capture.pcapng" );

        return rb.build();
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("user")
    @NoCache
    public Response deleteCapture( @PathParam("id") String id ) throws IOException, GeneralSecurityException
    {
        if( id == null )
        {
            return Response.status( Response.Status.BAD_REQUEST ).build();
        }
        
        DumpcapProcess proc = dumpcapDbService.find( UUID.fromString( id ), getPrincipalName() );
        File file = new File( proc.getPathName() );
        
        if( file.exists() ) 
        {
            boolean deleted = file.delete();
            
            if( deleted || proc.getStatus().equalsIgnoreCase( "deleted" ) )
            {
                dumpcapDbService.deleteDumpcapProcess( UUID.fromString( id ), getPrincipalName() );
            }
        }
        else
        {
            logger.error( "Deleting non-existent capture {} from database. Path {} does not exist.", proc.getId(), proc.getPathName() );
            dumpcapDbService.deleteDumpcapProcess( UUID.fromString( id ), getPrincipalName() );
        }
        
        // Following the REST Semantics demand that a DELETE operation
        // return the same result every time.
            
        return Response.ok().build();
    }
    
    private Optional<String> getPrincipalName()
    {
        Optional<SecurityIdentity> sidentity = Optional.ofNullable( identity );
        Optional<String> principal_name = sidentity.map( sid -> sid.getPrincipal() ).map( p -> p.getName() );
        
        if( allUsers( principal_name ) )
        {
            return Optional.empty();
        }
        
        return principal_name;
    }
    
    private boolean allUsers( Optional<String> principal_name )
    {
        if( principal_name.isPresent() )
        {
            return userPreferenceService.getBooleanUserPreferenceValue( principal_name.get(), "all_users", false );
        }
        
        return false;
    }

}