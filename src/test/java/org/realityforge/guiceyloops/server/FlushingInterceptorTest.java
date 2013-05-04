package org.realityforge.guiceyloops.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javax.persistence.EntityManager;
import org.aopalliance.intercept.MethodInvocation;
import org.realityforge.guiceyloops.JEETestingModule;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

public class FlushingInterceptorTest
{
  @Test
  public void invoke()
    throws Throwable
  {
    final EntityManager entityManager = mock( EntityManager.class );
    final Injector injector =
      Guice.createInjector( new FlushingInterceptorTestModule( entityManager ),
                            new JEETestingModule() );
    final FlushingInterceptor interceptor = injector.getInstance( FlushingInterceptor.class );
    final MethodInvocation invocation = mock( MethodInvocation.class );
    interceptor.invoke( invocation );
    verify( invocation, times( 1 ) ).proceed();
    verify( entityManager, times( 2 ) ).flush();
  }
}
