package org.realityforge.guiceyloops;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;

/**
 * The abstract TypeListener used to ensure fields annotated with a particular annotation are injected.
 * <p/>
 * <p>Subclasses should pass in the specific annotation and allowable field types that should be
 * injected into the constructor. The actual injector will be created in the {@link #createInjector} method.</p>
 */
public abstract class AnnotationTypeListener
    implements TypeListener
{
  private final Class<? extends Annotation> _annotation;
  private final Class<?>[] _acceptedTypes;

  /**
   * Create a type listener to inject a field regardless of it's type.
   *
   * @param annotation the annotation type that must be present on the field.
   */
  protected AnnotationTypeListener( final Class<? extends Annotation> annotation )
  {
    this( annotation, new Class[ 0 ] );
  }

  /**
   * Create a type listener to inject a field based on both annotation and the fields type.
   *
   * @param annotation    the annotation type that must be present on the field.
   * @param acceptedTypes the type of the field must be one of the accepted types to be injected. If
   *                      the array passed to the listener has zero elements then all field types are injected.
   */
  protected AnnotationTypeListener( final Class<? extends Annotation> annotation,
                                    final Class<?>[] acceptedTypes )
  {
    _annotation = annotation;
    _acceptedTypes = acceptedTypes;
  }

  /**
   * {@inheritDoc}
   */
  public <T> void hear( final TypeLiteral<T> typeLiteral,
                        final TypeEncounter<T> typeEncounter )
  {
    for ( Class<?> rawType = typeLiteral.getRawType(); rawType != Object.class; rawType = rawType.getSuperclass() )
    {
      for ( final Field field : rawType.getDeclaredFields() )
      {
        if ( field.isAnnotationPresent( _annotation ) )
        {
          boolean accepted = _acceptedTypes.length == 0;
          for ( final Class<?> type : _acceptedTypes )
          {
            if ( field.getType() == type )
            {
              accepted = true;
              break;
            }
          }
          if ( accepted )
          {
            final Annotation annotation = field.getAnnotation( _annotation );
            final MembersInjector<T> injector = createInjector( typeEncounter, annotation, field );
            typeEncounter.register( injector );
          }
        }
      }
    }
  }

  /**
   * Method invoked when a field has been selected for injection to create the relevant injector.
   *
   * @param typeEncounter the context of the encounter.
   * @param annotation    the annotation on the field that selected the field for injection.
   * @param field         the field.
   * @param <T>           the type that declares the field.
   * @return the injector created for the field.
   */
  @Nonnull
  protected abstract <T> MembersInjector<T> createInjector( @Nonnull TypeEncounter<T> typeEncounter,
                                                            @Nonnull Annotation annotation,
                                                            @Nonnull Field field );
}
