package org.realityforge.guiceyloops.shared;

import com.google.inject.name.Names;
import java.lang.reflect.Proxy;
import javax.inject.Singleton;
import org.mockito.Mockito;

public abstract class AbstractModule
  extends com.google.inject.AbstractModule
{
  @SuppressWarnings("unchecked")
  protected final void multiBind( final Class implementation, final Class... interfaces )
  {
    final Object impl = instantiate( implementation );
    requestInjection( impl );
    bind( interfaces[ 0 ] ).toInstance( impl );

    for ( int i = 1; i < interfaces.length; i++ )
    {
      final ClassLoader classLoader = getClass().getClassLoader();
      final Class type = interfaces[ i ];
      bind( type ).
        toInstance( Proxy.newProxyInstance( classLoader,
                                            new Class[]{ type },
                                            new AdapterInvocationHandler( impl ) ) );
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

  protected final <T> T bindMock( final Class<T> type )
  {
    final T mock = Mockito.mock( type );
    bind( type ).toInstance( mock );
    return mock;
  }

  protected final <T> T bindMock( final Class<T> type, final String name )
  {
    final T mock = Mockito.mock( type );
    bind( type ).annotatedWith( Names.named( name ) ).toInstance( mock );
    return mock;
  }

  protected final <T> void bindResource( final Class<T> resultType, final String name, final T instance )
  {
    bind( resultType ).annotatedWith( Names.named( name ) ).toInstance( instance );
  }

  protected final <T> void bindSingleton( final Class<T> service, final Class<? extends T> implementation )
  {
    bind( service ).to( implementation ).in( Singleton.class );
  }

  protected final <T> void bindSingleton( final Class<? extends T> implementation )
  {
    bind( implementation ).in( Singleton.class );
  }

  protected <T> void bindSingleton( final String name,
                                    final Class<T> service,
                                    final Class<? extends T> implementation )
  {
    bind( service ).annotatedWith( Names.named( name ) ).to( implementation ).in( Singleton.class );
  }
}
