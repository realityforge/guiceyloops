package org.realityforge.guiceyloops.server;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * An interceptor that flushes the entity manager before and after a method under test
 * so that getID() and other generated values are guaranteed to exist prior to being
 * used in method. It also ensures all data has hit the database after the test so we
 * can do queries against database state and verify behaviour of service.
 */
final class FlushingInterceptor
  implements MethodInterceptor
{
  private final Flushable _flushable;

  FlushingInterceptor( final Flushable flushable )
  {
    _flushable = flushable;
  }

  @Override
  public Object invoke( final MethodInvocation invocation )
    throws Throwable
  {
    _flushable.flush();
    try
    {
      return invocation.proceed();
    }
    finally
    {
      _flushable.flush();
    }
  }
}
