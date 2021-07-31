package com.rtds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtds.crypto.CryptoUtils;
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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/capture" )
public class PacketCaptureService
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
    @Path("/start")
    @Produces( MediaType.TEXT_PLAIN )
    public Response startCapture() throws IOException, GeneralSecurityException
    {
        java.nio.file.Path path = java.nio.file.Files.createTempFile( "wireshark-capture-", ".pcapng" );
        
        // Typically this would be in the scripts:
        // 
        // Goose: sudo dumpcap -f "ether proto 0x99B8" -w [path]
        // GSE:   sudo dumpcap -f "ether proto 0x99B9" -w [path]
        // SV:    sudo dumpcap -f "ether proto 0x88BA" -w [path]

        // This now works on Windows
        
        ProcessBuilder pb = new ProcessBuilder( "powershell.exe",  "-ExecutionPolicy", "Bypass", "-Command", startCaptureScript, path.toString() );
        
        pb.redirectErrorStream( true );
        
        Process proc = pb.start();
        
        long pid;
        
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

    @POST
    @Path("/stop")
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
        
        ph.ifPresent( handle -> {
            handle.destroy();
        } );

        return Response.ok( token ).build();
    }
    

    @GET
    @Path("/read")
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
            
            return Response.ok().build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }

    private Map<String, String> extractMapFromJSON( String json ) throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
        
        return mapper.readValue( json, typeRef );
    }

    private String extractJSONFromEncryptedToken( String token ) throws IOException, GeneralSecurityException
    {
        String[] split_token = token.split( ":" );
        IvParameterSpec iv = new IvParameterSpec( Base64.getDecoder().decode( split_token[0] ) );
        SecretKey key = getSecretKey();
        
        return CryptoUtils.decrypt( "AES/CBC/PKCS5Padding", split_token[1], key, iv );
    }
    
    private SecretKey getSecretKey() throws IOException, GeneralSecurityException
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