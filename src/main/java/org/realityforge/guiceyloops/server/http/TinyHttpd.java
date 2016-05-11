package org.realityforge.guiceyloops.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A minimalist http server.
 */
public final class TinyHttpd
  implements HttpHandler
{
  private final Lock _lock = new ReentrantLock();
  private final HttpServer _server;
  private HttpHandler _httpHandler;

  public TinyHttpd()
    throws Exception
  {
    final int port = new Random().nextInt( 3000 ) + 10000;
    final InetSocketAddress addr = new InetSocketAddress( InetAddress.getLocalHost(), port );
    _server = HttpServer.create( addr, 0 );

    _server.createContext( "/", this );
    _server.setExecutor( Executors.newCachedThreadPool() );
  }

  public String getBaseURL()
  {
    return "http://" + getAddressString() + "/";
  }

  public String getAddressString()
  {
    final InetSocketAddress address = _server.getAddress();
    return address.getAddress().getCanonicalHostName() + ":" + address.getPort();
  }

  public void setHttpHandler( final HttpHandler httpHandler )
  {
    _httpHandler = httpHandler;
  }

  public Lock getLock()
  {
    return _lock;
  }

  public void start()
    throws Exception
  {
    _server.start();
  }

  public void stop()
  {
    _server.stop( 1 );
  }

  public void handle( final HttpExchange exchange )
    throws IOException
  {
    //Force http server to aquire lock before proceeding. That way we can
    // force a timeout in our tests by aquiring the lock ourselves
    _lock.lock();
    _lock.unlock();

    if ( null != _httpHandler )
    {
      _httpHandler.handle( exchange );
    }
  }
}
