package org.realityforge.guiceyloops.server;

import com.google.inject.Scopes;
import javax.ejb.SessionContext;
import javax.enterprise.context.Dependent;
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
    bindScope( Dependent.class, Scopes.NO_SCOPE );
    bindMock( SessionContext.class );
    bind( TransactionSynchronizationRegistry.class ).
      to( TestTransactionSynchronizationRegistry.class ).
      asEagerSingleton();
  }
}
