package org.realityforge.guiceyloops.server.glassfish;

import javax.annotation.Nonnull;

public interface Provisioner
{
  void provision( @Nonnull GlassFishContainer glassfish )
    throws Exception;
}
