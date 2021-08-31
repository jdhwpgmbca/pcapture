/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.svc;

import com.rtds.view.DumpcapProcessDefaultView;
import com.rtds.view.DumpcapProcessUserView;
import com.rtds.jpa.DumpcapProcess;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
    
    public UUID createDumpcapProcess(
            @NotNull Long pid,
            @NotBlank @Size( min=1, max=1024 ) String path_name,
            @NotBlank @Size( min=1, max=10 ) String type,
            @NotNull Optional<String> uid )
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
    
    public void stopDumpcapProcess(
            @NotNull UUID id,
            @NotNull Optional<String> uid )
    {
        DumpcapProcess proc = em.find( DumpcapProcess.class, id );
        
        if( uid.isPresent() && proc != null && uid.get().equals( proc.getUid() ) )
        {
            proc.setStatus( "stopped" );
        }
        else if( uid.isEmpty() && proc != null )
        {
            proc.setStatus( "stopped" );
        }
    }
    
    public void deleteDumpcapProcess(
            @NotNull UUID id,
            @NotNull Optional<String> uid )
    {
        DumpcapProcess proc = em.find( DumpcapProcess.class, id );
        
        if( uid.isPresent() && proc != null && uid.get().equals( proc.getUid() ) )
        {
            em.remove( proc );
        }
        else if( uid.isEmpty() && proc != null )
        {
            em.remove( proc );
        }
    }
    
    public DumpcapProcess find(
            @NotNull UUID id,
            @NotNull Optional<String> uid )
    {
        DumpcapProcess proc = em.find( DumpcapProcess.class, id );
        
        if( uid.isPresent() && proc != null && uid.get().equals( proc.getUid() ) )
        {
            return proc;
        }
        else if( uid.isEmpty() )
        {
            return proc;
        }
        
        return null;
    }
    
    public List<DumpcapProcessDefaultView> list(
            @NotNull Optional<String> uid )
    {
        if( uid.isPresent() )
        {
            return em.createQuery( "select proc from DumpcapProcess proc where proc.uid = :uid", DumpcapProcess.class )
                    .setParameter( "uid", uid.get() )
                    .getResultList()
                    .stream()
                    .map( p -> new DumpcapProcessDefaultView( p ) )
                    .filter( view -> !view.getStatus().equals( "deleted" ) )
                    .collect( Collectors.toList() );
        }
        else
        {
            return listAll();
        }
    }
    
    public List<DumpcapProcessDefaultView> listAll()
    {
        return em.createQuery( "select proc from DumpcapProcess proc", DumpcapProcess.class ).
                getResultList().stream().map(p -> new DumpcapProcessUserView( p ) ).collect( Collectors.toList() );
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
    
}
