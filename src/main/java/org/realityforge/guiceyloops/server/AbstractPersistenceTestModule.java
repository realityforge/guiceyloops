package org.realityforge.guiceyloops.server;

import com.google.inject.Singleton;
import javax.transaction.TransactionSynchronizationRegistry;
import org.realityforge.guiceyloops.shared.AbstractModule;

public abstract class AbstractPersistenceTestModule
  extends AbstractModule
{
  protected void configure()
  {
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
