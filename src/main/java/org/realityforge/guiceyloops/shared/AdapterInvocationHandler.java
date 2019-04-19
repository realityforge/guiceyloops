package org.realityforge.guiceyloops.shared;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * A simple handler that wraps an object.
 */
class AdapterInvocationHandler
  implements InvocationHandler
{
  private final Object _impl;

  public AdapterInvocationHandler( final Object impl )
  {
    _impl = impl;
  }

  Object getImpl()
  {
    return _impl;
  }

  @Override
  public Object invoke( final Object proxy, final Method method, final Object[] args )
    throws Throwable
  {
    return method.invoke( _impl, args );
  }
}
