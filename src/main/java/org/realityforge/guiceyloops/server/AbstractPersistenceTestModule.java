package org.realityforge.guiceyloops.server;

import com.google.inject.Singleton;
import javax.transaction.TransactionSynchronizationRegistry;

public abstract class AbstractPersistenceTestModule
  extends AbstractModule
{
  protected void configure()
  {
    registerTransactionSynchronizationRegistry();
  }

  protected void registerTransactionSynchronizationRegistry()
  {
    try
    {
      bind( TransactionSynchronizationRegistry.class ).
        to( TestTransactionSynchronizationRegistry.class ).
        in( Singleton.class );
    }
    catch ( final Throwable e )
    {
      //Ignored. Probably as the classes the transaction extensions are not on the classpath
    }
  }

  /**
   * @return the name of the persistence unit under test.
   */
  protected abstract String getPersistenceUnitName();
}
