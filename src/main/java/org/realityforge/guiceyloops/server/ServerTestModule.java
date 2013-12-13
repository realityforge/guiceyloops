package org.realityforge.guiceyloops.server;

import javax.ejb.SessionContext;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * The client module for all the server side tests in absence of a specific module for a test.
 */
public class ServerTestModule
  extends FlushingTestModule
{
  public ServerTestModule( final Flushable flushable )
  {
    super( true, flushable );
  }

  @Override
  protected void configure()
  {
    bindMock( SessionContext.class );
    bind( TransactionSynchronizationRegistry.class ).
      to( TestTransactionSynchronizationRegistry.class ).
      asEagerSingleton();
  }
}
