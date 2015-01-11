package org.realityforge.guiceyloops.server;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import javax.persistence.EntityManager;
import org.mockito.cglib.proxy.Factory;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class MockPersistenceTestModuleTest
{
  @Test
  public void basicOperation()
    throws Throwable
  {
    final Injector injector =
      Guice.createInjector( new MockPersistenceTestModule( "TestUnit" ), new JEETestingModule() );

    final EntityManager entityManager =
      injector.getInstance( Key.get( EntityManager.class, Names.named( "TestUnit" ) ) );
    assertMockEntityManager( entityManager );

    assertNoDbCleaner( injector );
  }

  private void assertMockEntityManager( final EntityManager entityManager )
  {
    assertNotNull( entityManager );
    assertTrue( entityManager instanceof Factory );
  }

  private void assertNoDbCleaner( final Injector injector )
  {
    try
    {
      injector.getInstance( DbCleaner.class );
      fail( "Incorrectly able to lookup DbCleaner" );
    }
    catch ( final ConfigurationException ce )
    {
      //Expected to not be present
    }
  }
}
