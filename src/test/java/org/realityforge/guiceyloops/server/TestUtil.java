package org.realityforge.guiceyloops.server;

import java.io.File;
import java.io.IOException;

final class TestUtil
{
  private TestUtil()
  {
  }

  static void setDBProperties( final String driver,
                               final String url,
                               final String user,
                               final String password )
  {
    setSystemProperty( "test.db.driver", driver );
    setSystemProperty( "test.db.url", url );
    setSystemProperty( "test.db.user", user );
    setSystemProperty( "test.db.password", password );
  }

  private static void setSystemProperty( final String key, final String value )
  {
    if ( null != value )
    {
      System.setProperty( key, value );
    }
    else
    {
      System.getProperties().remove( key );
    }
  }

  private static String genJdbcUrl()
    throws IOException
  {
    return "jdbc:h2:" + File.createTempFile( "database", "h2" );
  }

  static void setupBasicDBProperties()
    throws IOException
  {
    final String url = genJdbcUrl();
    final String driver = org.h2.Driver.class.getName();
    final String user = null;
    final String password = null;

    setDBProperties( driver, url, user, password );
  }
}
