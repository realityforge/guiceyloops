package org.realityforge.guiceyloops.server.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class DefaultHttpHandler
  implements HttpHandler
{
  private final int _responseCode;
  private final String _contentType;
  private final String _content;

  public DefaultHttpHandler( final String content )
  {
    this( MediaType.TEXT_PLAIN, content );
  }

  public DefaultHttpHandler( final String contentType, final String content )
  {
    this( 200, contentType, content );
  }

  public DefaultHttpHandler( final int responseCode, final String content )
  {
    this( responseCode, MediaType.TEXT_PLAIN, content );
  }

  public DefaultHttpHandler( final int responseCode, final String contentType, final String content )
  {
    _responseCode = responseCode;
    _contentType = contentType;
    _content = content;
  }

  @Override
  public void handle( final HttpExchange exchange )
    throws IOException
  {
    try ( final OutputStream responseBody = exchange.getResponseBody() )
    {
      exchange.getResponseHeaders().set( HttpHeaders.CONTENT_TYPE, _contentType );
      exchange.sendResponseHeaders( _responseCode, 0 );
      responseBody.write( _content.getBytes() );
    }
  }
}
