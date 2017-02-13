package org.realityforge.guiceyloops.server;

import java.util.HashMap;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

public class TestTransactionSynchronizationRegistry
  implements TransactionSynchronizationRegistry
{
  private final HashMap<Object, Object> _resources = new HashMap<>();
  private boolean _rollbackOnly;
  private Integer _transactionStatus;

  public Object getTransactionKey()
  {
    throw new UnsupportedOperationException();
  }

  public void putResource( final Object key, final Object value )
  {
    _resources.put( key, value );
  }

  public Object getResource( final Object key )
  {
    return _resources.get( key );
  }

  public void clear()
  {
    _resources.clear();
  }

  @Override
  public void registerInterposedSynchronization( final Synchronization sync )
  {
    throw new UnsupportedOperationException();
  }

  public void setTransactionStatus( final int transactionStatus )
  {
    _transactionStatus = transactionStatus;
  }

  @Override
  public int getTransactionStatus()
  {
    if ( null != _transactionStatus )
    {
      return _transactionStatus;
    }
    else if ( _rollbackOnly )
    {
      return Status.STATUS_MARKED_ROLLBACK;
    }
    else
    {
      return Status.STATUS_ACTIVE;
    }
  }

  @Override
  public void setRollbackOnly()
  {
    _rollbackOnly = true;
  }

  @Override
  public boolean getRollbackOnly()
  {
    return _rollbackOnly;
  }
}
