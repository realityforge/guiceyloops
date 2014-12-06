package org.realityforge.guiceyloops.server;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.mail.Authenticator;
import javax.mail.Session;
import org.realityforge.guiceyloops.shared.AbstractModule;

public abstract class GreenMailTestModule
  extends AbstractModule
{
 private static final Logger LOG = Logger.getLogger( GreenMailTestModule.class.getName() );

  private final String _address;
  private final int _port;

  public GreenMailTestModule()
  {
    this( 4000 + new Random().nextInt( 4000 ) );
  }

  public GreenMailTestModule( final int port )
  {
    this( getLocalHostAddress(), port );
  }

  public GreenMailTestModule( final String address, final int port )
  {
    _address = address;
    _port = port;
  }

  public final String getAddress()
  {
    return _address;
  }

  public final int getPort()
  {
    return _port;
  }

  @Override
  protected void configure()
  {
    final ServerSetup config =
      new ServerSetup( getPort(), getAddress(), ServerSetup.PROTOCOL_SMTP );
    bind( GreenMail.class ).toInstance( new GreenMail( config ) );

    bindMailResources( config );
  }

  protected abstract void bindMailResources( @Nonnull ServerSetup config );

  protected final void bindMailResource( final ServerSetup config, final String name )
  {
    LOG.info( "Binding smtp session to " + config.getBindAddress() + ":" + config.getPort() );
    bindResource( Session.class, name, getSession( config ) );
  }

  @Nonnull
  public static String getLocalHostAddress()
  {
    try
    {
      return InetAddress.getLocalHost().getHostAddress();
    }
    catch ( final Exception e )
    {
      throw new IllegalStateException( e.getMessage(), e );
    }
  }

  protected final Session getSession( final ServerSetup config )
  {
    final Authenticator authenticator = new TestAuthenticator();
    final Properties properties = new Properties();
    properties.setProperty( "mail.smtp.host", config.getBindAddress() );
    properties.setProperty( "mail.smtp.port", String.valueOf( config.getPort() ) );

    return Session.getInstance( properties, authenticator );
  }

  private class TestAuthenticator
    extends Authenticator
  {
    public TestAuthenticator()
    {
    }
  }
}
