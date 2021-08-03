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

    @Test
    void testEncryptDecrypt()
            throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
                   BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException
    {

        String input = "Some not very random text.";
        SecretKey key = CryptoUtils.generateKey( "AES", 128 );
        IvParameterSpec ivParameterSpec = CryptoUtils.generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";
        String cipherText = CryptoUtils.encrypt( algorithm, input, key, ivParameterSpec );
        String plainText = CryptoUtils.decrypt( algorithm, cipherText, key, ivParameterSpec );

        Assertions.assertEquals( input, plainText );
    }
}
