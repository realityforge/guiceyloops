package org.realityforge.guiceyloops.server;

import java.sql.Connection;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class DatabaseUtilTest
{
  @BeforeMethod
  @AfterMethod
  public void clearDBProperties()
  {
    TestUtil.setDBProperties( null, null, null, null );
  }

  @Test
  public void initDatabaseProperties_default()
    throws Exception
  {
    assertDatabaseProperties( DatabaseUtil.MSSQL_DRIVER, "DATABASE_URL_UNSET", null, null );
  }

  @Test
  public void initDatabaseProperties_sysProperties()
    throws Exception
  {
    final String driver = "MyDriver" ;
    final String url = "MyURL" ;
    final String user = "MyUser" ;
    final String password = "MyPassword" ;

    TestUtil.setDBProperties( driver, url, user, password );
    assertDatabaseProperties( driver, url, user, password );
  }

  @Test
  public void initConnection_disposeConnection()
    throws Exception
  {
    TestUtil.setupBasicDBProperties();
    final Connection connection = DatabaseUtil.initConnection();
    connection.createStatement().execute( "SELECT 1" );

    assertFalse( connection.isClosed() );
    DatabaseUtil.disposeConnection( connection );
    assertTrue( connection.isClosed() );
  }


  private void assertDatabaseProperties( @Nonnull final String driver,
                                         @Nonnull final String url,
                                         @Nullable final String user,
                                         @Nullable final String password )
  {
    final Properties properties = DatabaseUtil.initDatabaseProperties();
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
