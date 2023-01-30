package org.realityforge.guiceyloops.server;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;

public final class EventStub<T>
  implements Event<T>
{
  private final List<T> _events = new ArrayList<>();
  private final Map<T, List<Annotation>> _eventQualifiersMap = new HashMap<>();

  @Override
  public void fire( final T event )
  {
    _events.add( event );
  }

  /**
   * Removes all fired events.
   */
  public void clear()
  {
    _events.clear();
  }

  /**
   * Returns the next event. FIFO order.
   */
  public T pop()
  {
    return _events.remove( 0 );
  }

  /**
   * Returns the number of events fired since construction or the last call to clear().
   */
  public int count()
  {
    return _events.size();
  }

  /**
   * Returns qualifiers used with {@link #select(Annotation...)} in conjunction with event firing.
   *
   * @param event The event that was fired upon.
   * @return list of qualifiers invoked for the event or null if none.
   */
  public List<Annotation> getQualifiers( final T event )
  {
    return Collections.unmodifiableList( _eventQualifiersMap.get( event ) );
  }

  public List<T> getEvents()
  {
    return Collections.unmodifiableList( _events );
  }

  @Override
  public Event<T> select( final Annotation... qualifiers )
  {
    return new QualifiedEvent( qualifiers );
  }

  @Override
  public <U extends T> Event<U> select( final Class<U> subtype, final Annotation... qualifiers )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <U extends T> CompletionStage<U> fireAsync( final U event )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <U extends T> CompletionStage<U> fireAsync( final U event, final NotificationOptions options )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <U extends T> Event<U> select( final TypeLiteral<U> subtype, final Annotation... qualifiers )
  {
    throw new UnsupportedOperationException();
  }

  private class QualifiedEvent
    implements Event<T>
  {
    private final Annotation[] _selectedQualifiers;

    QualifiedEvent( final Annotation... qualifiers )
    {
      _selectedQualifiers = qualifiers;
    }

    @Override
    public void fire( final T event )
    {
      EventStub.this.fire( event );
      _eventQualifiersMap.put( event, Collections.unmodifiableList( Arrays.asList( _selectedQualifiers ) ) );
    }

    @Override
    public Event<T> select( final Annotation... qualifiers )
    {
      return EventStub.this.select( qualifiers );
    }

    @Override
    public <U extends T> Event<U> select( final Class<U> subtype, final Annotation... qualifiers )
    {
      return EventStub.this.select( subtype, qualifiers );
    }

    @Override
    public <U extends T> Event<U> select( final TypeLiteral<U> subtype, final Annotation... qualifiers )
    {
      return EventStub.this.select( subtype, qualifiers );
    }

    @Override
    public <U extends T> CompletionStage<U> fireAsync( final U event )
    {
      return EventStub.this.fireAsync( event );
    }

    @Override
    public <U extends T> CompletionStage<U> fireAsync( final U event, final NotificationOptions options )
    {
      return EventStub.this.fireAsync( event, options );
    }
  }
}
