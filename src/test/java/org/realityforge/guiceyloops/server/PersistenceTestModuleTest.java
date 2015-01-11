package org.realityforge.guiceyloops.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PersistenceTestModuleTest
{
  private File _databaseFile;

  @BeforeMethod
  public final void setupEntityManager()
    throws Exception
  {
    _databaseFile = TestUtil.setupDatabase();
  }

  @AfterMethod
  public final void tearDownEntityManager()
    throws Exception
  {
    if ( null != _databaseFile )
    {
      if ( !_databaseFile.delete() )
      {
        _databaseFile.deleteOnExit();
      }
      _databaseFile = null;
    }
  }

  @Test
  public void basicOperation()
    throws Throwable
  {
    final Injector injector =
      Guice.createInjector( new TestPersistenceTestModule(), new JEETestingModule() );

    assertNotNull( injector.getInstance( Key.get( EntityManager.class, Names.named( "TestUnit" ) ) ) );

    final DbCleaner cleaner = injector.getInstance( Key.get( DbCleaner.class, Names.named( "TestUnit" ) ) );
    assertNotNull( cleaner );

    final Field field = cleaner.getClass().getDeclaredField( "_tableNames" );
    field.setAccessible( true );
    final String[] tableNames = (String[]) field.get( cleaner );
    assertEquals( tableNames.length, 2 );
    assertEquals( tableNames[ 0 ], "Test.tblTestEntity1" );
    assertEquals( tableNames[ 1 ], "Test.tblTestEntity2" );
  }

  static class TestPersistenceTestModule
    extends PersistenceTestModule
  {
    TestPersistenceTestModule()
    {
      super( "TestUnit" );
    }

    @Override
    protected void configure()
    {
      super.configure();
      final ArrayList<String> tables = new ArrayList<String>();
      tables.add( "Test.tblTestEntity1" );
      tables.add( "Test.tblTestEntity2" );
      requestCleaningOfTables( tables.toArray( new String[ tables.size() ] ) );
    }
  }
}
