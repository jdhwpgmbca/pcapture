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

package com.rtds.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
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
