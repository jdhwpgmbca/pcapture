/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    private Instant creationTime;
    private Instant lastModifiedTime;
    private Instant lastAccessTime;
    private Long length;

    public DumpcapProcessDto(DumpcapProcess proc)
    {
        this.id = proc.getId();

        Path path = Paths.get(proc.getPathName());

        try
        {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

            this.creationTime = attr.creationTime().toInstant();
            this.lastModifiedTime = attr.lastModifiedTime().toInstant();
            this.lastAccessTime = attr.lastAccessTime().toInstant();
            this.length = attr.size();
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

    public Instant getLastAccessTime()
    {
        return lastAccessTime;
    }

    public void setLastAccessTime( Instant lastAccessTime )
    {
        this.lastAccessTime = lastAccessTime;
    }

    public Long getLength()
    {
        return length;
    }

    public void setLength(Long length)
    {
        this.length = length;
    }

}
