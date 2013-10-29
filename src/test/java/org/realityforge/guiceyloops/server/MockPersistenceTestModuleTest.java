package org.realityforge.guiceyloops.server;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;
import org.mockito.cglib.proxy.Factory;
import org.realityforge.guiceyloops.JEETestingModule;
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
    assertNotNull( entityManager );
    assertTrue( entityManager instanceof Factory );

    final TransactionSynchronizationRegistry registry =
      injector.getInstance( TransactionSynchronizationRegistry.class );
    assertTrue( registry instanceof TestTransactionSynchronizationRegistry );

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
