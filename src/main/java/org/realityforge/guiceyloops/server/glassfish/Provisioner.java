package org.realityforge.guiceyloops.server.glassfish;

import java.util.Map;
import javax.annotation.Nonnull;

public interface Provisioner
{
  void provision( @Nonnull GlassFishContainer glassfish, @Nonnull Map<String, Object> services )
    throws Exception;
}
