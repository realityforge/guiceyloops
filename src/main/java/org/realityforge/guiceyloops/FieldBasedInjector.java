package org.realityforge.guiceyloops;

import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A field based injector that bases the injection of the field on the type of the field.
 * <p/>
 * <p>Designed to be sub-classed. Subclasses should override {@link #getValue()} to determine the value injected.</p>
 *
 * @param <T> the type of the object that declares the field
 */
public class FieldBasedInjector<T>
    implements MembersInjector<T>
{
  private final Field _field;
  private final Provider<?> _provider;

  /**
   * Create an injector based on a particular encounter with a field.
   *
   * @param typeEncounter the context of the encounter.
   * @param name          the name of the resource to inject.
   * @param field         the field.
   * @param <T>           the type of the object that declares the field
   * @return the injector for field.
   */
  public static <T> FieldBasedInjector<T> createFromEncounter( @Nonnull final TypeEncounter<T> typeEncounter,
                                                               @Nonnull final String name,
                                                               @Nonnull final Field field )
  {

    final Provider<?> provider = typeEncounter.getProvider( Key.get( field.getType(), Names.named( name ) ) );
    return new FieldBasedInjector<T>( provider, field );
  }

  /**
   * Create an injector based on a particular encounter with a field.
   *
   * @param typeEncounter the context of the encounter.
   * @param field         the field.
   * @param <T>           the type of the object that declares the field
   * @return the injector for field.
   */
  public static <T> FieldBasedInjector<T> createFromEncounter( @Nonnull final TypeEncounter<T> typeEncounter,
                                                               @Nonnull final Field field )
  {
    final Provider<?> provider = typeEncounter.getProvider( field.getType() );
    return new FieldBasedInjector<T>( provider, field );
  }

  /**
   * Inject field by using specified provider.
   *
   * @param provider the provider to use to create value to inject.
   * @param field    the field to inject.
   */
  public FieldBasedInjector( @Nonnull final Provider<?> provider, @Nonnull final Field field )
  {
    _field = field;
    _field.setAccessible( true );
    _provider = provider;
  }

  /**
   * {@inheritDoc}
   */
  public void injectMembers( final T t )
  {
    try
    {
      getField().set( t, getValue() );
    }
    catch( final IllegalAccessException iae )
    {
      throw new IllegalStateException( iae.getMessage(), iae );
    }
  }

  /**
   * @return the field on which the annotation was encountered.
   */
  protected final Field getField()
  {
    return _field;
  }

  /**
   * @return the value to bind to the field.
   */
  protected Object getValue()
  {
    return _provider.get();
  }
}
