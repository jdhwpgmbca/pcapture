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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtds.crypto.CryptoUtils;
import io.quarkus.security.Authenticated;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/capture" )
//@Authenticated
public class PacketCaptureResource
{
    @ConfigProperty(name = "startCaptureScript")
    private String startCaptureScript;
    
    @ConfigProperty(name = "keyStorePath" )
    private String keyStorePath;
    
    @ConfigProperty(name = "keyStoreAlias")
    private String keyStoreAlias;
    
    @ConfigProperty(name = "keyStorePassword")
    private String keyStorePassword;
    
    @POST
    @Produces( MediaType.TEXT_PLAIN )
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
        
        Process proc = pb.start();
        
        long pid;
        
        // Return the PID of the command that's run from the script in
        // the standard output, not the PID of the script itself, as would
        // have been returned by Process.pid(). This is important, because
        // when you run the /stop endpoint later, you want to terminate the
        // capture command (probably dumpcap or tcpdump), not the script
        // itself.
        
        try( InputStream in = proc.getInputStream() )
        {
            String length_string = new String( in.readAllBytes(), "UTF8" );
            
            pid = Long.parseLong( length_string.trim() );
        }
        
        Map<String,String> map = new HashMap<>();
        
        map.put( "path", path.toString() );
        map.put( "pid", Long.toString( pid ) );
        
        ObjectMapper mapper = new ObjectMapper();
        
        String cleartext = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( map );

        SecretKey key = getSecretKey();
        IvParameterSpec iv = CryptoUtils.generateIv();
        String cyphertext = CryptoUtils.encrypt( "AES/CBC/PKCS5Padding", cleartext, key, iv );
        String iv_text = Base64.getEncoder().encodeToString( iv.getIV() );
        
        // Now return the opaque (encrypted) token in the HTTP response.
        
        // Base64 is composed of upper and lowercase letters, 0-9 and +/-
        // and the == are terminators, so the : character is distinct.
        
        return Response.ok( iv_text + ":" + cyphertext ).build();
    }

    @PUT
    @Produces( MediaType.TEXT_PLAIN )
    public Response stopCapture( @HeaderParam("token") String token ) throws IOException, GeneralSecurityException
    {
        if( token == null )
        {
            return Response.status( Response.Status.BAD_REQUEST ).build();
        }
        
        String json = extractJSONFromEncryptedToken( token );
        Map<String, String> map = extractMapFromJSON( json );
        
        Optional<ProcessHandle> ph = ProcessHandle.of( Long.parseLong( map.get( "pid" ) ) );
        
        // Check to make the decrypted path exists, before using the process
        // id to terminate the process. This ensures that the decryption isn't
        // simply a random bunch of bytes from a token generated on the client
        // side.
        
        if( map.get( "path" ) != null )
        {
            File file = new File( map.get( "path" ) );
            
            if( file.exists() )
            {
                // Now that we know the path exists, we can assume the decrypted
                // data is valid, and we can use the PID to terminate the process.
                
                ph.ifPresent( handle -> {
                    handle.destroy();
                } );
            }
        }

        return Response.ok( token ).build();
    }
    

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response readCapture( @HeaderParam("token") String token ) throws IOException, InterruptedException, GeneralSecurityException
    {
        if( token == null )
        {
            return Response.status( Response.Status.BAD_REQUEST ).build();
        }
        
        String json = extractJSONFromEncryptedToken( token );
        Map<String, String> map = extractMapFromJSON( json );
        
        File file = new File( map.get( "path" ) );
        
        ResponseBuilder rb;
        
        BufferedInputStream bin = new BufferedInputStream( new FileInputStream( file ) );
        
        rb = Response.ok( bin );
        rb.header( "Content-Disposition", "attachment;filename=capture.pcapng" );

        return rb.build();
    }
    
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteCapture( @HeaderParam("token") String token ) throws IOException, GeneralSecurityException
    {
        if( token == null )
        {
            return Response.status( Response.Status.BAD_REQUEST ).build();
        }
        
        String json = extractJSONFromEncryptedToken( token );
        Map<String, String> map = extractMapFromJSON( json );
        File file = new File( map.get( "path" ) );
        
        if( file.exists() ) 
        {
            file.delete();
        }
        
        // Following the REST Semantics demand that a DELETE operation
        // return the same result every time.
            
        return Response.ok().build();
    }

    Map<String, String> extractMapFromJSON( String json ) throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
        
        return mapper.readValue( json, typeRef );
    }

    String extractJSONFromEncryptedToken( String token ) throws IOException, GeneralSecurityException
    {
        String[] split_token = token.split( ":" );
        IvParameterSpec iv = new IvParameterSpec( Base64.getDecoder().decode( split_token[0] ) );
        SecretKey key = getSecretKey();
        
        return CryptoUtils.decrypt( "AES/CBC/PKCS5Padding", split_token[1], key, iv );
    }
    
    SecretKey getSecretKey() throws IOException, GeneralSecurityException
    {
        SecretKey key;
        File keystore_file = new File( keyStorePath );
        
        if( ! keystore_file.exists() )
        {
            key = CryptoUtils.generateKey( "AES", 256 );
            KeyStore keystore = CryptoUtils.createOrLoadKeyStore( keyStorePath, keyStorePassword.toCharArray() );
            CryptoUtils.storeSecretKeyInKeyStore( key, keystore, keyStoreAlias, keyStorePassword.toCharArray() );
            CryptoUtils.saveKeyStore( keystore, keyStorePassword.toCharArray(), keystore_file );
        }
        else
        {
            KeyStore keystore = CryptoUtils.createOrLoadKeyStore( keyStorePath, keyStorePassword.toCharArray() );
            key = CryptoUtils.loadSecretKey( keystore, keyStoreAlias, keyStorePassword.toCharArray() );
        }
        
        return key;
    }
    
}