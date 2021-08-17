/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.svc;

import com.rtds.dto.DumpcapProcessDto;
import com.rtds.jpa.DumpcapProcess;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

/**
 *
 * @author jdh
 */
@ApplicationScoped
@Transactional
public class DumpcapDbService
{
    @Inject
    EntityManager em;
    
    public UUID createDumpcapProcess( Long pid, String path_name, String type, Optional<String> uid )
    {
        DumpcapProcess proc = new DumpcapProcess();
        
        proc.setPid(pid);
        proc.setPathName(path_name);
        proc.setStatus( "running" );
        proc.setType( type );
        
        if( uid.isPresent() )
        {
            proc.setUid( uid.get() );
        }
        
        em.persist( proc );
        em.flush();
        em.refresh( proc );
        
        return proc.getId();
    }
    
    public void stopDumpcapProcess( UUID id, Optional<String> uid )
    {
        DumpcapProcess proc = em.find( DumpcapProcess.class, id );
        
        if( proc != null && proc.getUid() != null && uid.isPresent() && uid.get().equals( proc.getUid() ) )
        {
            proc.setStatus( "stopped" );
        }
        else if( proc != null && proc.getUid() == null && uid.isEmpty() )
        {
            proc.setStatus( "stopped" );
        }
    }
    
    public void deleteDumpcapProcess( UUID id, Optional<String> uid )
    {
        DumpcapProcess proc = em.find( DumpcapProcess.class, id );
        
        if( proc != null && proc.getUid() != null && uid.isPresent() && uid.get().equals( proc.getUid() ) )
        {
            em.remove( proc );
        }
        else if( proc != null && proc.getUid() == null && uid.isEmpty() )
        {
            em.remove( proc );
        }
    }
    
    public DumpcapProcess find( UUID id, Optional<String> uid )
    {
        DumpcapProcess proc = em.find( DumpcapProcess.class, id );
        
        if( proc != null && proc.getUid() != null && uid.isPresent() && uid.get().equals( proc.getUid() ) )
        {
            return proc;
        }
        else if( proc != null && proc.getUid() == null && uid.isEmpty() )
        {
            return proc;
        }
        
        return null;
    }
    
    public List<DumpcapProcessDto> list( Optional<String> uid )
    {
        if( uid.isPresent() )
        {
            return em.createQuery( "select proc from DumpcapProcess proc where proc.uid = :uid", DumpcapProcess.class )
                    .setParameter( "uid", uid.get() )
                    .getResultList()
                    .stream()
                    .map( p -> new DumpcapProcessDto( p ) )
                    .filter( dto -> !dto.getStatus().equals( "deleted" ) )
                    .collect( Collectors.toList() );
        }
        else
        {
            return listAll();
        }
    }
    
    public void removeDeletedFilesFromDb()
    {
            List<DumpcapProcess> deleted_list = em.createQuery( "select proc from DumpcapProcess proc", DumpcapProcess.class )
                    .getResultList()
                    .stream()
                    .filter( proc -> !new File( proc.getPathName() ).exists() )
                    .collect( Collectors.toList() );
            
            deleted_list.forEach( proc -> em.remove( proc ) );
    }
    
    public List<DumpcapProcessDto> listAll()
    {
        return em.createQuery( "select proc from DumpcapProcess proc", DumpcapProcess.class ).
                getResultList().stream().map( p -> new DumpcapProcessDto( p ) ).collect( Collectors.toList() );
    }

}
