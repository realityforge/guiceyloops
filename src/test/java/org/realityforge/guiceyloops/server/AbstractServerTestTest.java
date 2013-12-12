package org.realityforge.guiceyloops.server;

import com.google.inject.Module;
import com.google.inject.name.Names;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class AbstractServerTestTest
{
  public static interface Service1
  {
  }

  public static interface Service2
  {
  }

  public static class Component1
    implements Service1, Service2
  {
    int _myField;
  }

  public static class MyServerTest
    extends AbstractServerTest
  {
    EntityManager _entityManager = Mockito.mock( EntityManager.class );
    DbCleaner _dbCleaner = Mockito.mock( DbCleaner.class );
    private final String _persistenceUnit;
    private final boolean _registerCleaner;

    public MyServerTest( final String persistenceUnit, final boolean registerCleaner )
    {
      _persistenceUnit = persistenceUnit;
      _registerCleaner = registerCleaner;
    }

    @Nullable
    @Override
    protected String getPrimaryPersistenceUnitName()
    {
      return _persistenceUnit;
    }

    protected Module[] getModules()
    {
      final ArrayList<Module> modules = new ArrayList<Module>();
      modules.addAll( Arrays.asList( super.getModules() ) );
      addModule( modules, null );
      return modules.toArray( new Module[ modules.size() ] );
    }

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
  public void postTestNullsInjector()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( null, false );
    test.preTest();
    assertNotNull( test.getInjector() );
    test.postTest();
    assertNull( test.getInjector() );
  }

  @Test
  public void entityManagerInteraction()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( null, false );
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
  public void toObject()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( null, false );
    test.preTest();

    assertEquals( test.toObject( Component1.class, test.s( Service1.class ) ),
                  test.toObject( Component1.class, test.s( Service1.class ) ) );
  }

  @Test
  public void resetTransactionSynchronizationRegistry()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( null, false );
    test.preTest();

    test.s( TransactionSynchronizationRegistry.class ).putResource( "key", "value" );
    test.resetTransactionSynchronizationRegistry();
    Assert.assertNull( test.s( TransactionSynchronizationRegistry.class ).getResource( "key" ) );
  }

  @Test
  public void serverTest_withoutDbCleaner()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( null, false );
    test.preTest();
    verify( test._dbCleaner, never() ).start();

    test.postTest();
    verify( test._dbCleaner, never() ).finish();
  }

  @Test
  public void serverTest_withNamedPersistenceUnit()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( "MyUnit", false );
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
    Assert.assertNull( test.s( TransactionSynchronizationRegistry.class ).getResource( "key" ) );

    test.postTest();
    verify( test._dbCleaner, never() ).finish();

    assertNull( test.getInjector() );
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
  public void setField()
    throws Exception
  {
    final MyServerTest test = new MyServerTest( null, true );
    test.preTest();
    final Component1 component1 = test.toObject( Component1.class, test.s( Service1.class ) );
    assertEquals( component1._myField, 0 );
    test.setField( component1, "_myField", 42 );
    assertEquals( component1._myField, 42 );
  }
}
