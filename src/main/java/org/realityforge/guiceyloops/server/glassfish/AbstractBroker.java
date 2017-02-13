package org.realityforge.guiceyloops.server.glassfish;

import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractBroker
{
  @Nullable
  private OpenMQContainer _openMQContainer;

  public void start()
    throws Exception
  {
    if ( null == _openMQContainer )
    {
      final Properties properties = new Properties();
      properties.setProperty( "imq.autocreate.topic", "false" );
      properties.setProperty( "imq.autocreate.queue", "false" );
      customizeBrokerProperties( properties );
      _openMQContainer = new OpenMQContainer( GlassFishContainerUtil.getRandomPort(), properties );
      _openMQContainer.start();
      createBrokerResources();
    }
  }

  public void stop()
  {
    if ( null != _openMQContainer )
    {
      try
      {
        _openMQContainer.stop();
      }
      catch ( final Exception e )
      {
        throw new IllegalStateException( e );
      }
      finally
      {
        _openMQContainer = null;
      }
    }
  }

  protected void customizeBrokerProperties( @Nonnull final Properties properties )
  {
  }

  protected abstract void createBrokerResources()
    throws Exception;

  @Nonnull
  public OpenMQContainer getOpenMQContainer()
  {
    assert null != _openMQContainer;
    return _openMQContainer;
  }
}
