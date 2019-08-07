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
import javax.annotation.Nullable;
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
    this( port, GlassFishContainerUtil.getEmbeddedGlassFishClasspath() );
  }

  public GlassFishContainer( @Nonnull final GlassFishVersion version )
    throws Exception
  {
    this( GlassFishContainerUtil.getRandomPort(), GlassFishContainerUtil.getEmbeddedGlassFishClasspath( version ) );
  }

  public GlassFishContainer( final int port, @Nonnull final GlassFishVersion version )
    throws Exception
  {
    this( port, GlassFishContainerUtil.getEmbeddedGlassFishClasspath( version ) );
  }

  public GlassFishContainer( final int port, @Nonnull final URL[] classpath )
  {
    _port = port;
    _glassfishClasspath = new ArrayList<>();
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
  public void addSpecToClasspath( @Nonnull final String spec )
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

  public void addToClasspath( @Nonnull final File file )
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

  public void addToClasspath( @Nonnull final URL url )
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

  @Nonnull
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
      // Set system property for derby that will result in a warning but
      // will also mean logs are not emitted in working directory.
      System.setProperty( "derby.stream.error.field", "X.X" );

      final ClassLoader loader = ClassLoader.getSystemClassLoader().getParent();
      assert null != loader;
      final URL[] classpath = _glassfishClasspath.toArray( new URL[ 0 ] );
      final ClassLoader classLoader = new URLClassLoader( classpath, loader );
      inContextClassLoader( classLoader, () -> {
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
        doStart();

        // Need to set java.security.auth.login.config otherwise the embedded container will
        // fail to find the login config ... even though it creates it...
        final String instanceRoot = System.getProperty( "com.sun.aas.instanceRoot" );
        System.setProperty( "java.security.auth.login.config",
                            instanceRoot + File.separator + "config" + File.separator + "login.conf" );
      } );

      LOG.info( "GlassFish started." );
    }
    else
    {
      LOG.warning( "Attempted to start already started GlassFish instance." );
    }
  }

  private void doStart()
    throws Exception
  {
    inContextClassLoader( () -> _glassfish.getClass().getMethod( "start" ).invoke( _glassfish ) );
  }

  public void restart()
    throws Exception
  {
    doStop();
    doStart();
  }

  public void stop()
  {
    if ( null != _glassfish )
    {
      LOG.info( "Stopping GlassFish." );
      try
      {
        doStop();
        inContextClassLoader( () -> _glassfish.getClass().getMethod( "dispose" ).invoke( _glassfish ) );
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

  private void doStop()
    throws Exception
  {
    inContextClassLoader( () -> _glassfish.getClass().getMethod( "stop" ).invoke( _glassfish ) );
  }

  @Nonnull
  public String deploy( @Nonnull final String contextRoot, @Nonnull final String appName, @Nonnull final File warFile )
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

  public void createPostgresJdbcResource( @Nonnull final String key, @Nonnull final Properties properties )
    throws Exception
  {
    createJdbcResource( key, "org.postgresql.ds.PGSimpleDataSource", properties );
  }

  public void createPostgresJdbcResource( @Nonnull final String key )
    throws Exception
  {
    createPostgresJdbcResource( key, (String) null );
  }

  public void createPostgresJdbcResource( @Nonnull final String key, @Nullable final String databasePrefix )
    throws Exception
  {
    createPostgresJdbcResource( key, DatabaseUtil.getGlassFishDataSourceProperties( databasePrefix ) );
  }

  public void createSqlServerJdbcResource( @Nonnull final String key, @Nonnull final Properties properties )
    throws Exception
  {
    createJdbcResource( key, "net.sourceforge.jtds.jdbcx.JtdsDataSource", properties );
  }

  public void createSqlServerJdbcResource( @Nonnull final String key )
    throws Exception
  {
    createSqlServerJdbcResource( key, (String) null );
  }

  public void createSqlServerJdbcResource( @Nonnull final String key, @Nullable final String databasePrefix )
    throws Exception
  {
    createSqlServerJdbcResource( key, DatabaseUtil.getGlassFishDataSourceProperties( databasePrefix ) );
  }

  @Nonnull
  public String toGlassFishPropertiesString( @Nonnull final Properties properties )
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

  public void createJdbcResource( @Nonnull final String key,
                                  @Nonnull final String dataSourceClassName,
                                  @Nonnull final Properties properties )
    throws Exception
  {
    createJdbcResource( key, dataSourceClassName, toGlassFishPropertiesString( properties ) );
  }

  private void createJdbcResource( @Nonnull final String key,
                                   @Nonnull final String dataSourceClassName,
                                   @Nonnull final String databaseConnectionProperty )
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

  public void createCustomResource( @Nonnull final String key, @Nonnull final String value )
    throws Exception
  {
    createCustomResource( "java.lang.String", key, value );
  }

  public void createCustomResource( @Nonnull final String key, final boolean value )
    throws Exception
  {
    createCustomResource( "java.lang.Boolean", key, String.valueOf( value ) );
  }

  public void createCustomResource( @Nonnull final String key, final long value )
    throws Exception
  {
    createCustomResource( "java.lang.Long", key, String.valueOf( value ) );
  }

  public void createCustomResource( @Nonnull final String key, final short value )
    throws Exception
  {
    createCustomResource( "java.lang.Short", key, String.valueOf( value ) );
  }

  public void createCustomResource( @Nonnull final String type, @Nonnull final String key, final String value )
    throws Exception
  {
    LOG.info( "Creating custom resource: " + key + "=" + value );
    execute( "create-custom-resource",
             "--factoryclass", "org.glassfish.resources.custom.factory.PrimitivesAndStringFactory",
             "--restype", type,
             "--property", "value=" + value.replace( ":", "\\:" ),
             key );
  }

  public void set( @Nonnull final String key, @Nonnull final String value )
    throws Exception
  {
    execute( "set", key + "=" + value );
  }

  public void createLocalIiopListener( @Nonnull final String key )
    throws Exception
  {
    createIiopListener( key, InetAddress.getLocalHost().getHostAddress(), GlassFishContainerUtil.getRandomPort() );
  }

  public void createIiopListener( @Nonnull final String key, @Nonnull final String hostAddress, final int port )
    throws Exception
  {
    execute( "create-iiop-listener",
             "--listeneraddress", hostAddress,
             "--iiopport", String.valueOf( port ),
             "--securityenabled", "false",
             "--enabled", "true",
             key );
  }

  public void deleteIiopListener( @Nonnull final String key )
    throws Exception
  {
    execute( "delete-iiop-listener", key );
  }

  public void deleteDefaultIiopListeners()
    throws Exception
  {
    deleteIiopListener( "orb-listener-1" );
    deleteIiopListener( "SSL" );
    deleteIiopListener( "SSL_MUTUALAUTH" );
  }

  public void deleteJmsHost( @Nonnull final String key )
    throws Exception
  {
    execute( "delete-jms-host", key );
  }

  public void setDefaultJmsHost( @Nonnull final String key,
                                 @Nonnull final OpenMQContainer container )
    throws Exception
  {
    createJmsHost( key, container );
    setAsDefaultJmsHost( key );
    deleteDefaultJmsHost();
    restart();
  }

  public void deleteDefaultJmsHost()
    throws Exception
  {
    deleteJmsHost( "default_JMS_host" );
  }

  public void setAsDefaultJmsHost( @Nonnull final String key )
    throws Exception
  {
    set( "configs.config.server-config.jms-service.type", "REMOTE" );
    set( "configs.config.server-config.jms-service.default-jms-host", key );
  }

  public void createJmsHost( @Nonnull final String key,
                             @Nonnull final OpenMQContainer container )
    throws Exception
  {
    createJmsHost( key, container.getHostAddress(), container.getPort() );
  }

  public void createJmsHost( @Nonnull final String key,
                             @Nonnull final String hostAddress,
                             final int port )
    throws Exception
  {
    createJmsHost( key, hostAddress, port, "admin", "admin" );
  }

  public void createJmsHost( @Nonnull final String key,
                             @Nonnull final String hostAddress,
                             final int port,
                             @Nonnull final String adminUser,
                             @Nonnull final String adminPassword )
    throws Exception
  {
    execute( "create-jms-host",
             "--mqhost", hostAddress,
             "--mqport", String.valueOf( port ),
             "--mquser", adminUser,
             "--mqpassword", adminPassword,
             key );
  }

  public void createJmsConnectionFactory( @Nonnull final String key,
                                          @Nonnull final OpenMQContainer container )
    throws Exception
  {
    createJmsConnectionFactory( key, container.getHostAddress(), container.getPort() );
  }

  public void createJmsConnectionFactory( @Nonnull final String key,
                                          @Nonnull final String hostAddress,
                                          final int port )
    throws Exception
  {
    createJmsConnectionFactory( key, hostAddress, port, "admin", "admin" );
  }

  public void createJmsConnectionFactory( @Nonnull final String key,
                                          @Nonnull final String hostAddress,
                                          final int port,
                                          @Nonnull final String adminUser,
                                          @Nonnull final String adminPassword )
    throws Exception
  {
    execute( "create-jms-resource",
             "--restype", "javax.jms.ConnectionFactory",
             "--property",
             "UserName=" + adminUser +
             ":Password=" + adminPassword +
             ":AddressList=" + hostAddress + "\\:" + port,
             key );
  }

  public void createJmsTopic( @Nonnull final String key,
                              @Nonnull final String queue )
    throws Exception
  {
    createJmsDestination( "javax.jms.Topic", key, queue );
  }

  public void createJmsQueue( @Nonnull final String key,
                              @Nonnull final String queue )
    throws Exception
  {
    createJmsDestination( "javax.jms.Queue", key, queue );
  }

  public void createJmsDestination( @Nonnull final String type,
                                    @Nonnull final String key,
                                    @Nonnull final String physicalName )
    throws Exception
  {
    execute( "create-jms-resource", "--restype", type, "--property", "Name=" + physicalName, key );
  }

  public void createManagedScheduledExecutorService( @Nonnull final String jndiName )
    throws Exception
  {
    execute( "create-managed-scheduled-executor-service", jndiName );
  }

  public void createManagedExecutorService( @Nonnull final String jndiName )
    throws Exception
  {
    execute( "create-managed-executor-service", jndiName );
  }

  public void createManagedThreadFactory( @Nonnull final String jndiName )
    throws Exception
  {
    execute( "create-managed-thread-factory", jndiName );
  }

  public void createContextService( @Nonnull final String jndiName )
    throws Exception
  {
    execute( "create-context-service", jndiName );
  }

  public void createJavamailResource( @Nonnull final String key,
                                      @Nonnull final String mailhost,
                                      @Nonnull final String mailuser,
                                      @Nonnull final String fromaddress )
    throws Exception
  {
    execute( "create-javamail-resource",
             "--mailhost", mailhost,
             "--mailuser", mailuser,
             "--fromaddress", fromaddress,
             key );
  }

  public void createUser( @Nonnull final String username,
                          @Nonnull final String password,
                          @Nonnull final String[] groups )
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

  public final String execute( @Nonnull final String command, @Nonnull final String... args )
    throws Exception
  {
    final AtomicReference<String> result = new AtomicReference<>();
    inContextClassLoader( () -> {
      final Object runner = _glassfish.getClass().getMethod( "getCommandRunner" ).invoke( _glassfish );
      final Object commandResult =
        runner.getClass()
          .getMethod( "run", new Class[]{ String.class, String[].class } )
          .invoke( runner, command, args );

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
        // Remove "PlainTextActionReporter" magic string, then SUCCESS, and should be left with actual output
        result.set( output.substring( "PlainTextActionReporterSUCCESS".length() ) );
      }
    } );
    return result.get();
  }

  interface Action
  {
    void call()
      throws Exception;
  }

  private <T> void inContextClassLoader( @Nonnull final Action action )
    throws Exception
  {
    inContextClassLoader( _glassfish.getClass().getClassLoader(), action );
  }

  private <T> void inContextClassLoader( @Nonnull final ClassLoader classLoader, @Nonnull final Action action )
    throws Exception
  {
    final Thread thread = Thread.currentThread();
    final ClassLoader existing = thread.getContextClassLoader();
    try
    {
      thread.setContextClassLoader( classLoader );
      action.call();
    }
    finally
    {
      thread.setContextClassLoader( existing );
    }
  }

  @SuppressWarnings( "unchecked" )
  private <T> T invokeMethod( @Nonnull final Object object, @Nonnull final String methodName )
    throws Exception
  {
    final Method method = object.getClass().getDeclaredMethod( methodName );
    method.setAccessible( true );
    return (T) method.invoke( object );
  }
}
