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

package com.rtds.dto;

import com.rtds.jpa.DumpcapProcess;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Data transfer object for the DumpcapProcess JPA entity.
 * 
 *  The intention of this object is to allow the exposure of some, but
 *  not all of the DumpcapProcess attributes, as well as some of the
 *  file meta-data, such as length. The path itself is not represented
 *  because the user doesn't need to know what it is. They only need the
 *  UUID value. The other attributes might be useful to the user, and don't
 *  give away anything that's security sensitive.
 * 
 * @author jdh
 */
public class DumpcapProcessDto
{
    private Logger logger = LoggerFactory.getLogger( DumpcapProcessDto.class );

    private UUID id;
    private String type;
    private String status;
    private Instant creationTime;
    private Instant lastModifiedTime;
    private Long length;

    public DumpcapProcessDto(DumpcapProcess proc)
    {
        this.id = proc.getId();
        this.status = proc.getStatus();
        this.type = proc.getType();

        Path path = Paths.get(proc.getPathName());

        try
        {
            if( path.toFile().exists() )
            {
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

                this.creationTime = attr.creationTime().toInstant();
                this.lastModifiedTime = attr.lastModifiedTime().toInstant();
                this.length = attr.size();
            }
            else
            {
                this.status = "deleted";
            }
        }
        catch( IOException ex )
        {
            logger.error( "Error caught while trying to read capture file attributes", ex );
        }
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Instant getCreationTime()
    {
        return creationTime;
    }

    public void setCreationTime( Instant creationTime )
    {
        this.creationTime = creationTime;
    }

    public Instant getLastModifiedTime()
    {
        return lastModifiedTime;
    }

    public void setLastModifiedTime( Instant lastModifiedTime )
    {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Long getLength()
    {
        return length;
    }

    public void setLength( Long length )
    {
        this.length = length;
    }

}
