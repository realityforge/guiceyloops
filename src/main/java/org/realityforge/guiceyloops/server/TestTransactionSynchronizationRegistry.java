package org.realityforge.guiceyloops.server;

import java.util.HashMap;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

public class TestTransactionSynchronizationRegistry
  implements TransactionSynchronizationRegistry
{
  private final HashMap<Object, Object> _resources = new HashMap<Object, Object>();

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

  @Override
  public int getTransactionStatus()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setRollbackOnly()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getRollbackOnly()
  {
    return false;
  }
}
