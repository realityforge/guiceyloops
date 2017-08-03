package org.realityforge.guiceyloops.server.glassfish;

import java.util.Objects;
import javax.annotation.Nonnull;

public abstract class AbstractProvisioner
  implements Provisioner
{
  private String _baseHttpURL;

  public final void provision( @Nonnull final GlassFishContainer glassfish )
    throws Exception
  {
    _baseHttpURL = glassfish.getBaseHttpURL();
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
}
