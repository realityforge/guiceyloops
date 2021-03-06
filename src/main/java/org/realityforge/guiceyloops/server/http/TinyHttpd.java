package org.realityforge.guiceyloops.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
  private HttpServer _server;
  private HttpHandler _httpHandler;
  private InetSocketAddress _address;

  public TinyHttpd()
    throws Exception
  {
    this( 0 );
  }

  public TinyHttpd( final int port )
    throws Exception
  {
    _address = new InetSocketAddress( InetAddress.getLocalHost(), port );
  }

  public String getBaseURL()
  {
    return "http://" + getAddressString() + "/";
  }

  public String getAddressString()
  {
    return getAddress().getAddress().getCanonicalHostName() + ":" + getAddress().getPort();
  }

  /**
   * Return the address associated with httpd.
   * If called before start then it may not be the actual address and may instead be
   * the requested address with a host like 0.0.0.0 or a port such as 0.
   */
  public InetSocketAddress getAddress()
  {
    if ( null != _server )
    {
      return _server.getAddress();
    }
    else
    {
      return _address;
    }
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
    if ( null != _server )
    {
      throw new IllegalStateException( "Attempting to start server that is already started" );
    }
    _server = HttpServer.create( _address, 0 );
    _server.createContext( "/", this );
    _server.setExecutor( Executors.newCachedThreadPool() );

    _server.start();
  }

  public void stop()
  {
    if ( null != _server )
    {
      _server.stop( 1 );
      _server = null;
    }
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
