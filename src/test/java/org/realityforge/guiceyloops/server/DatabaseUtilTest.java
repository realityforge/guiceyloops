package org.realityforge.guiceyloops.server;

import java.io.File;
import java.sql.Connection;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class DatabaseUtilTest
{
  private static final String DATABASE_PREFIX = "myprefix";

  @BeforeMethod
  @AfterMethod
  public void clearDBProperties()
  {
    TestUtil.setDBProperties( null, null, null, null, null );
    TestUtil.setDBProperties( DATABASE_PREFIX, null, null, null, null );
  }

  @Test
  public void initDatabaseProperties_default()
  {
    assertPersistenceUnitProperties( null, "DATABASE_DRIVER_UNSET", "DATABASE_URL_UNSET", null, null );
  }

  @Test
  public void initDatabaseProperties_sysProperties()
  {
    final String driver = "MyDriver";
    final String url = "MyURL";
    final String user = "MyUser";
    final String password = "MyPassword";

    TestUtil.setDBProperties( null, driver, url, user, password );
    assertPersistenceUnitProperties( null, driver, url, user, password );
  }

  @Test
  public void initDatabaseProperties_sysPropertiesWithPrefix()
  {
    final String driver = "MyDriver";
    final String url = "MyURL";
    final String user = "MyUser";
    final String password = "MyPassword";

    TestUtil.setDBProperties( DATABASE_PREFIX, driver, url, user, password );
    assertPersistenceUnitProperties( DATABASE_PREFIX, driver, url, user, password );
  }

  @Test
  public void setAdditionalPersistenceUnitProperties()
  {
    final String key = "MyMagicKey";
    final String value = "value";
    final Properties properties = new Properties();
    properties.setProperty( key, value );
    DatabaseUtil.setAdditionalPersistenceUnitProperties( properties );
    assertEquals( DatabaseUtil.initPersistenceUnitProperties( null ).getProperty( key ), value );

    // Assert the configuration is a copy
    properties.clear();
    assertEquals( DatabaseUtil.initPersistenceUnitProperties( null ).getProperty( key ), value );

    //Assert empty properties can be specified
    DatabaseUtil.setAdditionalPersistenceUnitProperties( properties );
    assertNull( DatabaseUtil.initPersistenceUnitProperties( null ).getProperty( key ) );
  }

  @Test( expectedExceptions = { IllegalArgumentException.class } )
  public void getGlassFishDataSourceProperties_unknownJdbc()
  {
    TestUtil.setDBProperties( null,
                              "",
                              "jdbc:otherdb://example.com/SomeDB",
                              null,
                              null );
    DatabaseUtil.getGlassFishDataSourceProperties();
  }

  @Test
  public void getGlassFishDataSourceProperties_sqlsvr_allProperties()
  {
    TestUtil.setDBProperties( null,
                              "",
                              "jdbc:jtds:sqlserver://example.com:1500/SomeDB;user=MyUserName;password=My-Password",
                              null,
                              null );
    final Properties properties = DatabaseUtil.getGlassFishDataSourceProperties();
    assertEquals( properties.getProperty( "ServerName" ), "example.com" );
    assertEquals( properties.getProperty( "DatabaseName" ), "SomeDB" );
    assertEquals( properties.getProperty( "User" ), "MyUserName" );
    assertEquals( properties.getProperty( "Password" ), "My-Password" );
    assertEquals( properties.getProperty( "PortNumber" ), "1500" );
  }

  @Test
  public void getGlassFishDataSourceProperties_sqlsvr_minimal()
  {
    TestUtil.setDBProperties( null,
                              "",
                              "jdbc:jtds:sqlserver://example.com/SomeDB",
                              "MyUserName",
                              "My-Password" );
    final Properties properties = DatabaseUtil.getGlassFishDataSourceProperties();
    assertEquals( properties.getProperty( "ServerName" ), "example.com" );
    assertEquals( properties.getProperty( "DatabaseName" ), "SomeDB" );
    assertEquals( properties.getProperty( "User" ), "MyUserName" );
    assertEquals( properties.getProperty( "Password" ), "My-Password" );
  }

  @Test
  public void getGlassFishDataSourceProperties_pgsql_allProperties()
  {
    TestUtil.setDBProperties( null,
                              "",
                              "jdbc:postgresql://example.com:5432/SomeDB?user=MyUserName&password=My-Password",
                              null,
                              null );
    final Properties properties = DatabaseUtil.getGlassFishDataSourceProperties();
    assertEquals( properties.getProperty( "ServerName" ), "example.com" );
    assertEquals( properties.getProperty( "DatabaseName" ), "SomeDB" );
    assertEquals( properties.getProperty( "User" ), "MyUserName" );
    assertEquals( properties.getProperty( "Password" ), "My-Password" );
    assertEquals( properties.getProperty( "PortNumber" ), "5432" );
  }

  @Test
  public void getGlassFishDataSourceProperties_pgsql_minimal()
  {
    TestUtil.setDBProperties( null,
                              "",
                              "jdbc:postgresql://example.com/SomeDB",
                              "MyUserName",
                              "My-Password" );
    final Properties properties = DatabaseUtil.getGlassFishDataSourceProperties();
    assertEquals( properties.getProperty( "ServerName" ), "example.com" );
    assertEquals( properties.getProperty( "DatabaseName" ), "SomeDB" );
    assertEquals( properties.getProperty( "User" ), "MyUserName" );
    assertEquals( properties.getProperty( "Password" ), "My-Password" );
  }

  @Test
  public void initConnection_disposeConnection()
    throws Exception
  {
    final File file = TestUtil.setupDatabase();
    final Connection connection = DatabaseUtil.initConnection();
    connection.createStatement().execute( "SELECT 1" );

    assertFalse( connection.isClosed() );
    DatabaseUtil.disposeConnection( connection );
    assertTrue( connection.isClosed() );
    if ( !file.delete() )
    {
      file.deleteOnExit();
    }
  }

  @Test
  public void createEntityManager()
    throws Exception
  {
    final File file = TestUtil.setupDatabase();
    final EntityManager em = DatabaseUtil.createEntityManager( "TestUnit" );
    assertTrue( em.isOpen() );

    em.getTransaction().begin();
    em.createNativeQuery( "SELECT 1" ).getFirstResult();
    em.getTransaction().commit();

    em.close();
    assertFalse( em.isOpen() );
    if ( !file.delete() )
    {
      file.deleteOnExit();
    }
  }

  private void assertPersistenceUnitProperties( @Nullable final String databasePrefix,
                                                @Nonnull final String driver,
                                                @Nonnull final String url,
                                                @Nullable final String user,
                                                @Nullable final String password )
  {
    final Properties properties = DatabaseUtil.initPersistenceUnitProperties( databasePrefix );
    assertPropertyValue( properties, "javax.persistence.transactionType", "RESOURCE_LOCAL" );
    assertPropertyValue( properties, "javax.persistence.jtaDataSource", "" );
    assertPropertyValue( properties, "javax.persistence.jdbc.driver", driver );
    assertPropertyValue( properties, "javax.persistence.jdbc.url", url );
    assertPropertyValue( properties, "javax.persistence.jdbc.user", user );
    assertPropertyValue( properties, "javax.persistence.jdbc.password", password );
    final int propertyCount = 4 + ( null != user ? 1 : 0 ) + ( null != password ? 1 : 0 );
    assertEquals( properties.size(), propertyCount );
  }

  private void assertPropertyValue( final Properties properties, final String key, @Nullable final String value )
  {
    assertEquals( properties.getProperty( key ), value );
  }
}
