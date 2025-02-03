package org.realityforge.guiceyloops.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * <p>Utilities for initializing database connections within tests.</p>
 * <p>The database connection uses a resource local transaction type and
 * gets the database characteristics from a system property. The following
 * table indicates the lookup keys for each property.</p>
 * <table>
 * <caption>System properties</caption>
 * <tr>
 * <th>Property</th>
 * <th>System property</th>
 * <th>Default value</th>
 * </tr>
 * <tr>
 * <td>javax.persistence.jdbc.url</td>
 * <td>test.db.url</td>
 * <td>DATABASE_URL_UNSET</td>
 * </tr>
 * <tr>
 * <td>javax.persistence.jdbc.driver</td>
 * <td>test.db.driver</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>javax.persistence.jdbc.user</td>
 * <td>test.db.user</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>javax.persistence.jdbc.password</td>
 * <td>test.db.password</td>
 * <td></td>
 * </tr>
 * </table>
 */
public final class DatabaseUtil
{
  @Nonnull
  static final String URL_KEY = "javax.persistence.jdbc.url";
  @Nonnull
  static final String DRIVER_KEY = "javax.persistence.jdbc.driver";
  @Nonnull
  static final String USER_KEY = "javax.persistence.jdbc.user";
  @Nonnull
  static final String PASSWORD_KEY = "javax.persistence.jdbc.password";
  @Nonnull
  static final String DB_PROPERTY_FILE = "test.db.property_file";
  @Nonnull
  static final String DB_DRIVER_SYS_PROPERTY = "test.db.driver";
  @Nonnull
  static final String DEFAULT_DRIVER = "DATABASE_DRIVER_UNSET";
  @Nonnull
  static final String DB_URL_SYS_PROPERTY = "test.db.url";
  @Nonnull
  static final String DEFAULT_URL = "DATABASE_URL_UNSET";
  @Nonnull
  static final String DB_USER_SYS_PROPERTY = "test.db.user";
  @Nullable
  static final String DEFAULT_USER = null;
  @Nonnull
  static final String DB_PASSWORD_SYS_PROPERTY = "test.db.password";
  @Nullable
  static final String DEFAULT_PASSWORD = null;
  @Nonnull
  public static final String JTDS_SQL_SERVER_JDBC_URL_PREFIX = "jdbc:jtds:sqlserver://";
  @Nonnull
  public static final String POSTGRES_SERVER_JDBC_URL_PREFIX = "jdbc:postgresql://";

  private DatabaseUtil()
  {
  }

  @Nonnull
  private static final Properties c_additionalPersistenceUnitProperties = new Properties();

  @Nonnull
  static Properties initDatabaseProperties( @Nullable final String databasePrefix )
  {
    final String qualifiedPrefix = null == databasePrefix ? "" : databasePrefix + ".";
    final Properties sourceProperties = getSourceProperties();
    final Properties targetProperties = new Properties();

    setProperty( sourceProperties,
                 DB_DRIVER_SYS_PROPERTY,
                 targetProperties,
                 DRIVER_KEY,
                 qualifiedPrefix,
                 qualifiedPrefix + DEFAULT_DRIVER );
    setProperty( sourceProperties,
                 DB_URL_SYS_PROPERTY,
                 targetProperties,
                 URL_KEY,
                 qualifiedPrefix,
                 qualifiedPrefix + DEFAULT_URL );
    setProperty( sourceProperties,
                 DB_USER_SYS_PROPERTY,
                 targetProperties,
                 USER_KEY,
                 qualifiedPrefix,
                 DEFAULT_USER );
    setProperty( sourceProperties,
                 DB_PASSWORD_SYS_PROPERTY,
                 targetProperties,
                 PASSWORD_KEY,
                 qualifiedPrefix,
                 DEFAULT_PASSWORD );
    return targetProperties;
  }

  @Nonnull
  private static Properties getSourceProperties()
  {
    final String propertyFilename = System.getProperty( DB_PROPERTY_FILE, null );
    if ( null != propertyFilename )
    {
      try
      {
        final Properties sourceProperties = new Properties();
        sourceProperties.load( new FileInputStream( propertyFilename ) );
        return sourceProperties;
      }
      catch ( IOException e )
      {
        throw new RuntimeException( "Failed to load database properties from file: " + propertyFilename );
      }
    }
    else
    {
      return System.getProperties();
    }
  }

  public static void setAdditionalPersistenceUnitProperties( @Nonnull final Properties properties )
  {
    c_additionalPersistenceUnitProperties.clear();
    c_additionalPersistenceUnitProperties.putAll( properties );
  }

  @Nonnull
  static Properties initPersistenceUnitProperties( @Nullable final String databasePrefix )
  {
    final Properties properties = initDatabaseProperties( databasePrefix );
    properties.put( "javax.persistence.transactionType", "RESOURCE_LOCAL" );
    properties.put( "javax.persistence.jtaDataSource", "" );
    properties.putAll( c_additionalPersistenceUnitProperties );
    return properties;
  }

  private static void setProperty( @Nonnull final Properties sourceProperties,
                                   @Nonnull final String sourceKey,
                                   @Nonnull final Properties targetProperties,
                                   @Nonnull final String targetKey,
                                   @Nullable final String prefix,
                                   @Nullable final String defaultValue )
  {
    final String value = sourceProperties.getProperty( prefix + sourceKey, defaultValue );
    if ( null != value )
    {
      targetProperties.put( targetKey, value );
    }
  }

  @Nonnull
  public static Properties getGlassFishDataSourceProperties()
  {
    return getGlassFishDataSourceProperties( null );
  }

  @Nonnull
  public static Properties getGlassFishDataSourceProperties( @Nullable final String databasePrefix )
  {
    final Properties properties = initDatabaseProperties( databasePrefix );

    final Properties gfProperties = new Properties();
    setProperty( gfProperties, "User", properties.getProperty( USER_KEY ) );
    setProperty( gfProperties, "Password", properties.getProperty( PASSWORD_KEY ) );
    final String jdbcUrl = properties.getProperty( URL_KEY );
    if ( jdbcUrl.startsWith( JTDS_SQL_SERVER_JDBC_URL_PREFIX ) )
    {
      parseSqlServerURL( gfProperties, jdbcUrl );
    }
    else if ( jdbcUrl.startsWith( POSTGRES_SERVER_JDBC_URL_PREFIX ) )
    {
      parsePostgresURL( gfProperties, jdbcUrl );
    }
    else
    {
      throw new IllegalArgumentException( "Can not yet parse jdbc url of the form: " + jdbcUrl );
    }
    return gfProperties;
  }

  private static void parseSqlServerURL( @Nonnull final Properties gfProperties, @Nonnull final String jdbcUrl )
  {
    final int paramSeparator = jdbcUrl.indexOf( ";" );
    if ( -1 != paramSeparator )
    {
      final String[] params = jdbcUrl.substring( paramSeparator + 1 ).split( ";" );
      for ( final String param : params )
      {
        final String[] components = param.split( "=" );
        if ( "user".equals( components[ 0 ] ) )
        {
          setProperty( gfProperties, "User", components[ 1 ] );
        }
        else if ( "password".equals( components[ 0 ] ) )
        {
          setProperty( gfProperties, "Password", components[ 1 ] );
        }
        else if ( "instance".equals( components[ 0 ] ) )
        {
          setProperty( gfProperties, "Instance", components[ 1 ] );
        }
      }
    }

    final int prefixEnd = JTDS_SQL_SERVER_JDBC_URL_PREFIX.length();
    final int hostEnd = jdbcUrl.indexOf( "/", prefixEnd );

    final String databaseName =
      jdbcUrl.substring( hostEnd + 1, ( -1 == paramSeparator ? jdbcUrl.length() : paramSeparator ) );
    setProperty( gfProperties, "DatabaseName", databaseName );

    final int portStart = jdbcUrl.indexOf( ":", prefixEnd );
    if ( -1 != portStart && portStart < hostEnd )
    {
      final String portString = jdbcUrl.substring( portStart + 1, hostEnd );
      setProperty( gfProperties, "PortNumber", portString );
    }

    final String serverName = jdbcUrl.substring( prefixEnd, ( -1 == portStart ? hostEnd : portStart ) );
    setProperty( gfProperties, "ServerName", serverName );

    setProperty( gfProperties, "jdbc30DataSource", "true" );
  }

  private static void parsePostgresURL( @Nonnull final Properties gfProperties, @Nonnull final String jdbcUrl )
  {
    final int paramSeparator = jdbcUrl.indexOf( "?" );
    if ( -1 != paramSeparator )
    {
      final String[] params = jdbcUrl.substring( paramSeparator + 1 ).split( "&" );
      for ( final String param : params )
      {
        final String[] components = param.split( "=" );
        if ( "user".equals( components[ 0 ] ) )
        {
          setProperty( gfProperties, "User", components[ 1 ] );
        }
        else if ( "password".equals( components[ 0 ] ) )
        {
          setProperty( gfProperties, "Password", components[ 1 ] );
        }
      }
    }

    final int prefixEnd = POSTGRES_SERVER_JDBC_URL_PREFIX.length();
    final int hostEnd = jdbcUrl.indexOf( "/", prefixEnd );

    final String databaseName =
      jdbcUrl.substring( hostEnd + 1, ( -1 == paramSeparator ? jdbcUrl.length() : paramSeparator ) );
    setProperty( gfProperties, "DatabaseName", databaseName );

    final int portStart = jdbcUrl.indexOf( ":", prefixEnd );
    if ( -1 != portStart && portStart < hostEnd )
    {
      final String portString = jdbcUrl.substring( portStart + 1, hostEnd );
      setProperty( gfProperties, "PortNumber", portString );
    }

    final String serverName = jdbcUrl.substring( prefixEnd, ( -1 == portStart ? hostEnd : portStart ) );
    setProperty( gfProperties, "ServerName", serverName );
  }

  private static void setProperty( @Nonnull final Properties properties,
                                   @Nonnull final String key,
                                   @Nullable final String value )
  {
    if ( null != value )
    {
      properties.setProperty( key, value );
    }
    else
    {
      properties.remove( key );
    }
  }

  @Nonnull
  public static EntityManager createEntityManager( @Nonnull final String persistenceUnitName )
  {
    return createEntityManager( persistenceUnitName, null );
  }

  @Nonnull
  public static EntityManager createEntityManager( @Nonnull final String persistenceUnitName,
                                                   @Nullable final String databasePrefix )
  {
    return createEntityManager( persistenceUnitName, databasePrefix, null );
  }

  @Nonnull
  public static EntityManager createEntityManager( @Nonnull final String persistenceUnitName,
                                                   @Nullable final String databasePrefix,
                                                   @Nullable final Properties additionalDatabaseProperties )
  {
    final Properties properties = initPersistenceUnitProperties( databasePrefix );
    if ( null != additionalDatabaseProperties )
    {
      properties.putAll( additionalDatabaseProperties );
    }
    final EntityManagerFactory factory = Persistence.createEntityManagerFactory( persistenceUnitName, properties );
    return factory.createEntityManager();
  }

  /**
   * Dispose the specified database connection.
   *
   * @param connection the database connection.
   */
  public static void disposeConnection( @Nullable final Connection connection )
  {
    if ( null != connection )
    {
      try
      {
        connection.close();
      }
      catch ( final Exception e )
      {
        throw new IllegalStateException( e.getMessage(), e );
      }
    }
  }

  /**
   * Create a direct database connection. The user should follow with a subsequent {@link #disposeConnection(java.sql.Connection)} call.
   *
   * @return the database connection.
   */
  @Nonnull
  public static Connection initConnection()
  {
    return initConnection( null );
  }

  /**
   * Create a direct database connection. The user should follow with a subsequent {@link #disposeConnection(java.sql.Connection)} call.
   *
   * @return the database connection.
   */
  @Nonnull
  public static Connection initConnection( @Nullable final String databasePrefix )
  {
    final Properties properties = initDatabaseProperties( databasePrefix );
    try
    {
      Class.forName( properties.getProperty( DRIVER_KEY ) );
    }
    catch ( final Exception e )
    {
      throw new IllegalStateException( e.getMessage(), e );
    }
    try
    {
      return DriverManager.getConnection( properties.getProperty( URL_KEY ),
                                          properties.getProperty( USER_KEY ),
                                          properties.getProperty( PASSWORD_KEY ) );
    }
    catch ( final Exception e )
    {
      throw new IllegalStateException( e.getMessage(), e );
    }
  }
}
