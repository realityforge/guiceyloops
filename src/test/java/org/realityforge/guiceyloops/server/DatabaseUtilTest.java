package org.realityforge.guiceyloops.server;

import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class DatabaseUtilTest
{
  @Test
  public void initDatabaseProperties_default()
    throws Exception
  {
    final String url = "DATABASE_URL_UNSET" ;
    final String driver = DatabaseUtil.MSSQL_DRIVER;
    final String user = null;
    final String password = null;

    assertDatabaseProperties( driver, url, user, password );
  }

  @Test
  public void initDatabaseProperties_sysProperties()
    throws Exception
  {
    final String driver = "MyDriver" ;
    final String url = "MyURL" ;
    final String user = "MyUser" ;
    final String password = "MyPassword" ;

    final String existingDriver = System.getProperty( "test.db.driver" );
    final String existingUrl = System.getProperty( "test.db.url" );
    final String existingUser = System.getProperty( "test.db.user" );
    final String existingPassword = System.getProperty( "test.db.password" );
    try
    {
      TestUtil.setDBProperties( driver, url, user, password );
      assertDatabaseProperties( driver, url, user, password );
    }
    finally
    {
      TestUtil.setDBProperties( existingDriver, existingUrl, existingUser, existingPassword );
    }
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
