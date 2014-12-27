package org.realityforge.guiceyloops.server;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
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
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProducerFactory;
import javax.inject.Inject;
import static org.mockito.Mockito.*;

public class TestBeanManager
  implements BeanManager
{
  private final Injector _injector;

  @Inject
  public TestBeanManager( final Injector injector )
  {
    _injector = injector;
  }

  @Override
  public Object getReference( final Bean<?> bean, final Type beanType, final CreationalContext<?> ctx )
  {
    return _injector.getInstance( (Class) beanType );
  }

  @Override
  public Object getInjectableReference( final InjectionPoint ij, final CreationalContext<?> ctx )
  {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T> CreationalContext<T> createCreationalContext( final Contextual<T> contextual )
  {
    return (CreationalContext<T>) mock( CreationalContext.class );
  }

  @Override
  public Set<Bean<?>> getBeans( final Type beanType, final Annotation... qualifiers )
  {
    try
    {
      _injector.getProvider( (Class) beanType );
      final HashSet<Bean<?>> beans = new HashSet<Bean<?>>();
      beans.add( mock( Bean.class ) );
      return beans;
    }
    catch ( final ConfigurationException ce )
    {
      return Collections.emptySet();
    }
  }

  @Override
  public Set<Bean<?>> getBeans( final String name )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Bean<?> getPassivationCapableBean( final String id )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <X> Bean<? extends X> resolve( final Set<Bean<? extends X>> beans )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void validate( final InjectionPoint injectionPoint )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void fireEvent( final Object event, final Annotation... qualifiers )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Set<ObserverMethod<? super T>> resolveObserverMethods( final T event, final Annotation... qualifiers )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Decorator<?>> resolveDecorators( final Set<Type> types, final Annotation... qualifiers )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Interceptor<?>> resolveInterceptors( final InterceptionType type,
                                                   final Annotation... interceptorBindings )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isScope( final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isNormalScope( final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isPassivatingScope( final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isQualifier( final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isInterceptorBinding( final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isStereotype( final Class<? extends Annotation> annotationType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Annotation> getInterceptorBindingDefinition( final Class<? extends Annotation> bindingType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Annotation> getStereotypeDefinition( final Class<? extends Annotation> stereotype )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean areQualifiersEquivalent( final Annotation qualifier1, final Annotation qualifier2 )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean areInterceptorBindingsEquivalent( final Annotation interceptorBinding1,
                                                   final Annotation interceptorBinding2 )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getQualifierHashCode( final Annotation qualifier )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getInterceptorBindingHashCode( final Annotation interceptorBinding )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Context getContext( final Class<? extends Annotation> scopeType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ELResolver getELResolver()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExpressionFactory wrapExpressionFactory( final ExpressionFactory expressionFactory )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> AnnotatedType<T> createAnnotatedType( final Class<T> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> InjectionTarget<T> createInjectionTarget( final AnnotatedType<T> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> InjectionTargetFactory<T> getInjectionTargetFactory( final AnnotatedType<T> annotatedType )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <X> ProducerFactory<X> getProducerFactory( final AnnotatedField<? super X> field, final Bean<X> declaringBean )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <X> ProducerFactory<X> getProducerFactory( final AnnotatedMethod<? super X> method,
                                                    final Bean<X> declaringBean )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> BeanAttributes<T> createBeanAttributes( final AnnotatedType<T> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public BeanAttributes<?> createBeanAttributes( final AnnotatedMember<?> type )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Bean<T> createBean( final BeanAttributes<T> attributes,
                                 final Class<T> beanClass,
                                 final InjectionTargetFactory<T> injectionTargetFactory )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T, X> Bean<T> createBean( final BeanAttributes<T> attributes,
                                    final Class<X> beanClass,
                                    final ProducerFactory<X> producerFactory )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public InjectionPoint createInjectionPoint( final AnnotatedField<?> field )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public InjectionPoint createInjectionPoint( final AnnotatedParameter<?> parameter )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T extends Extension> T getExtension( final Class<T> extensionClass )
  {
    throw new UnsupportedOperationException();
  }
}
