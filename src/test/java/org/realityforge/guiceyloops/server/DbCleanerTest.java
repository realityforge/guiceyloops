package org.realityforge.guiceyloops.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import javax.persistence.EntityManager;
import org.realityforge.guiceyloops.JEETestingModule;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DbCleanerTest
{
  private EntityManager _entityManager;
  private File _databaseFile;

  @BeforeMethod
  public final void setupEntityManager()
    throws Exception
  {
    _databaseFile = TestUtil.setupDatabase();
    _entityManager = DatabaseUtil.createEntityManager( "TestUnit" );
  }

  @AfterMethod
  public final void tearDownEntityManager()
    throws Exception
  {
    if ( null != _entityManager )
    {
      _entityManager.close();
      _entityManager = null;
    }
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
  public void interactionTest()
  {
    final String table1 = "my_table" ;
    final String table2 = "my_other_table" ;
    final String entry1 = "foo1" ;
    final String entry2 = "foo2" ;

    final String[] tables = { table1 };
    final Injector injector =
      Guice.createInjector( new DbCleanerTestModule( _entityManager, tables ),
                            new JEETestingModule() );
    final DbCleaner cleaner = injector.getInstance( DbCleaner.class );
    createTable( table1 );
    createTable( table2 );
    insertEntry( table1, entry1 );
    insertEntry( table2, entry1 );
    assertEntryPresent( table1, entry1 );
    assertEntryPresent( table2, entry1 );

    assertFalse( cleaner.isActive() );

    cleaner.start();
    assertTrue( cleaner.isActive() );
    // Initial start will perform a clean so table1 should be nuked
    assertEntryNotPresent( table1, entry1 );
    // while table2 is not cleaned
    assertEntryPresent( table2, entry1 );

    insertEntry( table1, entry2 );
    insertEntry( table2, entry2 );
    assertEntryPresent( table1, entry2 );
    assertEntryPresent( table2, entry2 );

    cleaner.finish();

    // Finish should roll back and thus clear out entry2 from both
    assertEntryNotPresent( table1, entry2 );
    assertEntryNotPresent( table2, entry2 );

    // Clean will have cleaned table1 but table2 is not cleaned
    assertEntryNotPresent( table1, entry1 );
    assertEntryPresent( table2, entry1 );

    // We sneakily insert again into table1
    // this would normally have been cleaned or
    // rolled back but because we do it out-of-band
    // we can use it to check whether the second start
    // called clean
    insertEntry( table1, entry1 );
    assertEntryPresent( table1, entry1 );

    cleaner.start();
    // Second start should not perform a clean so table1 should NOT be nuked
    assertEntryPresent( table1, entry1 );

    cleaner.finish();

    // In the next sequence we mark the run as using a transaction
    cleaner.start();
    assertEntryPresent( table1, entry1 );
    cleaner.usesTransaction();

    // Insert into table2 should persist as table2 is unmanaged and we
    // invoked usesTransaction()
    insertEntry( table2, entry2 );
    assertEntryPresent( table2, entry2 );

    cleaner.finish();

    // Can just call start again as usesTransaction reset transaction
    cleaner.start();
    // Should still be here
    assertEntryPresent( table2, entry2 );
    assertEntryNotPresent( table1, entry1 );
    cleaner.finish();
  }

  private void createTable( final String table )
  {
    executeUpdate( "CREATE TABLE " + table + "(id INT PRIMARY KEY AUTO_INCREMENT NOT NULL, name VARCHAR(50));" );
  }

  private void insertEntry( final String table, final String entry )
  {
    executeUpdate( "INSERT INTO " + table + "(name) VALUES ('" + entry + "');" );
  }

  private void assertEntryPresent( final String table, final String entry )
  {
    Assert.assertEquals( getEntryCount( table, entry ), 1 );
  }

  private void assertEntryNotPresent( final String table, final String entry )
  {
    Assert.assertEquals( getEntryCount( table, entry ), 0 );
  }

  private long getEntryCount( final String table, final String entry )
  {
    return (Long) querySingleResult( "SELECT COUNT(*) FROM " + table + " WHERE name = '" + entry + "'" );
  }

  @SuppressWarnings( "unchecked" )
  private <T> T querySingleResult( final String sql )
  {
    final boolean active = _entityManager.getTransaction().isActive();
    if ( !active )
    {
      _entityManager.getTransaction().begin();
    }
    final Object result = _entityManager.createNativeQuery( sql ).getSingleResult();
    if ( !active )
    {
      _entityManager.getTransaction().commit();
    }
    return (T) result;
  }

  private void executeUpdate( final String sql )
  {
    final boolean active = _entityManager.getTransaction().isActive();
    if ( !active )
    {
      _entityManager.getTransaction().begin();
    }
    _entityManager.createNativeQuery( sql ).executeUpdate();
    if ( !active )
    {
      _entityManager.getTransaction().commit();
    }
  }
}
