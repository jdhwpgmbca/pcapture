/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.data;

import org.jboss.resteasy.annotations.Form;

/**
 *
 * @author jdh
 */
public class CaptureInfo
{
    private String filePath;
    private long pid;
    
    public CaptureInfo( String filePath, long pid )
    {
        this.filePath = filePath;
        this.pid = pid;
    }
    
    public String getFilePath()
    {
        return filePath;
    }
    
    public void setFilePath( String filePath )
    {
        this.filePath = filePath;
    }
    
    public long getPid()
    {
        return pid;
    }
    
    public void setPid( long pid )
    {
        this.pid = pid;
    }
    
}
