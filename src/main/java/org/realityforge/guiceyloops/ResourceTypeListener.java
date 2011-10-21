package org.realityforge.guiceyloops;

import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.spi.TypeEncounter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Resource;

public class ResourceTypeListener
    extends AnnotationTypeListener
{
  public ResourceTypeListener()
  {
    super( Resource.class );
  }

  protected <T> MembersInjector<T> createInjector( final TypeEncounter<T> typeEncounter,
                                                   final Annotation annotation,
                                                   final Field field )
  {
    final Resource context = (Resource) annotation;
    return new ResourceFieldInjector<T>( typeEncounter, context, field );
  }

  private static final class ResourceFieldInjector<T>
      extends FieldBasedInjector<T>
  {
    private final Provider<?> _provider;

    private ResourceFieldInjector( final TypeEncounter<T> typeEncounter,
                                   final Annotation annotation,
                                   final Field field )
    {
      super( typeEncounter, annotation, field );
      _provider = getTypeEncounter().getProvider( field.getType() );
    }

    protected Object getValue()
    {
      return _provider.get();
    }
  }
}
