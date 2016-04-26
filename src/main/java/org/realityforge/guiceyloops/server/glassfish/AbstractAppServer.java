package org.realityforge.guiceyloops.server.glassfish;

import javax.annotation.Nonnull;

/**
 * Base class for defining a singleton AppServer. Useful within tests.
 */
public abstract class AbstractAppServer
{
  private GlassFishContainer _glassfish;
  private String _baseHttpURL;

  public final String getSiteURL()
  {
    return _baseHttpURL + getContextRoot();
  }

  public final String getBaseHttpURL()
  {
    return _baseHttpURL;
  }

  protected abstract String getContextRoot();

  public final void setUp()
    throws Exception
  {
    if ( null == _glassfish )
    {
      performSetup();
    }
  }

  public final void tearDown()
  {
    if ( null != _glassfish )
    {
      performTeardown();
    }
  }

  private void performSetup()
    throws Exception
  {
    _glassfish = new GlassFishContainer();

    _glassfish.start();
    configureGlassFish( _glassfish );

    performDeploy();
    postDeploy();
  }

  protected void postDeploy()
    throws Exception
  {
    _baseHttpURL = getGlassfish().getBaseHttpURL();
  }

  protected void configureGlassFish( @Nonnull GlassFishContainer glassfish )
    throws Exception
  {
  }

  protected abstract void performDeploy()
    throws Exception;

  protected void performTeardown()
  {
    _glassfish.stop();
    _glassfish = null;
    _baseHttpURL = null;
  }

  @Nonnull
  protected final GlassFishContainer getGlassfish()
  {
    assert null != _glassfish;
    return _glassfish;
  }

  @Nonnull
  protected final String getProperty( @Nonnull final String name )
  {
    final String property = System.getProperties().getProperty( name, null );
    if ( null == property )
    {
      throw new IllegalStateException( "Missing property: " + name );
    }
    return property;
  }
}
