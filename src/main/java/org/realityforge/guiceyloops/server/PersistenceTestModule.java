package org.realityforge.guiceyloops.server;

import com.google.inject.name.Names;
import java.util.Properties;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventManager;
import org.eclipse.persistence.internal.jpa.metadata.listeners.EntityListener;
import org.eclipse.persistence.sessions.Session;
import org.realityforge.guiceyloops.shared.AbstractModule;

public abstract class PersistenceTestModule
  extends AbstractModule
{
  private final String _persistenceUnitName;
  private final String _bindName;
  private final String[] _tablesToClean;
  private final String _databasePrefix;
  private final Properties _additionalDatabaseProperties;
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
    this( persistenceUnitName, persistenceUnitName, tablesToClean, databasePrefix, additionalDatabaseProperties );
  }

  public PersistenceTestModule( @Nonnull final String bindName,
                                @Nonnull final String persistenceUnitName,
                                @Nonnull final String[] tablesToClean,
                                @Nullable final String databasePrefix,
                                @Nullable final Properties additionalDatabaseProperties )
  {
    _bindName = bindName;
    _persistenceUnitName = persistenceUnitName;
    _tablesToClean = tablesToClean;
    _databasePrefix = databasePrefix;
    _additionalDatabaseProperties = additionalDatabaseProperties;
  }

  protected final EntityManager getEntityManager()
  {
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
      requestCleaningOfTables( _tablesToClean );
    }
  }

  /**
   * @return the name under which to bind EntityManager.
   */
  protected final String getBindName()
  {
    return _bindName;
  }

  private void requestCleaningOfTables( @Nonnull final String[] tables )
  {
    bind( DbCleaner.class ).
      annotatedWith( Names.named( getBindName() ) ).
      toInstance( new DbCleaner( tables, getEntityManager() ) );
  }

  /**
   * Request injection for entity listeners on all entities in persistence unit.
   */
  private void requestInjectionForAllEntityListeners()
  {
    final Session session = _entityManager.unwrap( Session.class );
    for ( final ClassDescriptor descriptor : session.getDescriptors().values() )
    {
      requestInjectionForEntityListeners( descriptor );
    }
  }

  private void requestInjectionForEntityListeners( final ClassDescriptor descriptor )
  {
    final DescriptorEventManager eventManager = descriptor.getEventManager();
    requestInjectionForEntityListeners( eventManager.getDefaultEventListeners() );
    requestInjectionForEntityListeners( eventManager.getEntityListenerEventListeners() );
  }

  private void requestInjectionForEntityListeners( final Vector eventListeners )
  {
    for ( final Object o : eventListeners )
    {
      if ( o instanceof EntityListener )
      {
        final EntityListener listener = (EntityListener) o;
        requestInjection( listener.getListener( listener.getOwningSession() ) );
      }
    }
  }
}
