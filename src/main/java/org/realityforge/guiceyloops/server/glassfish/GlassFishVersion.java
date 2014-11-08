package org.realityforge.guiceyloops.server.glassfish;

public enum GlassFishVersion
{
  V_3_1_2_2( "org.glassfish.main.extras:glassfish-embedded-all:jar:3.1.2.2" ),
  V_4_0( "org.glassfish.main.extras:glassfish-embedded-all:jar:4.0" ),
  V_4_1( "org.glassfish.main.extras:glassfish-embedded-all:jar:4.1" );

  private final String[] _specs;

  GlassFishVersion( final String... specs )
  {
    _specs = specs;
  }

  public String[] getSpecs()
  {
    return _specs;
  }
}
