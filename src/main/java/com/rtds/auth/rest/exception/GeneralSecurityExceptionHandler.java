package com.rtds.auth.rest.exception;

import com.rtds.auth.event.ApplicationEvent;
import java.security.GeneralSecurityException;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GeneralSecurityExceptionHandler implements ExceptionMapper<GeneralSecurityException>
{

    final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    Event<ApplicationEvent> applicationEvent;

    @Override
    public Response toResponse( GeneralSecurityException exception )
    {
        ApplicationEvent event = new ApplicationEvent( exception.getMessage() );

        applicationEvent.fire( event );

        return Response.status( Status.INTERNAL_SERVER_ERROR ).entity( exception.getMessage() ).build();
    }

}
