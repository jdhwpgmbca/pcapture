/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.view;

import com.rtds.jpa.DumpcapProcess;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 *
 * @author jdh
 */
public class DumpcapProcessUserView extends DumpcapProcessDefaultView
{
    private String uid;
    
    public DumpcapProcessUserView( @NotNull @Valid DumpcapProcess proc )
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
