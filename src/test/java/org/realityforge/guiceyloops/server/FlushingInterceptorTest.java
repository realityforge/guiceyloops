package org.realityforge.guiceyloops.server;

import org.aopalliance.intercept.MethodInvocation;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

public class FlushingInterceptorTest
{
  @Test
  public void invoke()
    throws Throwable
  {
    final Flushable flushable = mock( Flushable.class );
    final FlushingInterceptor interceptor = new FlushingInterceptor( true, flushable );
    final MethodInvocation invocation = mock( MethodInvocation.class );
    interceptor.invoke( invocation );
    verify( invocation, times( 1 ) ).proceed();
    verify( flushable, times( 2 ) ).flush();
  }

  @Test
  public void invoke_without_flushAtStart()
    throws Throwable
  {
    final Flushable flushable = mock( Flushable.class );
    final FlushingInterceptor interceptor = new FlushingInterceptor( false, flushable );
    final MethodInvocation invocation = mock( MethodInvocation.class );
    interceptor.invoke( invocation );
    verify( invocation, times( 1 ) ).proceed();
    verify( flushable, times( 1 ) ).flush();
  }
}
