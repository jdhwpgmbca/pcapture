/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.jpa;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 *
 * @author jdh
 */
@Entity
@Table(name="USER_PREF")
public class UserPreference implements Serializable
{
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(length = 255)
    @NotBlank
    @Size( min = 1, max = 255 )
    private String userName;
    
    @Column(length = 255)
    @NotBlank
    @Size( min = 1, max = 255 )
    private String prefName;
    
    @Column(length = 255)
    @NotBlank
    @Size( min = 1, max = 255 )
    private String prefValue;

    public Long getId()
    {
        return id;
    }

    public void setId( Long id )
    {
        this.id = id;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    public String getPrefName()
    {
        return prefName;
    }

    public void setPrefName( String prefName )
    {
        this.prefName = prefName;
    }

    public String getPrefValue()
    {
        return prefValue;
    }

    public void setPrefValue( String prefValue )
    {
        this.prefValue = prefValue;
    }

}
