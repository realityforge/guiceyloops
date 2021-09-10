package org.realityforge.guiceyloops.server;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

/**
 * Utility component to manage the database state during tests.
 * <p>The user should invoke {@link #start()} at the beginning of the test and {@link #finish()}. This will
 * create a transaction and rollback the transaction at the end of the test.</p>
 * <p>If the test makes use of
 * transactions then the user should invoke {@link #usesTransaction()} before any database interaction occurs.
 * This will allow the user to use transactions as desired and schedule a clean next time {@link #start()} is
 * invoked. The DbCleaner will also clean the database the first time {@link #start()} is invoked.
 * </p>
 * <p>Cleaning the database invokes deleting all rows from specified tables in the order specified.</p>
 */
public class DbCleaner
{
  /**
   * SQL to run prior to cleaning tables.
   */
  @Nonnull
  private final String[] _preCleanSql;
  /**
   * SQL to run after cleaning tables.
   */
  @Nonnull
  private final String[] _postCleanSql;
  @Nonnull
  private final String[] _tableNames;
  @Nonnull
  private final EntityManager _em;
  private boolean _inTransaction;
  private boolean _active;
  private boolean _clean;

  public DbCleaner( @Nonnull final String[] preCleanSql,
                    @Nonnull final String[] postCleanSql,
                    @Nonnull final String[] tableNames,
                    @Nonnull final EntityManager em )
  {
    _preCleanSql = Objects.requireNonNull( preCleanSql );
    _postCleanSql = Objects.requireNonNull( postCleanSql );
    _tableNames = Objects.requireNonNull( tableNames );
    _em = Objects.requireNonNull( em );
  }

  public boolean isActive()
  {
    return _active;
  }

  public boolean isTransactionActive()
  {
    return _inTransaction;
  }

  public boolean isClean()
  {
    return _clean;
  }

  public void usesTransaction()
  {
    if ( !_active )
    {
      throw new IllegalStateException( "Attempted to call uses_transaction without starting session" );
    }
    _clean = false;
    rollbackTransaction();
  }

  public void start()
  {
    if ( _active )
    {
      throw new IllegalStateException( "Attempted to start database cleaner without finishing last session" );
    }
    clean();
    _active = true;
    startTransaction();
  }

  public void finish()
  {
    if ( !_active )
    {
      throw new IllegalStateException( "Attempted to finish database cleaner without starting session" );
    }
    _active = false;
    rollbackTransaction();
  }

  @SuppressWarnings( { "SqlWithoutWhere", "SqlNoDataSourceInspection" } )
  private void clean()
  {
    if ( !_clean )
    {
      _em.getTransaction().begin();
      try
      {
        for ( final String sql : _preCleanSql )
        {
          _em.createNativeQuery( sql ).executeUpdate();
        }
        for ( final String tableName : _tableNames )
        {
          _em.createNativeQuery( "DELETE FROM " + tableName ).executeUpdate();
        }
        for ( final String sql : _postCleanSql )
        {
          _em.createNativeQuery( sql ).executeUpdate();
        }
      }
      finally
      {
        if ( _em.getTransaction().getRollbackOnly() )
        {
          _em.getTransaction().rollback();
        }
        else
        {
          _em.getTransaction().commit();
        }
      }
      _em.getEntityManagerFactory().getCache().evictAll();
      _clean = true;
    }
  }

  private void startTransaction()
  {
    _em.getTransaction().begin();
    _inTransaction = true;
  }

  private void rollbackTransaction()
  {
    if ( _inTransaction )
    {
      _em.getTransaction().rollback();
      _inTransaction = false;
    }
  }
}
