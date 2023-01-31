package org.realityforge.guiceyloops.server;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.InterceptionFactory;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProducerFactory;
import javax.inject.Inject;
import static org.mockito.Mockito.*;

public class TestBeanManager
  implements BeanManager
{
  @Nonnull
  private final Injector _injector;

  @SuppressWarnings( "CdiInjectionPointsInspection" )
  @Inject
  public TestBeanManager( @Nonnull final Injector injector )
  {
    _injector = Objects.requireNonNull( injector );
  }

  @Override
  public Object getReference( @Nonnull final Bean<?> bean,
                              @Nonnull final Type beanType,
                              @Nonnull final CreationalContext<?> ctx )
  {
    return _injector.getInstance( (Class<?>) beanType );
  }

  @Override
  public Object getInjectableReference( @Nonnull final InjectionPoint ij, @Nonnull final CreationalContext<?> ctx )
  {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T> CreationalContext<T> createCreationalContext( @Nonnull final Contextual<T> contextual )
  {
    return (CreationalContext<T>) mock( CreationalContext.class );
  }

  @Override
  public Set<Bean<?>> getBeans( @Nonnull final Type beanType, @Nonnull final Annotation... qualifiers )
  {
    try
    {
      _injector.getProvider( (Class<?>) beanType );
      final HashSet<Bean<?>> beans = new HashSet<>();
      beans.add( mock( Bean.class ) );
      return beans;
    }
    catch ( final ConfigurationException ce )
    {
      return Collections.emptySet();
    }
  }

  @Override
  @Nonnull
  public Set<Bean<?>> getBeans( @Nonnull final String name )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public Bean<?> getPassivationCapableBean( @Nonnull final String id )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public <X> Bean<? extends X> resolve( @Nonnull final Set<Bean<? extends X>> beans )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void validate( @Nonnull final InjectionPoint injectionPoint )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void fireEvent( @Nonnull final Object event, @Nonnull final Annotation... qualifiers )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public <T> Set<ObserverMethod<? super T>> resolveObserverMethods( @Nonnull final T event,
                                                                    @Nonnull final Annotation... qualifiers )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public List<Decorator<?>> resolveDecorators( @Nonnull final Set<Type> types, @Nonnull final Annotation... qualifiers )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public List<Interceptor<?>> resolveInterceptors( @Nonnull final InterceptionType type,
                                                   @Nonnull final Annotation... interceptorBindings )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isScope( @Nonnull final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isNormalScope( @Nonnull final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isPassivatingScope( @Nonnull final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isQualifier( @Nonnull final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInterceptorBinding( @Nonnull final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isStereotype( @Nonnull final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public Set<Annotation> getInterceptorBindingDefinition( @Nonnull final Class<? extends Annotation> bindingType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public Set<Annotation> getStereotypeDefinition( @Nonnull final Class<? extends Annotation> stereotype )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean areQualifiersEquivalent( @Nonnull final Annotation qualifier1, @Nonnull final Annotation qualifier2 )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean areInterceptorBindingsEquivalent( @Nonnull final Annotation interceptorBinding1,
                                                   @Nonnull final Annotation interceptorBinding2 )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getQualifierHashCode( @Nonnull final Annotation qualifier )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getInterceptorBindingHashCode( @Nonnull final Annotation interceptorBinding )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public Context getContext( @Nonnull final Class<? extends Annotation> scopeType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public ELResolver getELResolver()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExpressionFactory wrapExpressionFactory( @Nonnull final ExpressionFactory expressionFactory )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public <T> AnnotatedType<T> createAnnotatedType( @Nonnull final Class<T> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public <T> InjectionTarget<T> createInjectionTarget( @Nonnull final AnnotatedType<T> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public <T> InjectionTargetFactory<T> getInjectionTargetFactory( @Nonnull final AnnotatedType<T> annotatedType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull
  public <X> ProducerFactory<X> getProducerFactory( @Nonnull final AnnotatedField<? super X> field,
                                                    @Nonnull final Bean<X> declaringBean )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public <X> ProducerFactory<X> getProducerFactory(@Nonnull  final AnnotatedMethod<? super X> method,
                                                            @Nonnull     final Bean<X> declaringBean )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public <T> BeanAttributes<T> createBeanAttributes(@Nonnull  final AnnotatedType<T> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public BeanAttributes<?> createBeanAttributes(@Nonnull  final AnnotatedMember<?> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public <T> Bean<T> createBean( @Nonnull final BeanAttributes<T> attributes,
                                          @Nonnull    final Class<T> beanClass,
                                          @Nonnull     final InjectionTargetFactory<T> injectionTargetFactory )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public <T, X> Bean<T> createBean( @Nonnull final BeanAttributes<T> attributes,
                                    @Nonnull final Class<X> beanClass,
                                    @Nonnull final ProducerFactory<X> producerFactory )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public <T> InterceptionFactory<T> createInterceptionFactory(@Nonnull  final CreationalContext<T> ctx,@Nonnull  final Class<T> clazz )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public Event<Object> getEvent()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public Instance<Object> createInstance()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public InjectionPoint createInjectionPoint(@Nonnull  final AnnotatedField<?> field )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nonnull public InjectionPoint createInjectionPoint( @Nonnull final AnnotatedParameter<?> parameter )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @Nullable
  public <T extends Extension> T getExtension(@Nonnull  final Class<T> extensionClass )
  {
    return null;
  }
}
