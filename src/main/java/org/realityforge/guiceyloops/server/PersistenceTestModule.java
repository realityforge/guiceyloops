package org.realityforge.guiceyloops.server;

import com.google.inject.name.Names;
import java.util.List;
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
  private EntityManager _entityManager;

  public PersistenceTestModule( @Nonnull final String persistenceUnitName )
  {
    _persistenceUnitName = persistenceUnitName;
  }

  protected final EntityManager getEntityManager()
  {
    return _entityManager;
  }

  /**
   * @return the prefix used to lookup database properties.
   */
  @Nullable
  protected String getDatabasePrefix()
  {
    return null;
  }

  /**
   * Override this to further customize the persistence elements.
   */
  protected void configure()
  {
    _entityManager = DatabaseUtil.createEntityManager( _persistenceUnitName, getDatabasePrefix() );
    bindResource( EntityManager.class, _persistenceUnitName, _entityManager );
    requestInjectionForAllEntityListeners();
    final String[] tables = getTablesToClean();
    if ( 0 != tables.length )
    {
      requestCleaningOfTables( tables );
    }
  }

  /**
   * Return the list of tables to clean. Return an empty array to skip
   * registering the DbCleaner for EntityManager.
   */
  @Nonnull
  protected abstract String[] getTablesToClean();

  private void requestCleaningOfTables( @Nonnull final String[] tables )
  {
    bind( DbCleaner.class ).
      annotatedWith( Names.named( _persistenceUnitName ) ).
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
      final EntityListener listener = (EntityListener) o;
      requestInjection( listener.getListener( listener.getOwningSession() ) );
    }
  }
}
