package org.realityforge.guiceyloops.server;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import static org.testng.Assert.*;

public final class DatabaseAsserts
{
  private DatabaseAsserts()
  {
  }

  /**
   * Executes the given <code>sql</code> statement and verifies that it matches the <code>expectations</code>.
   *
   * <p>When the given <code>sql</code> does not return any result sets, expectations must be an empty array.
   * However, in such a case, it makes sense to use the variation of execute that only takes a sql statement
   * and executes it without any extra verifications.</p>
   *
   * <p>The number of expectations must match the number of rows returned by the query.</p>
   *
   * <p>Example usage:</p>
   * <pre>{@code
   * public class EntityTest extends AbstractDatabaseTest
   * {
   *  &#064;Test
   *  public void exampleTest() throws SQLException
   *  {
   *    HashMap<String, Matcher> map = new HashMap<String, Matcher>();
   *    map.put( "ID", is( equalTo( 12 ) ) );
   *    map.put( "EntityTypeID", is( equalTo( 10 ) ) );
   *    map.put( "Name", is( equalTo( "Evans, Michael (Mick)" ) ) );
   *    map.put( "DataSourceID", is( anything() ) );
   *    map.put( "FillTypeID", is( anything() ) );
   *    map.put( "StartedAt", is( anything() ) );
   *    execute( "SELECT * FROM Entity.vwEntity where ID = 12", map );
   *  }
   * }
   * }</pre>
   *
   * @param sql          the query under test
   * @param expectations expected matches, in order, one item per row
   * @return <code>true</code> if the first result is a <code>ResultSet</code> object; <code>false</code> if it is an update count or there are no results
   * @throws SQLException if a database access error occurs
   */
  @SafeVarargs
  public static boolean execute( @Nonnull final Connection connection,
                                 @Nonnull final String sql,
                                 final boolean orderImportant,
                                 @Nonnull final Map<String, Matcher<Object>>... expectations )
    throws SQLException
  {
    try ( Statement statement = connection.createStatement() )
    {
      final boolean hasResultSet = statement.execute( sql );
      if ( hasResultSet )
      {
        try ( ResultSet resultSet = statement.getResultSet() )
        {
          assertResultSet( resultSet, orderImportant, expectations );
        }
      }
      else
      {
        // No result sets are returned, expectations must be empty
        if ( expectations.length != 0 )
        {
          throw new AssertionError( "No ResultSets were returned" );
        }
      }

      return hasResultSet;
    }
  }

  /**
   * Executes the given <code>sql</code> statement and verifies that it matches the <code>expectations</code>.
   *
   * <p>When the given <code>sql</code> does not return any result sets, expectations must be an empty array.
   * However, in such a case, it makes sense to use the variation of execute that only takes a sql statement
   * and executes it without any extra verifications.</p>
   *
   * <p>The number of expectations must match the number of rows returned by the query.</p>
   *
   * <p>Example usage:</p>
   * <pre>{@code
   * public class EntityTest extends AbstractDatabaseTest
   * {
   *  &#064;Test
   *  public void exampleTest() throws SQLException
   *  {
   *    HashMap<String, Matcher> map = new HashMap<String, Matcher>();
   *    map.put( "ID", is( equalTo( 12 ) ) );
   *    map.put( "EntityTypeID", is( equalTo( 10 ) ) );
   *    map.put( "Name", is( equalTo( "Evans, Michael (Mick)" ) ) );
   *    map.put( "DataSourceID", is( anything() ) );
   *    map.put( "FillTypeID", is( anything() ) );
   *    map.put( "StartedAt", is( anything() ) );
   *    assertResultSet( connection().createStatement().executeQuery( "SELECT * FROM Entity.vwEntity where ID = 12" ), false, map );
   *  }
   * }
   * }</pre>
   *
   * @param resultSet      the resultSet to verify
   * @param orderImportant true if expectations should appear in order specified
   * @param expectations   expected matches, in order, one item per row
   * @throws SQLException if a database access error occurs
   */
  @SafeVarargs
  @SuppressWarnings( "varargs" )
  public static void assertResultSet( @Nonnull final ResultSet resultSet,
                                      final boolean orderImportant,
                                      @Nonnull final Map<String, Matcher<Object>>... expectations )
    throws SQLException
  {
    final ResultSetMetaData metaData = resultSet.getMetaData();
    final int columnCount = metaData.getColumnCount();

    final List<Map<String, Matcher<Object>>> candidates =
      orderImportant ? null : new ArrayList<>( Arrays.asList( expectations ) );

    int rowNumber = 0;
    while ( resultSet.next() )
    {
      if ( rowNumber >= expectations.length )
      {
        final String message =
          "Number of returned rows exceed the number of expectations. " +
          "Pass one expectation map per each row returning";
        throw new AssertionError( message );
      }

      if ( orderImportant )
      {
        final Map<String, Matcher<Object>> rowExpectation = expectations[ rowNumber ];
        if ( rowExpectation.size() != columnCount )
        {
          final String message = "Column count of the returned result set does not match the number of " +
                                 "expectation entries. Pass one entry per each column of the result set. " +
                                 "Number of columns: " + columnCount + ", number of expectation entries: " +
                                 rowExpectation.size();
          throw new AssertionError( message );
        }

        for ( final Map.Entry<String, Matcher<Object>> colExpectation : rowExpectation.entrySet() )
        {
          final Object val = resultSet.getObject( colExpectation.getKey() );
          final Matcher<Object> matcher = colExpectation.getValue();
          assertThat( val, matcher );
        }
      }
      else
      {
        boolean found = false;
        for ( final Map<String, Matcher<Object>> candidate : candidates )
        {
          if ( doesMatch( resultSet, candidate ) )
          {
            candidates.remove( candidate );
            found = true;
            break;
          }
        }

        if ( !found )
        {
          fail( "Unable to locate row: " + toRowData( resultSet ) + "\nMatchers Remaining: " + candidates );
        }
      }

      rowNumber += 1;
    }

    if ( !orderImportant && !candidates.isEmpty() )
    {
      fail( "Unmet expectations: " + candidates );
    }
  }

  @Nonnull
  private static Map<String, Object> toRowData( @Nonnull final ResultSet resultSet )
    throws SQLException
  {
    final Map<String, Object> results = new HashMap<>();
    final ResultSetMetaData metaData = resultSet.getMetaData();
    final int columnCount = metaData.getColumnCount();
    for ( int i = 1; i <= columnCount; i++ )
    {
      results.put( metaData.getColumnName( i ), resultSet.getObject( i ) );
    }
    return results;
  }

  private static boolean doesMatch( @Nonnull final ResultSet resultSet,
                                    @Nonnull final Map<String, Matcher<Object>> expectations )
    throws SQLException
  {
    boolean matched = true;
    for ( final Map.Entry<String, Matcher<Object>> colExpectation : expectations.entrySet() )
    {
      Object val = resultSet.getObject( colExpectation.getKey() );
      if ( val instanceof Clob )
      {
        final Clob clob = (Clob) val;
        val = clob.getSubString( 1, (int) clob.length() );
      }
      final Matcher<Object> matcher = colExpectation.getValue();
      matched &= matcher.matches( val );
    }
    return matched;
  }

  @SafeVarargs
  public static boolean execute( @Nonnull final Connection connection,
                                 @Nonnull final String sql,
                                 @Nonnull final Map<String, Matcher<Object>>... expectations )
    throws SQLException
  {
    return execute( connection, sql, false, expectations );
  }

  /**
   * Runs the given <code>sql</code> query and verifies that it returns the same number of rows
   * as <code>expectedRowCount</code>.
   *
   * <p>When -1 is passed as <code>expectedRowCount</code>, no row count checking will be performed.</p>
   *
   * @param sql              the query to test
   * @param expectedRowCount the expected size of the result set
   * @return <code>true</code> if the first result is a <code>ResultSet</code> object; <code>false</code> if it is an update count or there are no results
   * @throws SQLException if a database access error occurs
   */
  public static boolean execute( @Nonnull final Connection connection,
                                 @Nonnull final String sql,
                                 final int expectedRowCount )
    throws SQLException
  {
    try ( final Statement statement = connection.createStatement() )
    {
      final boolean hasResultSet = statement.execute( sql );
      int rowCount = 0;
      if ( hasResultSet )
      {
        final ResultSet resultSet = statement.getResultSet();
        while ( resultSet.next() )
        {
          rowCount += 1;
        }
      }

      if ( expectedRowCount > -1 )
      {
        assertEquals( rowCount, expectedRowCount );
      }

      return hasResultSet;
    }
  }

  public static boolean execute( @Nonnull final Connection connection, @Nonnull final String sql )
    throws SQLException
  {
    return execute( connection, sql, -1 );
  }

  public static void executeAndFail( @Nonnull final Connection connection,
                                     @Nonnull final String sql,
                                     @Nullable final Matcher<String> messageMatcher )
  {
    try
    {
      execute( connection, sql, -1 );
      fail( "Unexpectedly successful executing sql: " + sql );
    }
    catch ( final SQLException e )
    {
      if ( null != messageMatcher )
      {
        assertThat( e.getMessage(), messageMatcher );
      }
    }
  }

  private static <T> void assertThat( @Nullable final T result, @Nonnull final Matcher<T> matcher )
  {
    assertTrue( matcher.matches( result ), StringDescription.asString( matcher ) );
  }
}
