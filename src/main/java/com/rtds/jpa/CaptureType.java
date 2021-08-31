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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author jdh
 */
@Entity
@Table(name="CAPTURE_TYPE")
public class CaptureType implements Serializable
{
    @Id
    @Column(name = "URL_SUFFIX", length = 10)
    @NotBlank
    @Size( min = 1, max = 10 )
    private String urlSuffix;
    
    @Column(name = "LABEL", length = 50)
    @NotBlank
    @Size( min = 1, max = 50 )
    private String label;
    
    @Column(name = "CAPTURE_FILTER", length = 255)
    @Size( max = 255 )
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
