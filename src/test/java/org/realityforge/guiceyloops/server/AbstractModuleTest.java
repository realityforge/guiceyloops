package org.realityforge.guiceyloops.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;
import org.mockito.cglib.proxy.Factory;
import org.realityforge.guiceyloops.JEETestingModule;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

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

  static class MyServerTestModule
    extends ServerTestModule
  {
    @Override
    protected void configure()
    {
      super.configure();
      bindMock( EntityManager.class );
      bindService( Service1.class, Component1.class );
    }
  }

  @Test
  public void serverTestModule()
  {
    final Injector injector = Guice.createInjector( new MyServerTestModule(), new JEETestingModule() );
    assertTrue( injector.getInstance( SessionContext.class ) instanceof Factory );
    assertTrue( injector.getInstance( TransactionSynchronizationRegistry.class ) instanceof TestTransactionSynchronizationRegistry );

    final Service1 instance = injector.getInstance( Service1.class );
    final Component1 component1 = InjectUtil.toObject( Component1.class, instance );
    assertEquals( component1._count, 0 );
    instance.foo();
    verify( injector.getInstance( EntityManager.class ), times( 2 ) ).flush();
    assertEquals( component1._count, 1 );
  }
}