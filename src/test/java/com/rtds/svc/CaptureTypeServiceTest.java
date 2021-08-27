/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.svc;

import com.rtds.jpa.CaptureType;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author jdh
 */
@QuarkusTest
public class CaptureTypeServiceTest
{
    @InjectMock
    EntityManager em;
    
    CaptureTypeService captureTypeService;
    
    public CaptureTypeServiceTest()
    {
    }
    
    @BeforeEach
    public void setUp()
    {
        captureTypeService = new CaptureTypeService();
        
        assertNotNull( em );
        captureTypeService.em = em;
    }
    
    @AfterEach
    public void tearDown()
    {
    }

    /**
     * Test of createOrUpdateCaptureType method, of class CaptureTypeService
     * when the CaptureType isn't in the database.
     */
    @Test
    public void testCreateOrUpdateCaptureType_notFound()
    {
        System.out.println( "createOrUpdateCaptureType" );
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "testsuffix" );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( "testfilter" );
        
        captureTypeService.createOrUpdateCaptureType( value );
        
        verify( em ).persist( value );
    }

    /**
     * Test of createOrUpdateCaptureType method, of class CaptureTypeService
     * when the CaptureType is already in the database.
     */
    @Test
    public void testCreateOrUpdateCaptureType_found()
    {
        System.out.println( "createOrUpdateCaptureType" );
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "testsuffix" );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( "testfilter" );
        
        CaptureType persistent = mock( CaptureType.class );
        
        when( em.find( CaptureType.class, "testsuffix" ) ).thenReturn( persistent );
        
        captureTypeService.createOrUpdateCaptureType( value );
        
        verify( persistent ).setLabel( "testlabel" );
        verify( persistent ).setCaptureFilter( "testfilter" );
    }

    /**
     * Test of createOrUpdateCaptureType method, of class CaptureTypeService
     * when the CaptureType is a null value (I'm not sure that the RestEasy
     * implementation used by Quarkus will even allow this).
     */
    @Test
    public void testCreateOrUpdateCaptureType_null()
    {
        System.out.println( "createOrUpdateCaptureType" );
        CaptureType value = null;
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException ex )
        {
            // Expected: This will be caught and returned as an HTTP status code 400, BAD_REQUEST.
        }
    }

    /**
     * Test of createOrUpdateCaptureType method, of class CaptureTypeService
     * when the CaptureType is a null value (I'm not sure that the RestEasy
     * implementation used by Quarkus will even allow this).
     */
    @Test
    public void testCreateOrUpdateCaptureType_label_null()
    {
        System.out.println( "createOrUpdateCaptureType" );
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "testsuffix" );
        value.setLabel( null );
        value.setCaptureFilter( "testfilter" );
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException ex )
        {
            // Expected: This will be caught and returned as an HTTP status code 400, BAD_REQUEST.
        }
    }

    /**
     * Test of createOrUpdateCaptureType method, of class CaptureTypeService
     * when the CaptureType is a null value (I'm not sure that the RestEasy
     * implementation used by Quarkus will even allow this).
     */
    @Test
    public void testCreateOrUpdateCaptureType_urlsuffix_null()
    {
        System.out.println( "createOrUpdateCaptureType" );
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( null );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( "testfilter" );
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException ex )
        {
            // Expected: This will be caught and returned as an HTTP status code 400, BAD_REQUEST.
        }
    }

    /**
     * Test of createOrUpdateCaptureType method, of class CaptureTypeService
     * when the CaptureType is a null value (I'm not sure that the RestEasy
     * implementation used by Quarkus will even allow this).
     */
    @Test
    public void testCreateOrUpdateCaptureType_filter_null_unfound()
    {
        System.out.println( "createOrUpdateCaptureType" );
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "testurlsuffix" );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( null );
        
        captureTypeService.createOrUpdateCaptureType( value );
    }

    /**
     * Test of createOrUpdateCaptureType method, of class CaptureTypeService
     * when the CaptureType is a null value (I'm not sure that the RestEasy
     * implementation used by Quarkus will even allow this).
     */
    @Test
    public void testCreateOrUpdateCaptureType_filter_null_found()
    {
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "testsuffix" );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( null );
        
        CaptureType persistent = mock( CaptureType.class );
        
        when( em.find( CaptureType.class, "testsuffix" ) ).thenReturn( persistent );
        
        captureTypeService.createOrUpdateCaptureType( value );
        
        verify( persistent ).setLabel( "testlabel" );
        verify( persistent ).setCaptureFilter( null );
    }

    /**
     * Test of findFilter method, of class CaptureTypeService.
     */
    @Test
    public void testFindFilter_found()
    {
        System.out.println( "findFilter" );
        String url_suffix = "goose";
        CaptureType value = new CaptureType();
        value.setCaptureFilter( "testfilter" );
        when( em.find( CaptureType.class, url_suffix ) ).thenReturn( value );
        String expResult = "testfilter";
        String result = captureTypeService.findFilter( url_suffix );
        assertEquals( expResult, result );
    }

    /**
     * Test of findFilter method, of class CaptureTypeService.
     */
    @Test
    public void testFindFilter_notfound()
    {
        System.out.println( "findFilter" );
        String url_suffix = "goose";
        CaptureType value = new CaptureType();
        value.setCaptureFilter( "testfilter" );
        when( em.find( CaptureType.class, url_suffix ) ).thenReturn( null );
        String expResult = null;
        String result = captureTypeService.findFilter( url_suffix );
        assertEquals( expResult, result );
    }

    /**
     * Test of findFilter method, of class CaptureTypeService.
     */
    @Test
    public void testFindFilter_null_urlsuffix()
    {
        System.out.println( "findFilter" );
        String url_suffix = null;
        CaptureType value = new CaptureType();
        value.setCaptureFilter( "testfilter" );
        when( em.find( CaptureType.class, url_suffix ) ).thenReturn( null );
        
        try
        {
            captureTypeService.findFilter( url_suffix );
            fail( "The url_suffix must not be null" );
        }
        catch( IllegalArgumentException ex )
        {
            
        }
    }

    /**
     * Test of find method, of class CaptureTypeService.
     */
    @Test
    public void testFind_notfound()
    {
        System.out.println( "find" );
        String url_suffix = "sv";
        CaptureType expResult = null;
        CaptureType result = captureTypeService.find( url_suffix );
        assertEquals( expResult, result );
    }

    /**
     * Test of find method, of class CaptureTypeService.
     */
    @Test
    public void testFind_found()
    {
        System.out.println( "find" );
        String url_suffix = "sv";
        CaptureType value = new CaptureType();
        when( em.find( CaptureType.class, url_suffix ) ).thenReturn( value );
        CaptureType expResult = value;
        CaptureType result = captureTypeService.find( url_suffix );
        assertEquals( expResult, result );
    }

    /**
     * Test of list method, of class CaptureTypeService.
     */
    @Test
    @Disabled
    public void testList()
    {
        System.out.println( "list" );
        List<CaptureType> list = Collections.emptyList();
        TypedQuery<CaptureType> query  = mock( TypedQuery.class );
        // I really think this should work, but for some reason, Mockito is complaining about the return type being a TypedQuery<CaptureType> instead of a Query - which it's wrongly expecting.
        when( em.createQuery( "select ct from CaptureType ct", CaptureType.class ) ).thenReturn( query );
        when( query.getResultList() ).thenReturn( list );
        List<CaptureType> expResult = list;
        List<CaptureType> result = captureTypeService.list();
        assertEquals( expResult, result );
    }

    /**
     * Test of deleteCaptureType method, of class CaptureTypeService.
     */
    @Test
    public void testDeleteCaptureType()
    {
        System.out.println( "deleteCaptureType" );
        String url_suffix = "sv";
        captureTypeService.deleteCaptureType( url_suffix );
    }
    
    /**
     * Test of deleteCaptureType method, of class CaptureTypeService.
     */
    @Test
    public void testDeleteCaptureType_with_null_urlsuffix()
    {
        System.out.println( "deleteCaptureType" );
        String url_suffix = null;
        
        try
        {
            captureTypeService.deleteCaptureType( url_suffix );
            fail( "Expected IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException ex )
        {
            // expected
        }
    }
    
}
