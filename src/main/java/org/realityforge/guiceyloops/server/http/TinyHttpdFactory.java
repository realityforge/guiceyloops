package org.realityforge.guiceyloops.server.http;

import java.util.ArrayList;

public final class TinyHttpdFactory
{
  private static final ArrayList<TinyHttpd> c_httpServers = new ArrayList<>();

  public static TinyHttpd createServer()
  {
    try
    {
      final TinyHttpd httpd = new TinyHttpd();
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
  }
}
