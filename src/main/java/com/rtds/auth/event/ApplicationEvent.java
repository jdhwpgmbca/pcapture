/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.auth.event;

/**
 *
 * @author jdh
 */
public class ApplicationEvent
{
    private String message;
    
    public ApplicationEvent( String message )
    {
        this.message = message;
    }
    
    public String getMesage()
    {
        return message;
    }
}
