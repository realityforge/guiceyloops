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
    bindDependentScope();
    bindSessionContext();
    bindPrincipal();
    bindTransactionSynchronizationRegistry();
    bindBeanManager();
  }

  protected void bindDependentScope()
  {
    bindScope( Dependent.class, Scopes.NO_SCOPE );
  }

  protected void bindSessionContext()
  {
    bindMock( SessionContext.class );
  }

  protected void bindPrincipal()
  {
    bindMock( Principal.class );
  }

  protected void bindBeanManager()
  {
    bind( BeanManager.class ).to( TestBeanManager.class ).asEagerSingleton();
  }

  protected void bindTransactionSynchronizationRegistry()
  {
    bind( TransactionSynchronizationRegistry.class ).
      to( TestTransactionSynchronizationRegistry.class ).
      asEagerSingleton();
  }
}
