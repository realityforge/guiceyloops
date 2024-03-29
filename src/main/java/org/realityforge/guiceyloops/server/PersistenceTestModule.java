package org.realityforge.guiceyloops.server;

import com.google.inject.name.Names;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.eclipse.persistence.descriptors.DescriptorEventManager;
import org.eclipse.persistence.internal.jpa.metadata.listeners.EntityListener;
import org.eclipse.persistence.sessions.Session;
import org.realityforge.guiceyloops.shared.AbstractModule;

public abstract class PersistenceTestModule
  extends AbstractModule
{
  @Nonnull
  private final String _persistenceUnitName;
  @Nonnull
  private final String _bindName;
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
  private final String[] _tablesToClean;
  @Nullable
  private final String _databasePrefix;
  @Nullable
  private final Properties _additionalDatabaseProperties;
  @Nullable
  private EntityManager _entityManager;

  public PersistenceTestModule( @Nonnull final String persistenceUnitName, @Nonnull final String[] tablesToClean )
  {
    this( persistenceUnitName, tablesToClean, null );
  }

  public PersistenceTestModule( @Nonnull final String persistenceUnitName,
                                @Nonnull final String[] tablesToClean,
                                @Nullable final String databasePrefix )
  {
    this( persistenceUnitName, tablesToClean, databasePrefix, null );
  }

  public PersistenceTestModule( @Nonnull final String persistenceUnitName,
                                @Nonnull final String[] tablesToClean,
                                @Nullable final String databasePrefix,
                                @Nullable final Properties additionalDatabaseProperties )
  {
    this( persistenceUnitName,
          persistenceUnitName,
          new String[ 0 ],
          new String[ 0 ],
          tablesToClean,
          databasePrefix,
          additionalDatabaseProperties );
  }

  public PersistenceTestModule( @Nonnull final String persistenceUnitName,
                                @Nonnull final String[] preCleanSql,
                                @Nonnull final String[] postCleanSql,
                                @Nonnull final String[] tablesToClean,
                                @Nullable final String databasePrefix,
                                @Nullable final Properties additionalDatabaseProperties )
  {
    this( persistenceUnitName,
          persistenceUnitName,
          preCleanSql,
          postCleanSql,
          tablesToClean,
          databasePrefix,
          additionalDatabaseProperties );
  }
  public PersistenceTestModule( @Nonnull final String bindName,
                                @Nonnull final String persistenceUnitName,
                                @Nonnull final String[] preCleanSql,
                                @Nonnull final String[] postCleanSql,
                                @Nonnull final String[] tablesToClean,
                                @Nullable final String databasePrefix,
                                @Nullable final Properties additionalDatabaseProperties )
  {
    _bindName = Objects.requireNonNull( bindName );
    _persistenceUnitName = Objects.requireNonNull( persistenceUnitName );
    _preCleanSql = Objects.requireNonNull( preCleanSql );
    _postCleanSql = Objects.requireNonNull( postCleanSql );
    _tablesToClean = Objects.requireNonNull( tablesToClean );
    _databasePrefix = databasePrefix;
    _additionalDatabaseProperties = additionalDatabaseProperties;
  }

  @Nonnull
  protected final EntityManager getEntityManager()
  {
    assert null != _entityManager;
    return _entityManager;
  }

  /**
   * @return the name of the persistence unit.
   */
  @Nonnull
  protected final String getPersistenceUnitName()
  {
    return _persistenceUnitName;
  }

  /**
   * @return the prefix used to lookup database properties.
   */
  @Nullable
  protected final String getDatabasePrefix()
  {
    return _databasePrefix;
  }

  /**
   * Override this to further customize the persistence elements.
   */
  protected void configure()
  {
    _entityManager = DatabaseUtil.createEntityManager( _persistenceUnitName,
                                                       getDatabasePrefix(),
                                                       _additionalDatabaseProperties );
    bindResource( EntityManager.class, getBindName(), _entityManager );
    requestInjectionForAllEntityListeners();
    if ( 0 != _tablesToClean.length )
    {
      bind( DbCleaner.class ).
        annotatedWith( Names.named( getBindName() ) ).
        toInstance( new DbCleaner( _preCleanSql, _postCleanSql, _tablesToClean, getEntityManager() ) );
    }
  }

  /**
   * @return the name under which to bind EntityManager.
   */
  @Nonnull
  protected final String getBindName()
  {
    return _bindName;
  }

  /**
   * Request injection for entity listeners on all entities in persistence unit.
   */
  private void requestInjectionForAllEntityListeners()
  {
    assert null != _entityManager;
    final Session session = _entityManager.unwrap( Session.class );
    for ( final ClassDescriptor descriptor : session.getDescriptors().values() )
    {
      requestInjectionForEntityListeners( descriptor );
    }
  }

  private void requestInjectionForEntityListeners( @Nonnull final ClassDescriptor descriptor )
  {
    final DescriptorEventManager eventManager = descriptor.getEventManager();
    requestInjectionForEntityListeners( eventManager.getDefaultEventListeners() );
    requestInjectionForEntityListeners( eventManager.getEntityListenerEventListeners() );
  }

  @SuppressWarnings( "rawtypes" )
  private void requestInjectionForEntityListeners( @Nonnull final List<DescriptorEventListener> eventListeners )
  {
    for ( final DescriptorEventListener o : eventListeners )
    {
      if ( o instanceof EntityListener )
      {
        final EntityListener listener = (EntityListener) o;
        requestInjection( listener.getListener() );
      }
    }
  }
}
