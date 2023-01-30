package org.realityforge.guiceyloops.shared;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;
import org.realityforge.guiceyloops.server.Flushable;
import org.realityforge.guiceyloops.server.FlushingTestModule;
import org.realityforge.guiceyloops.server.JEETestingModule;
import org.realityforge.guiceyloops.server.ServerTestModule;
import org.realityforge.guiceyloops.server.TestTransactionSynchronizationRegistry;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class AbstractModuleTest
{
  public interface Service1
  {
    void foo();
  }

  public interface Service2
  {
  }

  public interface Service3
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

  public static class Component4
    implements Service3
  {
  }

  public static class Component5
  {
  }

  static class TestModule
    extends AbstractModule
  {
    @Override
    protected void configure()
    {
      bindMock( Runnable.class );
      bindMock( Runnable.class, "MySpecialRunner" );
      bindResource( String.class, "MyKey", "MyValue" );
      bindSingleton( Service1.class, Component1.class );
      multiBind( Component2.class, Service2.class, Service3.class );
      bindSingleton( "X", Service3.class, Component4.class );
      bindSingleton( Component5.class );
    }
  }

  @Test
  public void basicBindings()
  {
    final Injector injector = Guice.createInjector( new TestModule() );

    final Runnable m1 = injector.getInstance( Runnable.class );
    final Runnable m2 = injector.getInstance( Runnable.class );
    assertEquals( m1, m2 );
    assertTrue( m1 instanceof org.mockito.internal.creation.bytebuddy.MockAccess );

    final Provider<Runnable> runnerProvider =
      injector.getProvider( Key.get( Runnable.class, Names.named( "MySpecialRunner" ) ) );
    assertTrue( runnerProvider.get() instanceof org.mockito.internal.creation.bytebuddy.MockAccess );

    final Provider<String> provider =
      injector.getProvider( Key.get( String.class, Names.named( "MyKey" ) ) );
    assertEquals( provider.get(), "MyValue" );

    //singleton
    assertSame( injector.getInstance( Service1.class ), injector.getInstance( Service1.class ) );

    assertSame( injector.getInstance( Component5.class ), injector.getInstance( Component5.class ) );
    assertSame( injector.getInstance( Component5.class ).getClass(), Component5.class );

    assertEquals( provider.get(), "MyValue" );
    assertEquals( injector.getInstance( Key.get( Service3.class, Names.named( "X" ) ) ),
                  injector.getInstance( Key.get( Service3.class, Names.named( "X" ) ) ) );

    //Multibinding tests
    assertSame( injector.getInstance( Service2.class ), injector.getInstance( Service2.class ) );
    assertNotSame( injector.getInstance( Service2.class ), injector.getInstance( Service3.class ) );
    assertSame( InjectUtil.toObject( Component2.class, injector.getInstance( Service2.class ) ),
                InjectUtil.toObject( Component2.class, injector.getInstance( Service3.class ) ) );

    //Straight per request
    assertNotSame( injector.getInstance( Component3.class ), injector.getInstance( Component3.class ) );
  }

  static class MyServerTestModule
    extends FlushingTestModule
  {
    MyServerTestModule( final Flushable flushable )
    {
      super( flushable );
    }

    @Override
    protected void configure()
    {
      bindMock( EntityManager.class );
      bindService( Service1.class, Component1.class );
      bindService( Service2.class.getName(), Component2.class.getName() );
    }
  }

  @Test
  public void serverTestModule()
  {
    final Flushable flushable = mock( Flushable.class );
    final Injector injector =
      Guice.createInjector( new MyServerTestModule( flushable ), new JEETestingModule(), new ServerTestModule() );
    assertTrue( injector.getInstance( SessionContext.class ) instanceof org.mockito.internal.creation.bytebuddy.MockAccess );
    assertTrue( injector.getInstance(
      TransactionSynchronizationRegistry.class ) instanceof TestTransactionSynchronizationRegistry );

    final Service1 instance = injector.getInstance( Service1.class );
    final Component1 component1 = InjectUtil.toObject( Component1.class, instance );
    assertEquals( component1._count, 0 );
    instance.foo();
    verify( flushable, times( 2 ) ).flush();
    assertEquals( component1._count, 1 );

    final Service2 instance2 = injector.getInstance( Service2.class );
    final Component2 component2 = InjectUtil.toObject( Component2.class, instance2 );
    assertNotNull( component2 );
  }
}
