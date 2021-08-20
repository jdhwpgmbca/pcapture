/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.view;

import com.rtds.jpa.DumpcapProcess;

/**
 *
 * @author jdh
 */
public class DumpcapProcessUserView extends DumpcapProcessDefaultView
{
    private String uid;
    
    public DumpcapProcessUserView( DumpcapProcess proc )
    {
        super( proc );
        
        uid = proc.getUid();
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid( String uid )
    {
        this.uid = uid;
    }
    
}
