package org.realityforge.guiceyloops.server.glassfish;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.realityforge.guiceyloops.server.DatabaseUtil;

/**
 * A class that instantiates the glassfish app server in a separate classloader.
 */
public class GlassFishContainer
{
  private static final Logger LOG = Logger.getLogger( GlassFishContainer.class.getName() );

  private final int _port;
  private final List<URL> _glassfishClasspath;
  private Object _glassfish;

  public GlassFishContainer()
    throws Exception
  {
    this( GlassFishContainerUtil.getRandomPort() );
  }

  public GlassFishContainer( final int port )
    throws Exception
  {
    this( port, GlassFishVersion.V_4_0 );
  }

  public GlassFishContainer( final int port, @Nonnull final GlassFishVersion version )
    throws Exception
  {
    this( port, GlassFishContainerUtil.getEmbeddedGlassFishClasspath( version ) );
  }

  public GlassFishContainer( final int port, final URL[] classpath )
  {
    _port = port;
    _glassfishClasspath = new ArrayList<URL>();
    for ( final URL url : classpath )
    {
      addToClasspath( url );
    }
  }

  /**
   * Add a Maven-esque dependency spec to the classpath.
   * It is assumed to be in the local maven repository. An example spec
   * looks like "net.sourceforge.jtds:jtds:jar:1.3.1".
   *
   * @param spec the dependency spec.
   */
  public void addSpecToClasspath( final String spec )
  {
    final File file = GlassFishContainerUtil.specToFile( GlassFishContainerUtil.getMavenRepository(), spec );
    if ( !file.exists() )
    {
      final String message =
        "Attempted to add spec '" + spec + "' that does not exist in the local maven repository at '" + file + "'.";
      throw new IllegalStateException( message );
    }
    addToClasspath( file );
  }

  public void addToClasspath( final File file )
  {
    if ( !file.exists() )
    {
      final String message = "Attempted to file '" + file + "' that does not exist.";
      throw new IllegalStateException( message );
    }
    try
    {
      addToClasspath( file.toURI().toURL() );
    }
    catch ( final MalformedURLException mue )
    {
      throw new IllegalStateException();
    }
  }

  public void addToClasspath( final URL url )
  {
    if ( null != _glassfish )
    {
      throw new IllegalStateException( "Unable to add to classpath after glassfish already started." );
    }
    _glassfishClasspath.add( url );
  }

  public int getPort()
  {
    return _port;
  }

  public String getBaseHttpURL()
    throws Exception
  {
    final String hostAddress = InetAddress.getLocalHost().getHostAddress();
    return "http://" + hostAddress + ":" + getPort();
  }

  public void start()
    throws Exception
  {
    if ( null == _glassfish )
    {
      LOG.info( "Starting GlassFish." );
      DerbyUtil.configureNullLogger();

      final ClassLoader loader = ClassLoader.getSystemClassLoader().getParent();
      assert null != loader;
      final URL[] classpath = _glassfishClasspath.toArray( new URL[ _glassfishClasspath.size() ] );
      final ClassLoader classLoader = new URLClassLoader( classpath, loader );

      final Object properties = classLoader.loadClass( "org.glassfish.embeddable.GlassFishProperties" ).newInstance();

      properties.getClass().
        getMethod( "setPort", new Class[]{ String.class, int.class } ).
        invoke( properties, "http-listener", _port );

      LOG.info( "Configuring Glassfish on port: " + _port );

      final Object runtime =
        classLoader.loadClass( "org.glassfish.embeddable.GlassFishRuntime" ).getMethod( "bootstrap" ).invoke( null );

      _glassfish = runtime.getClass().
        getMethod( "newGlassFish", new Class[]{ properties.getClass() } ).invoke( runtime, properties );
      System.getProperties().remove( "java.naming.factory.initial" );
      _glassfish.getClass().getMethod( "start" ).invoke( _glassfish );

      // Need to set java.security.auth.login.config otherwise the embedded container will
      // fail to find the login config ... even though it creates it...
      final String instanceRoot = System.getProperty( "com.sun.aas.instanceRoot" );
      System.setProperty( "java.security.auth.login.config",
                          instanceRoot + File.separator + "config" + File.separator + "login.conf" );
      LOG.info( "GlassFish started." );
    }
    else
    {
      LOG.warning( "Attempted to start already started GlassFish instance." );
    }
  }

  public void stop()
  {
    if ( null != _glassfish )
    {
      LOG.info( "Stopping GlassFish." );
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
      LOG.info( "GlassFish stopped." );
    }
    else
    {
      LOG.warning( "Attempted to stop already stopped GlassFish instance." );
    }
  }

  public String deploy( final String contextRoot, final String appName, final File warFile )
    throws Exception
  {
    LOG.info( "Deploying war: " + warFile.getAbsolutePath() + " to " + getBaseHttpURL() + contextRoot );
    final String output =
      execute( "deploy",
               "--contextroot=" + contextRoot,
               "--name=" + appName,
               "--force=true",
               warFile.getAbsolutePath() );
    if ( !output.replace( "\n", " " ).matches( ".*Application deployed with name " + appName + "\\..*" ) )
    {
      throw new IllegalStateException( "Failed to deploy war: " + warFile.getAbsolutePath() );
    }
    return getBaseHttpURL() + contextRoot;
  }

  public void createSqlServerJdbcResource( final String key,
                                           final String databaseConnectionProperty )
    throws Exception
  {
    createJdbcResource( key, "net.sourceforge.jtds.jdbcx.JtdsDataSource", databaseConnectionProperty );
  }

  public void createSqlServerJdbcResource( final String key )
    throws Exception
  {
    final String databasePoolProperties =
      toGlassFishPropertiesString( DatabaseUtil.getGlassFishDataSourceProperties() );
    createJdbcResource( key, "net.sourceforge.jtds.jdbcx.JtdsDataSource", databasePoolProperties );
  }

  public String toGlassFishPropertiesString( final Properties properties )
  {
    final StringBuilder sb = new StringBuilder();
    for ( final String property : properties.stringPropertyNames() )
    {
      if ( 0 != sb.length() )
      {
        sb.append( ":" );
      }
      sb.append( property );
      sb.append( "=" );
      sb.append( properties.getProperty( property ) );
    }
    return sb.toString();
  }

  public void createJdbcResource( final String key,
                                  final String dataSourceClassName,
                                  final String databaseConnectionProperty )
    throws Exception
  {
    LOG.info( "Creating jdbc resource: " + key );
    final String poolID = key + "Pool";
    execute( "create-jdbc-connection-pool",
             "--datasourceclassname", dataSourceClassName,
             "--restype", "javax.sql.DataSource",
             "--isconnectvalidatereq=true",
             "--validationmethod", "auto-commit",
             "--ping", "true",
             "--property", databaseConnectionProperty,
             poolID );

    execute( "create-jdbc-resource", "--connectionpoolid", poolID, key );
  }

  public void createCustomResource( final String key, final String value )
    throws Exception
  {
    LOG.info( "Creating custom resource: " + key + "=" + value );
    execute( "create-custom-resource",
             "--factoryclass", "org.glassfish.resources.custom.factory.PrimitivesAndStringFactory",
             "--restype", "java.lang.String",
             "--property", "value=" + value.replace( ":", "\\:" ),
             key );
  }

  public void createCustomResource( final String key, final boolean value )
    throws Exception
  {
    LOG.info( "Creating custom resource: " + key + "=" + value );
    execute( "create-custom-resource",
             "--factoryclass", "org.glassfish.resources.custom.factory.PrimitivesAndStringFactory",
             "--restype", "java.lang.Boolean",
             "--property", "value=" + value,
             key );
  }

  public void createUser( final String username, final String password, final String[] groups )
    throws Exception
  {
    LOG.info( "Creating user: " + username );
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
    final Object commandResult =
      runner.getClass().getMethod( "run", new Class[]{ String.class, String[].class } ).invoke( runner, command, args );

    final Enum exitStatus = invokeMethod( commandResult, "getExitStatus" );
    final boolean failed = !"SUCCESS".equals( exitStatus.name() );
    final String output = invokeMethod( commandResult, "getOutput" );
    final Throwable throwable = invokeMethod( commandResult, "getFailureCause" );
    if ( failed || null != throwable )
    {
      throw new Exception( output, throwable );
    }
    else
    {
      return output;
    }
  }

  @SuppressWarnings( "unchecked" )
  private <T> T invokeMethod( final Object object, final String methodName )
    throws Exception
  {
    final Method method = object.getClass().getDeclaredMethod( methodName );
    method.setAccessible( true );
    return (T) method.invoke( object );
  }
}
