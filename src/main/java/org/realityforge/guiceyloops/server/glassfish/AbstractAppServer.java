package org.realityforge.guiceyloops.server.glassfish;

import javax.annotation.Nonnull;

/**
 * Base class for defining a singleton AppServer. Useful within tests.
 */
public abstract class AbstractAppServer
{
  private OpenMQContainer _openMQContainer;
  private GlassFishContainer _glassfish;
  private String _baseHttpURL;
  private boolean _enableOpenMQ;

  public final String getSiteURL()
  {
    return _baseHttpURL + getContextRoot();
  }

  public final String getBaseHttpURL()
  {
    return _baseHttpURL;
  }

  public abstract String getContextRoot();

  public boolean isOpenMQEnabled()
  {
    return _enableOpenMQ;
  }

  public void enableOpenMQ()
  {
    _enableOpenMQ = true;
  }

  public final void setUp()
    throws Exception
  {
    setUpOpenMQ();
    setUpGlassFish();
  }

  public final void tearDown()
  {
    teardownGlassFish();
    teardownOpenMQ();
    postTeardown();
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

  private void setUpGlassFish()
    throws Exception
  {
    if ( null == _glassfish )
    {
      _glassfish = new GlassFishContainer();
      _glassfish.start();

      configureGlassFish( _glassfish );

      performDeploy();
      postDeploy();
    }
  }

  private void setUpOpenMQ()
    throws Exception
  {
    if ( isOpenMQEnabled() && null == _openMQContainer )
    {
      _openMQContainer = new OpenMQContainer();
      _openMQContainer.start();
    }
  }

  private void teardownOpenMQ()
  {
    if ( null != _openMQContainer )
    {
      _openMQContainer.stop();
      _openMQContainer = null;
    }
  }

  private void teardownGlassFish()
  {
    if ( null != _glassfish )
    {
      _glassfish.stop();
      _glassfish = null;
    }
  }

  protected void postTeardown()
  {
    _baseHttpURL = null;
  }

  @Nonnull
  public final GlassFishContainer getGlassfish()
  {
    assert null != _glassfish;
    return _glassfish;
  }

  @Nonnull
  public final OpenMQContainer getOpenMQContainer()
  {
    assert null != _openMQContainer;
    return _openMQContainer;
  }
}
