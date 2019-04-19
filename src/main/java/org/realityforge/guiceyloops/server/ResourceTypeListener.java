package org.realityforge.guiceyloops.server;

import com.google.inject.MembersInjector;
import com.google.inject.spi.TypeEncounter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.annotation.Resource;

/**
 * The annotation listener responsible for injecting fields annotated with the @Resource annotation.
 */
public class ResourceTypeListener
  extends AnnotationTypeListener
{
  public ResourceTypeListener()
  {
    super( Resource.class );
  }

  @Nonnull
  protected <T> MembersInjector<T> createInjector( @Nonnull final TypeEncounter<T> typeEncounter,
                                                   @Nonnull final Annotation annotation,
                                                   @Nonnull final Field field )
  {
    final Resource resource = (Resource) annotation;
    final String name = resource.name();
    final String key = resource.lookup();
    if ( !"".equals( key ) )
    {
      return FieldBasedInjector.createFromEncounter( typeEncounter, key, field );
    }
    else if ( !"".equals( name ) )
    {
      return FieldBasedInjector.createFromEncounter( typeEncounter, name, field );
    }
    else
    {
      return FieldBasedInjector.createFromEncounter( typeEncounter, field );
    }
  }
}
