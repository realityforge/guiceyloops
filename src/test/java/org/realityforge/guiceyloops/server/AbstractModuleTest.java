package org.realityforge.guiceyloops.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import org.mockito.cglib.proxy.Factory;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class AbstractModuleTest
{
  public static interface S1
  {
  }

  public static interface S2
  {
  }

  public static interface S3
  {
  }

  public static class I1
    implements S1
  {
  }

  public static class I2
    implements S2, S3
  {
  }

  public static class I3
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
      bindSingleton( S1.class, I1.class );
      multiBind( I2.class, S2.class, S3.class );
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
    assertSame( injector.getInstance( S1.class ), injector.getInstance( S1.class ) );

    //Multibinding tests
    assertSame( injector.getInstance( S2.class ), injector.getInstance( S2.class ) );
    assertNotSame( injector.getInstance( S2.class ), injector.getInstance( S3.class ) );
    assertSame( InjectUtil.toObject( I2.class, injector.getInstance( S2.class ) ),
                InjectUtil.toObject( I2.class, injector.getInstance( S3.class ) ) );

    //Straight per request
    assertNotSame( injector.getInstance( I3.class ), injector.getInstance( I3.class ) );
  }
}
