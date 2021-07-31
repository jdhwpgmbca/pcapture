/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 * @author jdh
 */
public class CryptoUtils
{

    public static SecretKey generateKey( String algorithm, int key_size ) throws NoSuchAlgorithmException
    {
        KeyGenerator keyGenerator = KeyGenerator.getInstance( algorithm );
        keyGenerator.init( key_size );
        SecretKey key = keyGenerator.generateKey();
        
        return key;
    }
    
    public static KeyStore createOrLoadKeyStore( String path, char[] password ) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
    {
        KeyStore ks = KeyStore.getInstance( KeyStore.getDefaultType() );
        File ksfile = new File( path );
        
        if( ksfile.exists() )
        {
            try( BufferedInputStream bin = new BufferedInputStream( new FileInputStream( ksfile ) ) )
            {
                ks.load( bin, password );
            }
        }
        else
        {
            ks.load( null, password );
        }
        
        return ks;
    }
    
    public static void saveKeyStore( KeyStore keystore, char[] password, File keystore_file ) throws IOException, GeneralSecurityException
    {
        try( BufferedOutputStream stream = new BufferedOutputStream( new FileOutputStream( keystore_file ) ) )
        {
            keystore.store( stream, password );
        }
    }
    
    public static SecretKey loadSecretKey( KeyStore keystore, String alias, char[] password ) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        return (SecretKey) keystore.getKey( alias, password );
    }
    
    public static void storeSecretKeyInKeyStore( SecretKey key, KeyStore keystore, String alias, char[] password ) throws IOException, GeneralSecurityException
    {
        KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry( key );
        KeyStore.ProtectionParameter pass = new KeyStore.PasswordProtection( password );
        
        keystore.setEntry( alias, secret, pass );
    }

    public static IvParameterSpec generateIv()
    {
        byte[] iv = new byte[16];

        new SecureRandom().nextBytes( iv );

        return new IvParameterSpec( iv );
    }

    public static String encrypt( String algorithm, String input, SecretKey key, IvParameterSpec iv )
            throws NoSuchPaddingException, NoSuchAlgorithmException,
                   InvalidAlgorithmParameterException, InvalidKeyException,
                   BadPaddingException, IllegalBlockSizeException
    {
        Cipher cipher = Cipher.getInstance( algorithm );

        cipher.init( Cipher.ENCRYPT_MODE, key, iv );

        byte[] cipherText = cipher.doFinal( input.getBytes() );

        return Base64.getEncoder().encodeToString( cipherText );
    }

    public static String decrypt( String algorithm, String cipherText, SecretKey key, IvParameterSpec iv )
            throws NoSuchPaddingException, NoSuchAlgorithmException,
                   InvalidAlgorithmParameterException, InvalidKeyException,
                   BadPaddingException, IllegalBlockSizeException
    {

        Cipher cipher = Cipher.getInstance( algorithm );
        cipher.init( Cipher.DECRYPT_MODE, key, iv );
        
        byte[] plainText = cipher.doFinal( Base64.getDecoder().decode( cipherText ) );
        
        return new String( plainText );
    }
}
