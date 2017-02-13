package org.realityforge.guiceyloops.server;

import com.google.inject.MembersInjector;
import com.google.inject.spi.TypeEncounter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;

/**
 * The annotation listener responsible for injecting fields of type JMSContext annotated with
 * the @JMSConnectionFactory annotation.
 */
public class JmsTypeListener
  extends AnnotationTypeListener
{
  public JmsTypeListener()
  {
    super( JMSConnectionFactory.class, new Class[]{ JMSContext.class } );
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  protected <T> MembersInjector<T> createInjector( @Nonnull final TypeEncounter<T> typeEncounter,
                                                   @Nonnull final Annotation annotation,
                                                   @Nonnull final Field field )
  {
    final String name = ( (JMSConnectionFactory) annotation ).value();
    if ( "".equals( name ) )
    {
      return FieldBasedInjector.createFromEncounter( typeEncounter, field );
    }
    else
    {
      return FieldBasedInjector.createFromEncounter( typeEncounter, name, field );
    }
  }
}
