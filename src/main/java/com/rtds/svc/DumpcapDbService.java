/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rtds.svc;

import com.rtds.jpa.DumpcapProcess;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    
    public UUID createDumpCapProcess( Long pid, String path_name, Optional<String> uid )
    {
        DumpcapProcess proc = new DumpcapProcess();
        
        proc.setPid(pid);
        proc.setPathName(path_name);
        
        if( uid.isPresent() )
        {
            proc.setUid( uid.get() );
        }
        
        em.persist( proc );
        em.flush();
        em.refresh( proc );
        
        return proc.getId();
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
    
    public List<UUID> list( Optional<String> uid )
    {
        if( uid.isPresent() )
        {
            return em.createQuery( "select proc.id from DumpcapProcess proc where proc.uid = :uid", UUID.class ).
                setParameter( "uid", uid.get() ).
                getResultList();
        }
        else
        {
            return listAll();
        }
    }
    
    public List<UUID> listAll()
    {
        return em.createQuery( "select proc.id from DumpcapProcess proc", UUID.class ).
                getResultList();
    }
}
