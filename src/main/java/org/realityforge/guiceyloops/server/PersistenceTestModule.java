package org.realityforge.guiceyloops.server;

import com.google.inject.name.Names;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.transaction.UserTransaction;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventManager;
import org.eclipse.persistence.internal.jpa.metadata.listeners.EntityListener;
import org.eclipse.persistence.sessions.Session;

public abstract class PersistenceTestModule
  extends AbstractPersistenceTestModule
{
  private final boolean _singleEntityManagerProject;
  private EntityManager _entityManager;

  protected PersistenceTestModule( final boolean singleEntityManagerProject )
  {
    _singleEntityManagerProject = singleEntityManagerProject;
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
    _entityManager = DatabaseUtil.createEntityManager( getPersistenceUnitName(), getDatabasePrefix() );
    bindResource( EntityManager.class, getPersistenceUnitName(), _entityManager );
  }

  /**
   * Return true if there is a single "primary" EntityManager. This is the EntityManager that
   * the application reads/writes two and thus the one that DbCleaner must clean. If the application
   * reads and writes to multiple EntityManagers then this
   */
  protected final boolean isSingleEntityManagerProject()
  {
    return _singleEntityManagerProject;
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
    if ( isSingleEntityManagerProject() )
    {
      bind( EntityManager.class ).toInstance( getEntityManager() );
      bind( DbCleaner.class ).toInstance( new DbCleaner( tables, getEntityManager() ) );
    }
    else
    {
      bind( DbCleaner.class ).
        annotatedWith( Names.named( getPersistenceUnitName() ) ).
        toInstance( new DbCleaner( tables, getEntityManager() ) );
    }
  }

  /**
   * Request injection for entity listeners.
   *
   * @param model the type to inject.
   */
  protected final void requestInjectionForEntityListener( final Class model )
  {
    final Session session = _entityManager.unwrap( Session.class );
    final ClassDescriptor descriptor = session.getClassDescriptor( model );
    if ( null == descriptor )
    {
      final String message = "Unable to locate entity of type " + model.getName() + " in the persistence context";
      throw new IllegalStateException( message );
    }
    final DescriptorEventManager eventManager = descriptor.getEventManager();
    for ( final Object o : eventManager.getDefaultEventListeners() )
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
