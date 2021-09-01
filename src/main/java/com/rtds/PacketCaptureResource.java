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
import com.rtds.svc.CaptureTypeService;
import com.rtds.svc.DumpcapDbService;
import com.rtds.svc.UserPreferenceService;
import io.quarkus.security.identity.SecurityIdentity;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
    
    @ConfigProperty(name = "data-directory")
    Optional<String> dataDirectory;
    
    @Inject
    DumpcapDbService dumpcapDbService;
    
    @Inject
    UserPreferenceService userPreferenceService;
    
    @Inject
    CaptureTypeService captureTypeService;
    
    @Inject
    SecurityIdentity identity;
    
    @POST
    @Path("/{type}")
    @Produces( MediaType.TEXT_PLAIN )
    @RolesAllowed( { "user", "admin" } )
    public Response startTypedCapture(
            @NotBlank
            @Size( min=1, max=10 )
            @PathParam("type")
            String url_suffix ) throws IOException, GeneralSecurityException
    {
        String filter = captureTypeService.findFilter( url_suffix );
        java.nio.file.Path script_path = java.nio.file.Path.of( startCaptureScript );
        
        logger.info(  "startCaptureScript path {}", startCaptureScript );
        
        if( Files.notExists( script_path, LinkOption.NOFOLLOW_LINKS ) )
        {
            logger.error( "startCaptureScript can't be found in specified location {}", startCaptureScript );
        }
        
        java.nio.file.Path path = createCaptureFile();
        
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
        
        UUID dbid = dumpcapDbService.createDumpcapProcess( pid, path.toString(), url_suffix, principal_name );
        
        return Response.ok( dbid ).build();
    }

    @PUT
    @Path("/{id}")
    @Produces( MediaType.TEXT_PLAIN )
    @RolesAllowed( { "user", "admin" } )
    @NoCache
    public Response stopCapture(
            @NotBlank
            @Size( min=36, max=36 )
            @PathParam("id")
            String id ) throws IOException, GeneralSecurityException
    {
        // UUID values are expected to be exactly 36 characters long.
        
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
    @RolesAllowed( { "user", "admin" } )
    @NoCache
    public Response list()
    {
        List<DumpcapProcessDefaultView> capture_ids = dumpcapDbService.list( getPrincipalName() );
        
        return Response.ok( capture_ids ).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed( { "user", "admin" } )
    @NoCache
    public Response readCapture(
            @NotBlank
            @Size( min=36, max=36 )
            @PathParam("id") String id ) throws IOException, InterruptedException, GeneralSecurityException
    {
        // UUID values are expected to be exactly 36 characters long.
        
        if( id.length() != 36 )
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
    @RolesAllowed( { "user", "admin" } )
    @NoCache
    public Response deleteCapture(
            @NotBlank
            @Size( min=36, max=36 )
            @PathParam("id") String id ) throws IOException, GeneralSecurityException
    {
        // UUID values are expected to be exactly 36 characters long.
        
        if( id.length() != 36 )
        {
            return Response.status( Response.Status.BAD_REQUEST ).build();
        }
        
        DumpcapProcess proc = dumpcapDbService.find( UUID.fromString( id ), getPrincipalName() );
        
        if( proc == null )
        {
            // Deleted already... This was probably caused by a double click.
            
            return Response.ok().build();
        }
        
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
    
    private java.nio.file.Path createCaptureFile() throws IOException
    {
        java.nio.file.Path dir = null, path = null;
        
        if( dataDirectory.isPresent() && ! dataDirectory.get().isBlank() )
        {
            dir = java.nio.file.Paths.get( dataDirectory.get() );
            
            if( ! dir.toFile().exists() )
            {
                if( dir.toFile().mkdirs() )
                {
                    path = java.nio.file.Files.createTempFile( dir, "wireshark-capture-", ".pcapng" );
                }
            }
            else
            {
                path = java.nio.file.Files.createTempFile( dir, "wireshark-capture-", ".pcapng" );
            }
        }
        
        if( path == null )
        {
            path = java.nio.file.Files.createTempFile( "wireshark-capture-", ".pcapng" );
        }
        
        return path;
    }
    
    private Optional<String> getPrincipalName()
    {
        Optional<SecurityIdentity> sidentity = Optional.ofNullable( identity );
        Optional<Boolean> admin = sidentity.map( id -> id.hasRole( "admin" ) );
        Optional<String> principal_name = sidentity.map( sid -> sid.getPrincipal() ).map( p -> p.getName() );
        
        // If the this is an admin user and the all_users preference is true, then
        // return an Optional.empty() instead of the principal_name.
        
        if( allUsers( principal_name ) && admin.isPresent() && admin.get() == true )
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