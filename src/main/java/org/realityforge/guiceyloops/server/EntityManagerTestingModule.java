package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import java.lang.reflect.Field;
import java.util.List;
import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.transaction.TransactionSynchronizationRegistry;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.eclipse.persistence.sessions.Session;

public abstract class EntityManagerTestingModule
  extends AbstractModule
{
  private EntityManager _entityManager;

  /**
   * Override this to
   */
  protected void configure()
  {
    _entityManager = DatabaseUtil.createEntityManager( getPersistenceUnitName() );
    bind( EntityManager.class ).toInstance( _entityManager );

    bind( DbCleaner.class ).in( Singleton.class );

    try
    {
      bind( TransactionSynchronizationRegistry.class ).
        to( TestTransactionSynchronizationRegistry.class ).
        in( Singleton.class );
    }
    catch ( final Throwable e )
    {
      //Ignored. Probably as the classes the transaction extensions are not on the classpath
    }
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
    bind( String[].class ).annotatedWith( Names.named( DbCleaner.TABLE_NAME_KEY ) ).toInstance( tables );
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
    for ( final Object o : descriptor.getEventManager().getDefaultEventListeners() )
    {
      requestInjection( toEntityListener( (DescriptorEventListener) o ) );
    }
  }

  private Object toEntityListener( final DescriptorEventListener listener )
  {
    try
    {
      final Field field = listener.getClass().getDeclaredField( "m_listener" );
      field.setAccessible( true );
      return field.get( listener );
    }
    catch ( final Throwable t )
    {
      throw new IllegalStateException( "Error retrieving listener", t );
    }
  }

  /**
   * @return the name of the persistence unit under test.
   */
  protected abstract String getPersistenceUnitName();

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