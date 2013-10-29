package org.realityforge.guiceyloops.server;

import com.google.inject.matcher.AbstractMatcher;
import javax.ejb.SessionContext;
import javax.transaction.TransactionSynchronizationRegistry;
import static com.google.inject.matcher.Matchers.any;

/**
 * The client module for all the server side tests in absence of a specific module for a test.
 */
public class ServerTestModule
  extends AbstractModule
{
  private final FlushingInterceptor _interceptor;

  public ServerTestModule( final Flushable flushable )
  {
    _interceptor = new FlushingInterceptor( flushable );
  }

  @Override
  protected void configure()
  {
    bindMock( SessionContext.class );
    bindService( TransactionSynchronizationRegistry.class, TestTransactionSynchronizationRegistry.class );
  }

  protected <T> void bindService( final Class<T> service,
                                  final Class<? extends T> implementation )
  {
    bindSingleton( service, implementation );
    bindInterceptor( new AbstractMatcher<Class<?>>()
    {
      @Override
      public boolean matches( final Class<?> aClass )
      {
        return implementation == aClass;
      }
    }, any(), _interceptor );
  }
}
