package org.realityforge.guiceyloops.server;

import com.google.inject.Module;
import com.google.inject.name.Names;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.TransactionSynchronizationRegistry;
import org.realityforge.guiceyloops.shared.AbstractModule;
import org.realityforge.guiceyloops.shared.ValueUtil;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@SuppressWarnings( "WeakerAccess" )
public class AbstractServerTestTest
{
  public static final String SECONDARY_UNIT_NAME = ValueUtil.randomString();

  public interface Service1
  {
  }

  public interface Service2
  {
  }

  public static class Component1
    implements Service1, Service2
  {
  }

  public static class MyServerTest
    extends AbstractServerTest
  {
    EntityManager _entityManager;
    DbCleaner _dbCleaner;
    private final String _persistenceUnit;
    private final boolean _registerCleaner;
    private EntityTransaction _transaction;

    MyServerTest()
    {
      this( null );
    }

    MyServerTest( final String persistenceUnit )
    {
      this( persistenceUnit, false );
    }

    MyServerTest( final String persistenceUnit, final boolean registerCleaner )
    {
      this( persistenceUnit, registerCleaner, true, false );
    }

    MyServerTest( final String persistenceUnit,
                  final boolean registerCleaner,
                  final boolean inActiveTransaction,
                  final boolean inRollback )
    {
      _persistenceUnit = persistenceUnit;
      _registerCleaner = registerCleaner;
      _entityManager = mock( EntityManager.class );
      _dbCleaner = mock( DbCleaner.class );
      _transaction = mock( EntityTransaction.class );
      when( _entityManager.getTransaction() ).thenReturn( _transaction );
      when( _transaction.isActive() ).thenReturn( inActiveTransaction );
      when( _transaction.getRollbackOnly() ).thenReturn( inRollback );
    }

    @SuppressWarnings( "unchecked" )
    protected <T> T getInstance( final Class<T> type )
    {
      if ( EntityManager.class == type )
      {
        return (T) _entityManager;
      }
      else
      {
        return super.getInstance( type );
      }
    }

    @SuppressWarnings( "unchecked" )
    protected <T> T getInstance( final String name, final Class<T> type )
    {
      if ( EntityManager.class == type && SECONDARY_UNIT_NAME.equals( name ) )
      {
        return (T) _entityManager;
      }
      else
      {
        return super.getInstance( name, type );
      }
    }

    @Nullable
    @Override
    protected String getPrimaryPersistenceUnitName()
    {
      return _persistenceUnit;
    }

    protected Module[] getModules()
    {
      final ArrayList<Module> modules = new ArrayList<>();
      modules.addAll( Arrays.asList( super.getModules() ) );
      addModule( modules, null );
      return modules.toArray( new Module[ modules.size() ] );
    }

    @SuppressWarnings( "deprecation" )
    @Override
    protected Module getEntityModule()
    {
      return new AbstractModule()
      {
        @Override
        protected void configure()
        {
          if ( null == _persistenceUnit )
          {
            bind( EntityManager.class ).toInstance( _entityManager );
          }
          else
          {
            bind( EntityManager.class ).annotatedWith( Names.named( _persistenceUnit ) ).toInstance( _entityManager );
          }
          if ( _registerCleaner )
          {
            bind( DbCleaner.class ).toInstance( _dbCleaner );
          }
          multiBind( Component1.class, Service1.class, Service2.class );
        }
      };
    }

    @Nonnull
    @Override
    protected String getPrimaryJmsConnectionFactoryName()
    {
      return "";
    }

    @Nonnull
    @Override
    protected String getPrimaryBrokerName()
    {
      return "";
    }
  }

  @Test
  public void dbCleanerInteraction()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( null, true );
    test.preTest();
    verify( test._dbCleaner ).start();

    test.postTest();
    verify( test._dbCleaner ).finish();
  }

  @Test
  public void entityManagerInteraction()
    throws Exception
  {
    final MyServerTest test = new MyServerTest();
    test.preTest();

    test.flush();
    verify( test._entityManager ).flush();

    test.clear();
    verify( test._entityManager ).clear();

    final Object entity = new Object();
    test.refresh( entity );
    verify( test._entityManager ).refresh( entity );
  }

  @Test
  public void resetTransactionSynchronizationRegistry()
    throws Exception
  {
    final MyServerTest test = new MyServerTest();
    test.preTest();

    test.s( TransactionSynchronizationRegistry.class ).putResource( "key", "value" );
    test.resetTransactionSynchronizationRegistry();
    assertNull( test.s( TransactionSynchronizationRegistry.class ).getResource( "key" ) );
  }

  @Test
  public void serverTest_withoutDbCleaner()
    throws Exception
  {
    final MyServerTest test = new MyServerTest();
    test.preTest();
    verify( test._dbCleaner, never() ).start();

    test.postTest();
    verify( test._dbCleaner, never() ).finish();
  }

  @Test
  public void serverTest_withNamedPersistenceUnit()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( "MyUnit" );
    test.preTest();

    test.flush();
    verify( test._entityManager ).flush();

    test.clear();
    verify( test._entityManager ).clear();

    final Object entity = new Object();
    test.refresh( entity );
    verify( test._entityManager ).refresh( entity );

    test.s( TransactionSynchronizationRegistry.class ).putResource( "key", "value" );
    test.resetTransactionSynchronizationRegistry();
    assertNull( test.s( TransactionSynchronizationRegistry.class ).getResource( "key" ) );

    test.postTest();
    verify( test._dbCleaner, never() ).finish();
  }

  @Test
  public void usesTransaction()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( null, true );
    test.preTest();
    verify( test._dbCleaner, times( 1 ) ).start();

    test.usesTransaction();
    verify( test._dbCleaner, times( 1 ) ).usesTransaction();
  }

  @Test
  public void inTransaction_Runnable()
    throws Exception
  {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final MyServerTest test = new MyServerTest( null, false );
    test.inTransaction( latch::countDown );

    verify( test._transaction, times( 1 ) ).begin();
    verify( test._transaction, times( 1 ) ).commit();

    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void inTransaction_Runnable_named()
    throws Exception
  {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final MyServerTest test = new MyServerTest( null, false );
    test.inTransaction( SECONDARY_UNIT_NAME, latch::countDown );

    verify( test._transaction, times( 1 ) ).begin();
    verify( test._transaction, times( 1 ) ).commit();

    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void inTransaction_Callable()
    throws Exception
  {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final MyServerTest test = new MyServerTest( null, false );
    final Integer result =
      test.inTransaction( () ->
                          {
                            latch.countDown();
                            return 2;
                          } );

    assertEquals( result, (Integer) 2 );

    verify( test._transaction, times( 1 ) ).begin();
    verify( test._transaction, times( 1 ) ).commit();

    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void inTransaction_Callable_named()
    throws Exception
  {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final MyServerTest test = new MyServerTest( null, false );
    final Integer result =
      test.inTransaction( SECONDARY_UNIT_NAME,
                          () ->
                          {
                            latch.countDown();
                            return 2;
                          } );

    assertEquals( result, (Integer) 2 );

    verify( test._transaction, times( 1 ) ).begin();
    verify( test._transaction, times( 1 ) ).commit();

    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void tran_Runnable()
    throws Exception
  {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final MyServerTest test = new MyServerTest( null, false );
    test.tran( latch::countDown );

    verify( test._transaction, times( 1 ) ).begin();
    verify( test._transaction, times( 1 ) ).commit();

    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void tran_Runnable_named()
    throws Exception
  {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final MyServerTest test = new MyServerTest( null, false );
    test.tran( SECONDARY_UNIT_NAME, latch::countDown );

    verify( test._transaction, times( 1 ) ).begin();
    verify( test._transaction, times( 1 ) ).commit();

    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void tran_Callable()
    throws Exception
  {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final MyServerTest test = new MyServerTest( null, false );
    final Integer result =
      test.tran( () ->
                 {
                   latch.countDown();
                   return 2;
                 } );

    assertEquals( result, (Integer) 2 );

    verify( test._transaction, times( 1 ) ).begin();
    verify( test._transaction, times( 1 ) ).commit();

    assertEquals( latch.getCount(), 0 );
  }

  @Test
  public void tran_Callable_named()
    throws Exception
  {
    final CountDownLatch latch = new CountDownLatch( 1 );
    final MyServerTest test = new MyServerTest( null, false );
    final Integer result =
      test.tran( SECONDARY_UNIT_NAME,
                 () ->
                 {
                   latch.countDown();
                   return 2;
                 } );

    assertEquals( result, (Integer) 2 );

    verify( test._transaction, times( 1 ) ).begin();
    verify( test._transaction, times( 1 ) ).commit();

    assertEquals( latch.getCount(), 0 );
  }
}
