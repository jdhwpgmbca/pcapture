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
import org.mockito.Mockito;
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
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "testsuffix" );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( "testfilter" );
        
        when( em.find( CaptureType.class, "testsuffix" ) ).thenReturn( null );
        
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
    public void testCreateOrUpdateCaptureType_filter_null_unfound()
    {
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "testurlsuffix" );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( null );
        
        captureTypeService.createOrUpdateCaptureType( value );
        
        verify( em ).persist( value );
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
        String url_suffix = "goose";
        CaptureType value = new CaptureType();
        value.setCaptureFilter( "testfilter" );
        when( em.find( CaptureType.class, url_suffix ) ).thenReturn( null );
        try
        {
            captureTypeService.findFilter( url_suffix );
            fail( "Expected IllegalArgumentException not thrown." );
        }
        catch( IllegalArgumentException ex )
        {
            // expected fail.
        }
    }

    /**
     * Test of find method, of class CaptureTypeService.
     */
    @Test
    public void testFind_notfound()
    {
        String url_suffix = "sv";
        CaptureType expResult = null;
        when( em.find( CaptureTypeService.class, url_suffix ) ).thenReturn( null );
        CaptureType result = captureTypeService.find( url_suffix );
        assertEquals( expResult, result );
    }

    /**
     * Test of find method, of class CaptureTypeService.
     */
    @Test
    public void testFind_found()
    {
        String url_suffix = "sv";
        CaptureType persistent = new CaptureType();
        when( em.find( CaptureType.class, url_suffix ) ).thenReturn( persistent );
        CaptureType expResult = persistent;
        CaptureType result = captureTypeService.find( url_suffix );
        assertEquals( expResult, result );
    }

    /**
     * Test of list method, of class CaptureTypeService.
     */
    @Test
    @Disabled
    @SuppressWarnings("unchecked")
    public void testList()
    {
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
    public void testDeleteCaptureType_found()
    {
        String url_suffix = "sv";
        CaptureType persistent = new CaptureType();
        persistent.setUrlSuffix( url_suffix );
        when( em.find( CaptureType.class, url_suffix ) ).thenReturn( persistent );
        captureTypeService.deleteCaptureType( url_suffix );
        verify( em ).remove( persistent );
    }
    
    @Test
    public void testDeleteCaptureType_notFound()
    {
        String url_suffix = "sv";
        CaptureType persistent = new CaptureType();
        persistent.setUrlSuffix( url_suffix );
        when( em.find( CaptureType.class, url_suffix ) ).thenReturn( null );
        captureTypeService.deleteCaptureType( url_suffix );
        verify( em ).find( CaptureType.class, url_suffix );
        Mockito.verifyNoMoreInteractions( em );
    }
    
}
