/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.jpa;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author jdh
 */
@Entity
@Table(name="CAPTURE_TYPE")
public class CaptureType implements Serializable
{
    @Id
    @Column(name = "URL_SUFFIX")
    private String urlSuffix;
    
    @Column(name = "LABEL")
    private String label;
    
    @Column(name = "CAPTURE_FILTER")
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
