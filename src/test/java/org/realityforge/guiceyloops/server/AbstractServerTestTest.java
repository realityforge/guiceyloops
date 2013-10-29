package org.realityforge.guiceyloops.server;

import com.google.inject.Module;
import java.util.ArrayList;
import java.util.Arrays;
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
  }

  public static class MyServerTest
    extends AbstractServerTest
  {
    EntityManager _entityManager = Mockito.mock( EntityManager.class );
    DbCleaner _dbCleaner = Mockito.mock( DbCleaner.class );

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
          bind( EntityManager.class ).toInstance( _entityManager );
          bind( DbCleaner.class ).toInstance( _dbCleaner );
          bindResource( String[].class, DbCleaner.TABLE_NAME_KEY, new String[ 0 ] );
          multiBind( Component1.class, Service1.class, Service2.class );
        }
      };
    }
  }

  @Test
  public void serverTest()
    throws Exception
  {
    final MyServerTest test = new MyServerTest();
    test.preTest();
    verify( test._dbCleaner ).start();
    //final Injector injector = test.getInjector();

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

    assertEquals( test.toObject( Component1.class, test.s( Service1.class ) ),
                  test.toObject( Component1.class, test.s( Service1.class ) ) );

    test.postTest();
    verify( test._dbCleaner ).finish();

    assertNull( test.getInjector() );
  }
}
