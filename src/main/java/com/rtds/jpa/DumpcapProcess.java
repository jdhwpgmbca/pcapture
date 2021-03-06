/*
 *   Copyright (c) 2021, RTDS Technologies Inc.
 *
 *   Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *   LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *   GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 *   DAMAGE.
 */

package com.rtds.jpa;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;

/**
 *
 * @author jdh
 */
@Entity
@Table(name="DUMPCAP_PROCESS")
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

    @NotNull
    private Long pid;
    
    @Column( length = 10 )
    @NotBlank
    @Size( min = 1, max = 10)
    private String type;
    
    @Column( length = 1024 )
    @NotBlank
    @Size( min = 1, max = 1024 )
    private String pathName;

    @Column( length = 50 )
    @NotBlank
    @Size( min = 1, max = 50)
    private String uid;
    
    @Column( length = 255 )
    @NotBlank
    @Size( min = 1, max = 255)
    private String status;

    public UUID getId()
    {
        return id;
    }

    public void setId( UUID id)
    {
        this.id = id;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

}
