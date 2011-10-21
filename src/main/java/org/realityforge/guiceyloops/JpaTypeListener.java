package org.realityforge.guiceyloops;

import com.google.inject.MembersInjector;
import com.google.inject.spi.TypeEncounter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * The annotation listener responsible for injecting fields of type EntityManager annotated with
 * the @PersistenceContext annotation.
 */
public class JpaTypeListener
    extends AnnotationTypeListener
{
  public JpaTypeListener()
  {
    super( PersistenceContext.class, new Class[]{ EntityManager.class } );
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  protected <T> MembersInjector<T> createInjector( @Nonnull final TypeEncounter<T> typeEncounter,
                                                   @Nonnull final Annotation annotation,
                                                   @Nonnull final Field field )
  {
    return FieldBasedInjector.createFromEncounter( typeEncounter, field );
  }
}
