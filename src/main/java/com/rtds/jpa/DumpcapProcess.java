/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.jpa;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author jdh
 */
@Entity
public class DumpcapProcess implements Serializable
{

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator( 
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column( name = "id", updatable = false, nullable = false )
    private UUID id;

    private Long pid;
    private String pathName;
    private String uid;

    public UUID getId()
    {
        return id;
    }

    public void setId( UUID id)
    {
        this.id = id;
    }

    public Long getPid()
    {
        return pid;
    }

    public void setPid(Long pid)
    {
        this.pid = pid;
    }

    public String getPathName()
    {
        return pathName;
    }

    public void setPathName(String pathName)
    {
        this.pathName = pathName;
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

}
