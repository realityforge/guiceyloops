package org.realityforge.guiceyloops.server;

import com.google.inject.matcher.AbstractMatcher;
import javax.ejb.SessionContext;
import static com.google.inject.matcher.Matchers.any;

/**
 * The client module for all the server side tests in absence of a specific module for a test.
 */
public class ServerTestModule
  extends AbstractModule
{
  private final FlushingInterceptor _interceptor = new FlushingInterceptor();

  @Override
  protected void configure()
  {
    requestInjection( _interceptor );
    bindMock( SessionContext.class );
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
