package org.realityforge.guiceyloops.server;

import com.google.inject.Scopes;
import javax.ejb.SessionContext;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.security.Principal;

/**
 * The client module for all the server side tests in absence of a specific module for a test.
 */
public final class ServerTestModule
  extends org.realityforge.guiceyloops.shared.AbstractModule
{
  @Override
  protected void configure()
  {
    bindScope( Dependent.class, Scopes.NO_SCOPE );
    bindMock( SessionContext.class );
    bindMock( Principal.class );
    bind( TransactionSynchronizationRegistry.class ).
      to( TestTransactionSynchronizationRegistry.class ).
      asEagerSingleton();
    bind( BeanManager.class ).to( TestBeanManager.class ).asEagerSingleton();
  }
}
