package org.realityforge.guiceyloops.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import javax.annotation.Nonnull;
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
 * <td>net.sourceforge.jtds.jdbc.Driver</td>
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
  static final String MSSQL_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
  static final String DEFAULT_DRIVER = MSSQL_DRIVER;

  static final String DB_URL_SYS_PROPERTY = "test.db.url";
  static final String DEFAULT_URL = "DATABASE_URL_UNSET";

  static final String DB_USER_SYS_PROPERTY = "test.db.user";
  static final String DEFAULT_USER = null;

  static final String DB_PASSWORD_SYS_PROPERTY = "test.db.password";
  static final String DEFAULT_PASSWORD = null;

  private DatabaseUtil()
  {
  }

  private static final Properties c_additionalPersistenceUnitProperties = new Properties();

  static Properties initDatabaseProperties()
  {
    final Properties properties = new Properties();
    setProperty( properties, DRIVER_KEY, DB_DRIVER_SYS_PROPERTY, DEFAULT_DRIVER );
    setProperty( properties, URL_KEY, DB_URL_SYS_PROPERTY, DEFAULT_URL );
    setProperty( properties, USER_KEY, DB_USER_SYS_PROPERTY, DEFAULT_USER );
    setProperty( properties, PASSWORD_KEY, DB_PASSWORD_SYS_PROPERTY, DEFAULT_PASSWORD );
    return properties;
  }

  public static void setAdditionalPersistenceUnitProperties( @Nonnull final Properties properties )
  {
    c_additionalPersistenceUnitProperties.clear();
    c_additionalPersistenceUnitProperties.putAll( properties );
  }

  static Properties initPersistenceUnitProperties()
  {
    final Properties properties = initDatabaseProperties();
    properties.put( "javax.persistence.transactionType", "RESOURCE_LOCAL" );
    properties.put( "javax.persistence.jtaDataSource", "" );
    properties.putAll( c_additionalPersistenceUnitProperties );
    return properties;
  }

  private static void setProperty( final Properties properties,
                                   final String key,
                                   final String systemPropertyKey,
                                   final String defaultValue )
  {
    final String value = System.getProperty( systemPropertyKey, defaultValue );
    if ( null != value )
    {
      properties.put( key, value );
    }
  }

  public static EntityManager createEntityManager( final String persistenceUnitName )
  {
    final Properties properties = initPersistenceUnitProperties();
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
    final Properties properties = initDatabaseProperties();
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
