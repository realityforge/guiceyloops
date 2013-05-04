package org.realityforge.guiceyloops.server;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
  public static final String TABLE_NAME_KEY = "TablesToClean";

  @Named( TABLE_NAME_KEY )
  @Inject
  private String[] _tableNames;

  @PersistenceContext
  private EntityManager _em;

  private boolean _inTransaction = false;
  private boolean _active = false;
  private boolean _clean = false;

  public boolean isActive()
  {
    return _active;
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

  private void clean()
  {
    if ( !_clean )
    {
      _em.getTransaction().begin();
      for ( final String tableName : _tableNames )
      {
        _em.createNativeQuery( "DELETE FROM " + tableName ).executeUpdate();
      }
      _em.getTransaction().commit();
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
