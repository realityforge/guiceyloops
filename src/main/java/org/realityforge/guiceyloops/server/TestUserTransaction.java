package org.realityforge.guiceyloops.server;

import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public final class TestUserTransaction
  implements UserTransaction
{
  private final EntityManager _em;

  public TestUserTransaction( final EntityManager em )
  {
    _em = em;
  }

  @Override
  public void begin()
    throws NotSupportedException, SystemException
  {
    _em.getTransaction().begin();
  }

  @Override
  public void commit()
    throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
           IllegalStateException, SystemException
  {
    _em.getTransaction().commit();
  }

  @Override
  public void rollback()
    throws IllegalStateException, SecurityException, SystemException
  {
    _em.getTransaction().rollback();
  }

  @Override
  public void setRollbackOnly()
    throws IllegalStateException, SystemException
  {
    _em.getTransaction().setRollbackOnly();
  }

  @Override
  public int getStatus()
    throws SystemException
  {
    return _em.getTransaction().isActive() ? Status.STATUS_ACTIVE : Status.STATUS_UNKNOWN;
  }

  @Override
  public void setTransactionTimeout( final int seconds )
    throws SystemException
  {
    //Ignore
  }
}
