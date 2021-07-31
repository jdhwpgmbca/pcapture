/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author jdh
 */
public class CryptoUtilsTest
{
    
    public CryptoUtilsTest()
    {
    }
    
    @BeforeEach
    public void setUp()
    {
    }
    
    @AfterEach
    public void tearDown()
    {
    }

    /**
     * Test of generateKey method, of class CryptoUtils.
     */
//    @Test
//    public void testGenerateKey() throws Exception
//    {
//        System.out.println( "generateKey" );
//        String algorithm = "";
//        int key_size = 0;
//        SecretKey expResult = null;
//        SecretKey result = CryptoUtils.generateKey( algorithm, key_size );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }

    /**
     * Test of generateIv method, of class CryptoUtils.
     */
//    @Test
//    public void testGenerateIv()
//    {
//        System.out.println( "generateIv" );
//        IvParameterSpec expResult = null;
//        IvParameterSpec result = CryptoUtils.generateIv();
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }

    /**
     * Test of encrypt method, of class CryptoUtils.
     */
//    @Test
//    public void testEncrypt() throws Exception
//    {
//        System.out.println( "encrypt" );
//        String algorithm = "";
//        String input = "";
//        SecretKey key = null;
//        IvParameterSpec iv = null;
//        String expResult = "";
//        String result = CryptoUtils.encrypt( algorithm, input, key, iv );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }

    /**
     * Test of decrypt method, of class CryptoUtils.
     */
//    @Test
//    public void testDecrypt() throws Exception
//    {
//        System.out.println( "decrypt" );
//        String algorithm = "";
//        String cipherText = "";
//        SecretKey key = null;
//        IvParameterSpec iv = null;
//        String expResult = "";
//        String result = CryptoUtils.decrypt( algorithm, cipherText, key, iv );
//        assertEquals( expResult, result );
//        // TODO review the generated test code and remove the default call to fail.
//        fail( "The test case is a prototype." );
//    }
    
    @Test
    void givenString_whenEncrypt_thenSuccess()
        throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
        BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException { 

        String input = "baeldung";
        SecretKey key = CryptoUtils.generateKey( "AES", 128 );
        IvParameterSpec ivParameterSpec = CryptoUtils.generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";
        String cipherText = CryptoUtils.encrypt(algorithm, input, key, ivParameterSpec);
        String plainText = CryptoUtils.decrypt(algorithm, cipherText, key, ivParameterSpec);
        
        Assertions.assertEquals(input, plainText);
    }
}
