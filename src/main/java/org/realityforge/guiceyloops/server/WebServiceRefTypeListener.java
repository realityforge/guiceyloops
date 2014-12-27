package org.realityforge.guiceyloops.server;

import com.google.inject.MembersInjector;
import com.google.inject.spi.TypeEncounter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.xml.ws.WebServiceRef;

/**
 * The annotation listener responsible for injecting fields annotated with the @WebServiceRef annotation.
 */
public final class WebServiceRefTypeListener
    extends AnnotationTypeListener
{
  public WebServiceRefTypeListener()
  {
    super( WebServiceRef.class );
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
