package org.realityforge.guiceyloops.server;

import com.google.inject.name.Names;
import java.util.List;
import java.util.Vector;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.transaction.UserTransaction;
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
    if ( shouldInjectEntityListeners() )
    {
      requestInjectionForAllEntityListeners();
    }
  }

  /**
   * @return true if entity listeners for all entities should be injected.
   */
  protected boolean shouldInjectEntityListeners()
  {
    return true;
  }

  protected void registerUserTransaction()
  {
    bind( UserTransaction.class ).toInstance( new TestUserTransaction( _entityManager ) );
  }

  @Nonnull
  protected String toQualifiedTableName( final Table annotation )
  {
    final String qualifiedTableName;
    final String tableName = annotation.name();
    qualifiedTableName =
      annotation.schema() + "." + ( tableName.startsWith( "vw" ) ? tableName.replace( "vw", "tbl" ) : tableName );
    return qualifiedTableName;
  }

  protected final void requestCleaningOfTables( @Nonnull final String[] tables )
  {
    bind( DbCleaner.class ).
      annotatedWith( Names.named( _persistenceUnitName ) ).
      toInstance( new DbCleaner( tables, getEntityManager() ) );
  }

  /**
   * Request injection for entity listeners on all entities in persistence unit.
   */
  protected final void requestInjectionForAllEntityListeners()
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

  protected final void collectTableName( final List<String> tables, final Class<?> model )
  {
    //Annotation null when we hit an abstract class
    final Table annotation = model.getAnnotation( Table.class );
    final String qualifiedTableName = null != annotation ? toQualifiedTableName( annotation ) : null;
    if ( null != qualifiedTableName )
    {
      tables.add( qualifiedTableName );
    }
  }
}
