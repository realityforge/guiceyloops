package org.realityforge.guiceyloops.server;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import java.net.InetAddress;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Session;

public abstract class GreenMailTestModule
  extends AbstractModule
{
  @Override
  protected void configure()
  {
    final ServerSetup config =
      new ServerSetup( getSmtpPort(), getLocalHost().getHostAddress(), ServerSetup.PROTOCOL_SMTP );
    bind( GreenMail.class ).toInstance( new GreenMail( config ) );

    bindMailResources( config );
  }

  protected abstract void bindMailResources( ServerSetup config );

  protected final void bindMailResource( final ServerSetup config, final String name )
  {
    bindResource( Session.class, name, getSession( config ) );
  }

  protected int getSmtpPort()
  {
    return 3025;
  }

  private InetAddress getLocalHost()
  {
    try
    {
      return InetAddress.getLocalHost();
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
