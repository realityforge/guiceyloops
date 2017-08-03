package org.realityforge.guiceyloops.server.glassfish;

import java.util.ArrayList;
import javax.annotation.Nonnull;

/**
 * Base class for defining a singleton AppServer. Useful within tests.
 */
public abstract class AbstractAppServer
{
  private final ArrayList<Provisioner> _provisioners = new ArrayList<>();
  private OpenMQContainer _openMQContainer;
  private GlassFishContainer _glassfish;
  private boolean _enableOpenMQ;

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

  private void setUpGlassFish()
    throws Exception
  {
    if ( null == _glassfish )
    {
      _glassfish = new GlassFishContainer();
      _glassfish.start();

      preProvision();
      for ( final Provisioner provisioner : _provisioners )
      {
        provisioner.provision( _glassfish );
      }
      postProvision();
    }
  }

  protected void preProvision()
  {
  }

  protected void postProvision()
  {
  }

  protected void addProvisioner( @Nonnull final Provisioner provisioner )
  {
    _provisioners.add( provisioner );
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
