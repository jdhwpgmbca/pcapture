package com.rtds.svc;

import com.rtds.jpa.CaptureType;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author jdh
 */
@QuarkusTest
public class CaptureTypeServiceIntegrationTest
{
    @Inject
    CaptureTypeService captureTypeService;
    
    @Inject
    EntityManager em;
    
    public CaptureTypeServiceIntegrationTest()
    {
    }
    
    @BeforeEach
    public void setUp()
    {
        // QuarkusTest does not seem to inject embedded members, so you need to
        // do that manually.
        
        captureTypeService.em = em;
    }
    
    @AfterEach
    public void tearDown()
    {
    }
    
    @Test
    public void testCreateOrUpdateCaptureType_CaptureTypeNull()
    {
        try
        {
            captureTypeService.createOrUpdateCaptureType( null );
            fail( "Expected ConstraintViolationException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }
    
    @Test
    public void testCreateOrUpdateCaptureType_urlSuffixNull()
    {
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( null );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( "testfilter" );
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected ConstraintViolationException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }

    /**
     * Test of createOrUpdateCaptureType method, of class CaptureTypeService.
     */
    @Test
    public void testCreateOrUpdateCaptureType_urlSuffixTooLong()
    {
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "test1234567890" );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( "testfilter" );
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected RollbackException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }

    @Test
    public void testCreateOrUpdateCaptureType_urlSuffixBlank()
    {
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "" );
        value.setLabel( "testlabel" );
        value.setCaptureFilter( "testfilter" );
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected RollbackException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }
    
    @Test
    public void testCreateOrUpdateCaptureType_labelTooLong()
    {
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "test" );
        value.setLabel( "123456789012345678901234567890123456789012345678901" );
        value.setCaptureFilter( "testfilter" );
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected ConstraintViolationException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }

    @Test
    public void testCreateOrUpdateCaptureType_labelBlank()
    {
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "test" );
        value.setLabel( "" );
        value.setCaptureFilter( "testfilter" );
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected ConstraintViolationException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }

    @Test
    public void testCreateOrUpdateCaptureType_labelNull()
    {
        CaptureType value = new CaptureType();
        
        value.setUrlSuffix( "test" );
        value.setLabel( null );
        value.setCaptureFilter( "testfilter" );
        
        try
        {
            captureTypeService.createOrUpdateCaptureType( value );
            fail( "Expected ConstraintViolationException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }
    
    @Test
    public void testFindFilter_null_urlSuffix()
    {
        try
        {
            captureTypeService.findFilter( null );
            fail( "Expected ConstraintViolationException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }
    
    @Test
    public void testDeleteCaptureType_with_null_urlsuffix()
    {
        try
        {
            captureTypeService.deleteCaptureType( null );
            fail( "Expected ConstraintViolationException not thrown." );
        }
        catch( ConstraintViolationException ex )
        {
            printConstraintViolationException( ex );
        }
    }
    
    void printConstraintViolationException( ConstraintViolationException ex )
    {
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        
        Function<ConstraintViolation<?>,String> mapper = violation ->
                String.format( "%s: %s", violation.getPropertyPath(), violation.getMessage() );
        
        violations.stream().map( mapper ).forEach( System.out::println );
    }

    
}
