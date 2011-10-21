package org.realityforge.guiceyloops;

import com.google.inject.MembersInjector;
import com.google.inject.spi.TypeEncounter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A field based injector that bases the injection of the field on the type of the field.
 *
 * <p>Designed to be sub-classed. Subclasses should override {@link #getValue()} to determine the value injected.</p>
 *
 * @param <T> the type of the object that declares the field
 */
public class FieldBasedInjector<T>
    implements MembersInjector<T>
{
  private final TypeEncounter<T> _typeEncounter;
  private final Field _field;

  public FieldBasedInjector( @Nonnull final TypeEncounter<T> typeEncounter,
                             @Nonnull final Field field )
  {
    _typeEncounter = typeEncounter;
    _field = field;
    _field.setAccessible( true );
  }

  /**
   * {@inheritDoc}
   */
  public void injectMembers( final T t )
  {
    try
    {
      _field.set( t, getValue() );
    }
    catch( IllegalAccessException e )
    {
      throw new RuntimeException( e );
    }
  }

  /**
   * @return the context in which the indictable field was encountered.
   */
  protected final TypeEncounter<T> getTypeEncounter()
  {
    return _typeEncounter;
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
    return getTypeEncounter().getProvider( getField().getType() ).get();
  }
}
