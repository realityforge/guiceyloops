package org.realityforge.guiceyloops.server;

import com.google.inject.matcher.AbstractMatcher;
import javax.annotation.Nonnull;
import org.realityforge.guiceyloops.shared.AbstractModule;
import static com.google.inject.matcher.Matchers.any;

/**
 * The module that provides a will flush before and after invoking the service methods.
 */
public abstract class FlushingTestModule
  extends AbstractModule
{
  private final FlushingInterceptor _interceptor;

  public FlushingTestModule( final Flushable flushable )
  {
    this( true, flushable );
  }

  public FlushingTestModule( final boolean flushAtStart, final Flushable flushable )
  {
    _interceptor = new FlushingInterceptor( flushAtStart, flushable );
  }

  protected final void bindService( final String interfaceName, final String implementationName )
  {
    try
    {
      final Class<?> interfaceClass = Class.forName( interfaceName );
      final Class implClass = Class.forName( implementationName );
      bindService( interfaceClass, implClass );
    }
    catch ( final ClassNotFoundException e )
    {
      final String message =
        "Error attempting to define service with interface: " + interfaceName +
        " and implementation " + implementationName;
      throw new IllegalStateException( message, e );
    }
  }

  protected final <T> void bindService( @Nonnull final Class<T> service,
                                        @Nonnull final Class<? extends T> implementation )
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
