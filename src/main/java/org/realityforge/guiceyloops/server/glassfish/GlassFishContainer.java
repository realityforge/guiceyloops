package org.realityforge.guiceyloops.server.glassfish;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

/**
 * A class that instantiates the glassfish app server in a separate classloader.
 */
public final class GlassFishContainer
{
  private static final Logger LOG = Logger.getLogger( GlassFishContainer.class.getName() );

  private final int _port;
  private final URL[] _glassfishClasspath;
  private Object _glassfish;

  public GlassFishContainer( final int port, final URL[] glassfishClasspath )
  {
    _port = port;
    _glassfishClasspath = glassfishClasspath;
  }

  public int getPort()
  {
    return _port;
  }

  public void start()
    throws Exception
  {
    final ClassLoader loader = ClassLoader.getSystemClassLoader().getParent();
    assert null != loader;
    final ClassLoader classLoader = new URLClassLoader( _glassfishClasspath, loader );

    final Object properties = classLoader.loadClass( "org.glassfish.embeddable.GlassFishProperties" ).newInstance();

    properties.getClass().
      getMethod( "setPort", new Class[]{ String.class, int.class } ).
      invoke( properties, "http-listener", _port );

    LOG.info( "Configuring Glassfish on port: " + _port );

    final Object runtime =
      classLoader.loadClass( "org.glassfish.embeddable.GlassFishRuntime" ).getMethod( "bootstrap" ).invoke( null );

    _glassfish = runtime.getClass().
      getMethod( "newGlassFish", new Class[]{ properties.getClass() } ).invoke( runtime, properties );

    _glassfish.getClass().getMethod( "start" ).invoke( _glassfish );

    // Need to set java.security.auth.login.config otherwise the embedded container will
    // fail to find the login config ... even though it creates it...
    final String instanceRoot = System.getProperty( "com.sun.aas.instanceRoot" );
    System.setProperty( "java.security.auth.login.config",
                        instanceRoot + File.separator + "config" + File.separator + "login.conf" );

  }

  public void stop()
  {
    if ( null != _glassfish )
    {
      try
      {
        _glassfish.getClass().getMethod( "stop" ).invoke( _glassfish );
        _glassfish.getClass().getMethod( "dispose" ).invoke( _glassfish );
      }
      catch ( final Exception e )
      {
        // Ignored
      }
      _glassfish = null;
    }
  }

  public void deploy( final String contextRoot, final String appName, final File warFile )
    throws Exception
  {
    LOG.info( "Deploying planner warfile: " + warFile.getAbsolutePath() );
    final String output =
      execute( "deploy",
               "--contextroot=" + contextRoot,
               "--name=" + appName,
               "--force=true",
               warFile.getAbsolutePath() );
    if ( !output.replace( "\n", " " ).matches( ".*Application deployed with name " + appName + "\\..*" ) )
    {
      throw new IllegalStateException( "Failed to deploy planner" );
    }

  }

  public void createCustomResource( final String service, final String value )
    throws Exception
  {
    execute( "create-custom-resource",
             "--factoryclass", "org.glassfish.resources.custom.factory.PrimitivesAndStringFactory",
             "--restype", "java.lang.String",
             "--property", "value=" + value.replace( ":", "\\:" ),
             service );
  }

  public void createUser( final String username, final String password, final String[] groups )
    throws Exception
  {
    final StringBuilder sb = new StringBuilder();
    for ( final String group : groups )
    {
      if ( 0 != sb.length() )
      {
        sb.append( ':' );
      }
      sb.append( group );
    }
    final File passwordFile = File.createTempFile( "passwd", "txt" );
    final FileOutputStream outputStream = new FileOutputStream( passwordFile );
    outputStream.write( ( "AS_ADMIN_USERPASSWORD=" + password + "\n" ).getBytes() );
    outputStream.close();
    execute( "create-file-user",
             "--authrealmname", "file",
             "--groups", sb.toString(),
             "--passwordfile=" + passwordFile,
             username );
  }

  public final String execute( final String command, final String... args )
    throws Exception
  {
    final Object runner = _glassfish.getClass().getMethod( "getCommandRunner" ).invoke( _glassfish );
    final Object result =
      runner.getClass().getMethod( "run", new Class[]{ String.class, String[].class } ).invoke( runner, command, args );
    final Method method = result.getClass().getDeclaredMethod( "getOutput" );
    method.setAccessible( true );
    return (String) method.invoke( result );
  }
}