package org.realityforge.guiceyloops.server;

import java.io.File;
import java.io.IOException;

public final class TestUtil
{
  private TestUtil()
  {
  }

  static void setDBProperties( final String databasePrefix,
                               final String driver,
                               final String url,
                               final String user,
                               final String password )
  {
    final String prefix = null == databasePrefix ? "" : databasePrefix + ".";
    setSystemProperty( prefix + "test.db.driver", driver );
    setSystemProperty( prefix + "test.db.url", url );
    setSystemProperty( prefix + "test.db.user", user );
    setSystemProperty( prefix + "test.db.password", password );
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

  public static File setupDatabase()
    throws IOException
  {
    final File databaseFile = File.createTempFile( "database", "h2" );
    final String url = "jdbc:h2:" + databaseFile;
    final String driver = org.h2.Driver.class.getName();
    final String user = null;
    final String password = null;

    setDBProperties( null, driver, url, user, password );
    return databaseFile;
  }
}
