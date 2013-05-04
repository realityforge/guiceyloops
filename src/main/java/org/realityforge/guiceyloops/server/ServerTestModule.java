package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.ejb.SessionContext;
import javax.inject.Singleton;
import org.mockito.Mockito;
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

  @SuppressWarnings( "unchecked" )
  protected void multiBind( final Class implementation, final Class... interfaces )
  {
    final Object impl = instantiate( implementation );
    requestInjection( impl );
    bind( interfaces[ 0 ] ).toInstance( impl );

    for ( int i = 1; i < interfaces.length; i++ )
    {
      final ClassLoader classLoader = getClass().getClassLoader();
      final Class type = interfaces[ i ];
      final InvocationHandler handler = new InvocationHandler()
      {
        @Override
        public Object invoke( final Object proxy, final Method method, final Object[] args )
          throws Throwable
        {
          return method.invoke( impl, args );
        }
      };
      bind( type ).toInstance( Proxy.newProxyInstance( classLoader, new Class[] { type }, handler ) );
    }
  }

  private Object instantiate( final Class implementation )
  {
    try
    {
      return implementation.newInstance();
    }
    catch ( final Throwable throwable )
    {
      throw new IllegalStateException( throwable.getMessage(), throwable );
    }
  }

  protected <T> void bindMock( final Class<T> type )
  {
    bind( type ).toInstance( Mockito.mock( type ) );
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

  protected <T> void bindSingleton( final Class<T> service, final Class<? extends T> implementation )
  {
    bind( service ).to( implementation ).in( Singleton.class );
  }
}
