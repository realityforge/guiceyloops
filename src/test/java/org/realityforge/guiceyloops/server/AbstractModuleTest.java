package org.realityforge.guiceyloops.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import org.mockito.Mockito;
import org.mockito.cglib.proxy.Factory;
import org.testng.annotations.Test;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class AbstractModuleTest
{
  public static interface Service1
  {
    void foo();
  }

  public static interface Service2
  {
  }

  public static interface Service3
  {
  }

  public static class Component1
    implements Service1
  {
    int _count;

    @Override
    public void foo()
    {
      _count++;
    }
  }

  public static class Component2
    implements Service2, Service3
  {
  }

  public static class Component3
  {
  }

  static class TestModule
    extends AbstractModule
  {
    @Override
    protected void configure()
    {
      bindMock( Runnable.class );
      bindResource( String.class, "MyKey", "MyValue" );
      bindSingleton( Service1.class, Component1.class );
      multiBind( Component2.class, Service2.class, Service3.class );
    }
  }

  @Test
  public void basicBindings()
  {
    final Injector injector = Guice.createInjector( new TestModule() );

    final Runnable m1 = injector.getInstance( Runnable.class );
    final Runnable m2 = injector.getInstance( Runnable.class );
    assertEquals( m1, m2 );
    assertTrue( m1 instanceof Factory );

    final Provider<String> provider =
      injector.getProvider( Key.get( String.class, Names.named( "MyKey" ) ) );
    assertEquals( provider.get(), "MyValue" );

    //singleton
    assertSame( injector.getInstance( Service1.class ), injector.getInstance( Service1.class ) );

    //Multibinding tests
    assertSame( injector.getInstance( Service2.class ), injector.getInstance( Service2.class ) );
    assertNotSame( injector.getInstance( Service2.class ), injector.getInstance( Service3.class ) );
    assertSame( InjectUtil.toObject( Component2.class, injector.getInstance( Service2.class ) ),
                InjectUtil.toObject( Component2.class, injector.getInstance( Service3.class ) ) );

    //Straight per request
    assertNotSame( injector.getInstance( Component3.class ), injector.getInstance( Component3.class ) );
  }
}
