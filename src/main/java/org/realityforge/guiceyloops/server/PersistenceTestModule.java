package org.realityforge.guiceyloops.server;

import com.google.inject.Singleton;
import com.google.inject.name.Names;
import java.lang.reflect.Field;
import java.util.List;
import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.eclipse.persistence.sessions.Session;

public abstract class PersistenceTestModule
  extends AbstractPersistenceTestModule
{
  private EntityManager _entityManager;

  /**
   * Override this to
   */
  protected void configure()
  {
    _entityManager = DatabaseUtil.createEntityManager( getPersistenceUnitName() );

    /*
     * This assumes there is a single "primary" EntityManager. This is the one that the
     * application reads/writes two and thus the one that DbCleaner must clean. When/if
     * this assumption ever changes this may result in the next line being removed.
     */
    bind( EntityManager.class ).toInstance( _entityManager );

    bindResource( EntityManager.class, getPersistenceUnitName(), _entityManager );

    bind( DbCleaner.class ).in( Singleton.class );

    super.configure();
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
