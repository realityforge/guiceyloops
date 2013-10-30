package org.realityforge.guiceyloops.server;

import com.google.inject.Singleton;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

public abstract class AbstractPersistenceTestModule
  extends AbstractModule
{
  protected void configure()
  {
    registerTransactionSynchronizationRegistry();
    registerUserTransaction();
  }

  protected void registerUserTransaction()
  {
    bindMock( UserTransaction.class );
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
