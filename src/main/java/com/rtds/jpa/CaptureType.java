/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.jpa;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author jdh
 */
@Entity
public class CaptureType implements Serializable
{
    @Id
    private String urlSuffix;
    private String label;
    private String captureFilter;

    public String getLabel()
    {
        return label;
    }

    public void setLabel( String label )
    {
        this.label = label;
    }

    public String getUrlSuffix()
    {
        return urlSuffix;
    }

    public void setUrlSuffix( String urlSuffix )
    {
        this.urlSuffix = urlSuffix;
    }

    public String getCaptureFilter()
    {
        return captureFilter;
    }

    public void setCaptureFilter( String captureFilter )
    {
        this.captureFilter = captureFilter;
    }
    
}
