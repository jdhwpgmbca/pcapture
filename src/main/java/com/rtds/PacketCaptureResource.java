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

import com.rtds.dto.DumpcapProcessDto;
import com.rtds.jpa.DumpcapProcess;
import com.rtds.svc.DumpcapDbService;
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

@Path("/api/capture" )
public class PacketCaptureResource
{
    @ConfigProperty(name = "startCaptureScript")
    String startCaptureScript;
    
    @ConfigProperty(name = "keyStorePath" )
    String keyStorePath;
    
    @ConfigProperty(name = "keyStoreAlias")
    String keyStoreAlias;
    
    @ConfigProperty(name = "keyStorePassword")
    String keyStorePassword;

    @Inject
    DumpcapDbService dumpcapDbService;
    
    @Inject
    SecurityIdentity identity;
    
    @POST
    @Produces( MediaType.TEXT_PLAIN )
    @RolesAllowed("user")
    public Response startCapture() throws IOException, GeneralSecurityException
    {
        java.nio.file.Path path = java.nio.file.Files.createTempFile( "wireshark-capture-", ".pcapng" );
        
        // Typically this would be in the scripts:
        // 
        // Goose: dumpcap -f "ether proto 0x99B8" -w [path]
        // GSE:   dumpcap -f "ether proto 0x99B9" -w [path]
        // SV:    dumpcap -f "ether proto 0x88BA" -w [path]

        // This now works on Windows
        
        ProcessBuilder pb = new ProcessBuilder( "powershell.exe",  "-ExecutionPolicy", "RemoteSigned", "-Command", startCaptureScript, path.toString() );
        
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
        
        // Store the process information, file path and the name of the user in
        // the database. This will be used later to lookup the process ID, and
        // to remove the capture file.
        
        UUID dbid = dumpcapDbService.createDumpcapProcess( pid, path.toString(), getPrincipalName() );
        
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
        List<DumpcapProcessDto> capture_ids = dumpcapDbService.list( getPrincipalName() );
        
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
            
            if( deleted )
            {
                dumpcapDbService.deleteDumpcapProcess( UUID.fromString( id ), getPrincipalName() );
            }
        }
        
        // Following the REST Semantics demand that a DELETE operation
        // return the same result every time.
            
        return Response.ok().build();
    }

    private Optional<String> getPrincipalName()
    {
        Optional<SecurityIdentity> sidentity = Optional.ofNullable( identity );
        
        return sidentity.map( sid -> sid.getPrincipal() ).map( p -> p.getName() );
    }
    
}