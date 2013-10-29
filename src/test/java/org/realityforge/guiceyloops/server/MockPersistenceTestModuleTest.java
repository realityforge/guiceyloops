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
      Guice.createInjector( new MockPersistenceTestModule( false, "TestUnit" ), new JEETestingModule() );

    final EntityManager entityManager =
      injector.getInstance( Key.get( EntityManager.class, Names.named( "TestUnit" ) ) );
    assertMockEntityManager( entityManager );

    assertTransactionSynchronizationRegistryPresent( injector );
    assertNoDbCleaner( injector );
  }

  @Test
  public void basicOperation_withBindUnnamed()
    throws Throwable
  {
    final Injector injector =
      Guice.createInjector( new MockPersistenceTestModule( true, "TestUnit" ), new JEETestingModule() );

    final EntityManager entityManager = injector.getInstance( EntityManager.class );

    assertMockEntityManager( entityManager );
    assertTransactionSynchronizationRegistryPresent( injector );
    assertNoDbCleaner( injector );
  }

  @Test
  public void basicOperation_withNullPersistenceUnitName()
    throws Throwable
  {
    final Injector injector =
      Guice.createInjector( new MockPersistenceTestModule( true, null ), new JEETestingModule() );

    assertMockEntityManager( injector.getInstance( EntityManager.class ) );
    assertTransactionSynchronizationRegistryPresent( injector );
    assertNoDbCleaner( injector );

    try
    {
      injector.getInstance( Key.get( EntityManager.class, Names.named( "TestUnit" ) ) );
      fail("Unexpected got named persistence unit");
    }
    catch ( final ConfigurationException ce )
    {
      //Expected
    }
  }

  private void assertTransactionSynchronizationRegistryPresent( final Injector injector )
  {
    final TransactionSynchronizationRegistry registry =
      injector.getInstance( TransactionSynchronizationRegistry.class );
    assertTrue( registry instanceof TestTransactionSynchronizationRegistry );
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
