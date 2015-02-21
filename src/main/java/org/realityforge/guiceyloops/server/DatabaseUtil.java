package org.realityforge.guiceyloops.server;

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
  static final String URL_KEY = "javax.persistence.jdbc.url";
  static final String DRIVER_KEY = "javax.persistence.jdbc.driver";
  static final String USER_KEY = "javax.persistence.jdbc.user";
  static final String PASSWORD_KEY = "javax.persistence.jdbc.password";

  static final String DB_DRIVER_SYS_PROPERTY = "test.db.driver";
  static final String DEFAULT_DRIVER = "DATABASE_DRIVER_UNSET";

  static final String DB_URL_SYS_PROPERTY = "test.db.url";
  static final String DEFAULT_URL = "DATABASE_URL_UNSET";

  static final String DB_USER_SYS_PROPERTY = "test.db.user";
  static final String DEFAULT_USER = null;

  static final String DB_PASSWORD_SYS_PROPERTY = "test.db.password";
  static final String DEFAULT_PASSWORD = null;
  public static final String JTDS_SQL_SERVER_JDBC_URL_PREFIX = "jdbc:jtds:sqlserver://";
  public static final String POSTGRES_SERVER_JDBC_URL_PREFIX = "jdbc:postgresql://";

  private DatabaseUtil()
  {
  }

  private static final Properties c_additionalPersistenceUnitProperties = new Properties();

  static Properties initDatabaseProperties( @Nullable final String databasePrefix )
  {
    final Properties properties = new Properties();
    setProperty( properties, databasePrefix, DRIVER_KEY, DB_DRIVER_SYS_PROPERTY, DEFAULT_DRIVER );
    setProperty( properties, databasePrefix, URL_KEY, DB_URL_SYS_PROPERTY, DEFAULT_URL );
    setProperty( properties, databasePrefix, USER_KEY, DB_USER_SYS_PROPERTY, DEFAULT_USER );
    setProperty( properties, databasePrefix, PASSWORD_KEY, DB_PASSWORD_SYS_PROPERTY, DEFAULT_PASSWORD );
    return properties;
  }

  public static void setAdditionalPersistenceUnitProperties( @Nonnull final Properties properties )
  {
    c_additionalPersistenceUnitProperties.clear();
    c_additionalPersistenceUnitProperties.putAll( properties );
  }

  static Properties initPersistenceUnitProperties( @Nullable final String databasePrefix )
  {
    final Properties properties = initDatabaseProperties( databasePrefix );
    properties.put( "javax.persistence.transactionType", "RESOURCE_LOCAL" );
    properties.put( "javax.persistence.jtaDataSource", "" );
    properties.putAll( c_additionalPersistenceUnitProperties );
    return properties;
  }

  private static void setProperty( @Nonnull final Properties properties,
                                   @Nullable final String databasePrefix,
                                   @Nonnull final String key,
                                   @Nonnull final String systemPropertyKey,
                                   @Nullable final String defaultValue )
  {
    final String prefix = null == databasePrefix ? "" : databasePrefix + ".";
    final String value = System.getProperty( prefix + systemPropertyKey, defaultValue );
    if ( null != value )
    {
      properties.put( key, value );
    }
  }

  public static Properties getGlassFishDataSourceProperties()
  {
    return getGlassFishDataSourceProperties( null );
  }

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

  private static void parseSqlServerURL( final Properties gfProperties, final String jdbcUrl )
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
  }

  private static void parsePostgresURL( final Properties gfProperties, final String jdbcUrl )
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

  public static EntityManager createEntityManager( @Nonnull final String persistenceUnitName )
  {
    return createEntityManager( persistenceUnitName, null );
  }

  public static EntityManager createEntityManager( @Nonnull final String persistenceUnitName,
                                                   @Nullable final String databasePrefix )
  {
    final Properties properties = initPersistenceUnitProperties( databasePrefix );
    final EntityManagerFactory factory = Persistence.createEntityManagerFactory( persistenceUnitName, properties );
    return factory.createEntityManager();
  }

  /**
   * Dispose the specified database connection.
   *
   * @param connection the database connection.
   */
  public static void disposeConnection( final Connection connection )
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
  public static Connection initConnection()
  {
    return initConnection( null );
  }

  /**
   * Create a direct database connection. The user should follow with a subsequent {@link #disposeConnection(java.sql.Connection)} call.
   *
   * @return the database connection.
   */
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
