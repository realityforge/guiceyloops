package org.realityforge.guiceyloops.server;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * An interceptor that flushes the entity manager before and after a method under test
 * so that getID() and other generated values are guaranteed to exist prior to being
 * used in method. It also ensures all data has hit the database after the test so we
 * can do queries against database state and verify behaviour of service.
 */
public class FlushingInterceptor
  implements MethodInterceptor
{
  @PersistenceContext
  private EntityManager _em;

  @Override
  public Object invoke( final MethodInvocation invocation )
    throws Throwable
  {
    _em.flush();
    try
    {
      return invocation.proceed();
    }
    finally
    {
      _em.flush();
    }
  }
}
