/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.svc;

import com.rtds.jpa.UserPreference;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author jdh
 */
@ApplicationScoped
@Transactional
public class UserPreferenceService
{

    @Inject
    EntityManager em;

    public UserPreference getUserPreference( @NotBlank String username, @NotBlank String pref_name )
    {
        try
        {
            return em.createQuery( "select pref from UserPreference pref where pref.userName = :username and pref.prefName = :prefname", UserPreference.class )
                    .setParameter( "username", username )
                    .setParameter( "prefname", pref_name )
                    .getSingleResult();
        }
        catch( NoResultException ex )
        {
            return null;
        }
    }

    public String getUserPreferenceValue( @NotBlank String username, @NotBlank String pref_name, @NotBlank String default_value )
    {
        UserPreference pref = getUserPreference( username, pref_name );
        
        return pref != null ? pref.getPrefValue() : default_value;
    }
    
    public boolean getBooleanUserPreferenceValue( @NotBlank String username, @NotBlank String pref_name, boolean default_value )
    {
        String string_value  = getUserPreferenceValue( username, pref_name, Boolean.toString( default_value ) );
        
        return Boolean.valueOf( string_value );
    }
    
    public void setBooleanPreferenceValue( @NotBlank String username, @NotBlank String pref_name, boolean pref_value )
    {
        setPreferenceValue( username, pref_name, Boolean.toString( pref_value ) );
    }

    public void setPreferenceValue( @NotBlank String username, @NotBlank String pref_name, @NotBlank String pref_value )
    {
        UserPreference pref = getUserPreference( username, pref_name );

        if( pref != null )
        {
            pref.setPrefValue( pref_value );
        }
        else
        {
            pref = new UserPreference();

            pref.setUserName( username );
            pref.setPrefName( pref_name );
            pref.setPrefValue( pref_value );

            em.persist( pref );
        }
    }
}
