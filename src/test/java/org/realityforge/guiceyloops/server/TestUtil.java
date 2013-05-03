package org.realityforge.guiceyloops.server;

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
}
