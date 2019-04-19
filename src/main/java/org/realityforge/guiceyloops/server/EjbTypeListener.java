package org.realityforge.guiceyloops.server;

import com.google.inject.MembersInjector;
import com.google.inject.spi.TypeEncounter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.ejb.EJB;

/**
 * The annotation listener responsible for injecting fields annotated with the @EJB annotation.
 */
public final class EjbTypeListener
  extends AnnotationTypeListener
{
  public EjbTypeListener()
  {
    super( EJB.class );
  }

  @Nonnull
  protected <T> MembersInjector<T> createInjector( @Nonnull final TypeEncounter<T> typeEncounter,
                                                   @Nonnull final Annotation annotation,
                                                   @Nonnull final Field field )
  {
    return FieldBasedInjector.createFromEncounter( typeEncounter, field );
  }
}
