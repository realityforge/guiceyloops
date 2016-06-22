package org.realityforge.guiceyloops.server.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public final class TinyHttpdFactory
{
  private static final ArrayList<TinyHttpd> c_httpServers = new ArrayList<>();
  private static final Map<String, TinyHttpd> c_httpServerMap = new HashMap<>();

  public static TinyHttpd createServer()
  {
    return findOrCreateServer( null );
  }

  public static TinyHttpd findOrCreateServer( @Nullable final String key )
  {
    if ( null != key )
    {
      final TinyHttpd httpd = c_httpServerMap.get( key );
      if ( null != httpd )
      {
        return httpd;
      }
    }
    try
    {
      final TinyHttpd httpd = new TinyHttpd();
      if ( null != key )
      {
        c_httpServerMap.put( key, httpd );
      }
      c_httpServers.add( httpd );
      return httpd;
    }
    catch ( final Exception e )
    {
      throw new IllegalStateException( "Unable to create httpd server", e );
    }
  }

  public static void startServers()
    throws Exception
  {
    for ( final TinyHttpd http : c_httpServers )
    {
      http.start();
    }
  }

  public static void shutdownServers()
  {
    for ( final TinyHttpd http : c_httpServers )
    {
      http.stop();
    }
  }

  public static void destroyServers()
  {
    shutdownServers();
    c_httpServers.clear();
    c_httpServerMap.clear();
  }
}
