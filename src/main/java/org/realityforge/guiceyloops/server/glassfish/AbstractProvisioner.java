package org.realityforge.guiceyloops.server.glassfish;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

public abstract class AbstractProvisioner
  implements Provisioner
{
  private String _baseHttpURL;
  private Map<String, Object> _services;
  private GlassFishContainer _glassfish;

  @Override
  public void provision( @Nonnull final GlassFishContainer glassfish, @Nonnull final Map<String, Object> services )
    throws Exception
  {
    _glassfish = glassfish;
    _services = services;
    _baseHttpURL = _glassfish.getBaseHttpURL();
    configure( glassfish );
    deploy( glassfish );
    postDeploy( glassfish );
  }

  protected abstract void configure( @Nonnull GlassFishContainer glassfish )
    throws Exception;

  protected abstract void deploy( @Nonnull GlassFishContainer glassfish )
    throws Exception;

  protected void postDeploy( @Nonnull final GlassFishContainer glassfish )
    throws Exception
  {
  }

  @Nonnull
  protected final String getBaseHttpURL()
  {
    return Objects.requireNonNull( _baseHttpURL );
  }

  @Nonnull
  public final OpenMQContainer getOpenMQContainer()
  {
    final OpenMQContainer _openMQContainer = (OpenMQContainer) _services.get( OpenMQContainer.class.getName() );
    assert null != _openMQContainer;
    return _openMQContainer;
  }
}
