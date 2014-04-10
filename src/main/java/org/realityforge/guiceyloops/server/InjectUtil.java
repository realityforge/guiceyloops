package org.realityforge.guiceyloops.server;

import java.lang.reflect.Proxy;

/**
 * A utility class for interacting with the injection system
 */
final class InjectUtil
{
  InjectUtil()
  {
  }

  @SuppressWarnings( "unchecked" )
  protected static <I, T extends I> T toObject( final Class<T> type, final I object )
  {
    final Object result;
    if ( Proxy.isProxyClass( object.getClass() ) )
    {
      final AdapterInvocationHandler handler = (AdapterInvocationHandler) Proxy.getInvocationHandler( object );
      result = handler.getImpl();
    }
    else
    {
      result = object;
    }
    if ( !type.isInstance( result ) )
    {
      throw new IllegalStateException( "Attempted to convert incompatible type to " + type );
    }
    return (T) result;
  }
}
