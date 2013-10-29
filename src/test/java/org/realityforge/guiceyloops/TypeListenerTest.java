package org.realityforge.guiceyloops;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import java.lang.reflect.Field;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import javax.xml.ws.WebServiceRef;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class TypeListenerTest
{
  @Test
  public void ensureThatTheCorrectFieldsAreInjected()
    throws Exception
  {
    final ComponentA instance = getInjectedComponent( ComponentA.class );
    assertFieldNotInjected( instance, ComponentA.class, "_componentB" );
    assertFieldNotInjected( instance, ComponentA.class, "_componentBViaPersistenceContext" );
    assertFieldInjected( instance, ComponentA.class, "_componentBViaEJB" );
    assertFieldInjected( instance, ComponentA.class, "_componentBViaResource" );
    assertFieldInjected( instance, ComponentA.class, "_componentBViaNamedResource" );
    assertFieldInjected( instance, ComponentA.class, "_entityManagerBViaPersistenceContext" );
    assertFieldInjected( instance, ComponentA.class, "_webServiceType" );
  }

  @Test
  public void ensureThatTheCorrectFieldsAreInjectedInSubclass()
    throws Exception
  {
    final SubclassOfComponentA instance = getInjectedComponent( SubclassOfComponentA.class );
    assertFieldNotInjected( instance, ComponentA.class, "_componentB" );
    assertFieldNotInjected( instance, ComponentA.class, "_componentBViaPersistenceContext" );
    assertFieldInjected( instance, ComponentA.class, "_componentBViaEJB" );
    assertFieldInjected( instance, ComponentA.class, "_componentBViaResource" );
    assertFieldInjected( instance, ComponentA.class, "_componentBViaNamedResource" );
    assertFieldInjected( instance, ComponentA.class, "_entityManagerBViaPersistenceContext" );
    assertFieldInjected( instance, ComponentA.class, "_webServiceType" );
  }

  private void assertFieldNotInjected( final Object instance, final Class declaringType, final String fieldName )
    throws Exception
  {
    assertNull( getFieldValue( instance, declaringType, fieldName ) );
  }

  private void assertFieldInjected( final Object instance, final Class declaringType, final String fieldName )
    throws Exception
  {
    assertNotNull( getFieldValue( instance, declaringType, fieldName ) );
  }

  private static Object getFieldValue( final Object instance, final Class declaringType, final String fieldName )
    throws Exception
  {
    final Field field = declaringType.getDeclaredField( fieldName );
    field.setAccessible( true );
    return field.get( instance );
  }

  private static <T> T getInjectedComponent( final Class<T> type )
  {
    return createInjector().getInstance( type );
  }

  private static Injector createInjector()
  {
    return Guice.createInjector( new MyTestModule(), new JEETestingModule() );
  }

  public static class MyTestModule
    extends AbstractModule
  {
    protected void configure()
    {
      bind( EntityManager.class ).to( TestEntityManager.class );
      bind( ComponentB.class ).annotatedWith( Names.named( "some/resource/name" ) ).toInstance( new ComponentB() );
    }
  }

  public static class ComponentA
  {
    //Should not be injected
    private ComponentB _componentB;

    @EJB
    private ComponentB _componentBViaEJB;

    @Resource
    private ComponentB _componentBViaResource;

    @Resource(name = "some/resource/name")
    private ComponentB _componentBViaNamedResource;

    //Should not be injected
    @PersistenceContext
    private ComponentB _componentBViaPersistenceContext;

    @PersistenceContext
    private EntityManager _entityManagerBViaPersistenceContext;

    @WebServiceRef
    private ComponentB _webServiceType;
  }

  public static class SubclassOfComponentA
    extends ComponentA
  {
  }

  public static class ComponentB
  {
  }

  public static class TestEntityManager implements EntityManager
  {
    public void persist( final Object entity )
    {
    }

    public <T> T merge( final T entity )
    {
      return null;
    }

    public void remove( final Object entity )
    {

    }

    public <T> T find( final Class<T> entityClass, final Object primaryKey )
    {
      return null;
    }

    public <T> T find( final Class<T> entityClass, final Object primaryKey, final Map<String, Object> properties )
    {
      return null;
    }

    public <T> T find( final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode )
    {
      return null;
    }

    public <T> T find( final Class<T> entityClass,
                       final Object primaryKey,
                       final LockModeType lockMode,
                       final Map<String, Object> properties )
    {
      return null;
    }

    public <T> T getReference( final Class<T> entityClass, final Object primaryKey )
    {
      return null;
    }

    public void flush()
    {

    }

    public void setFlushMode( final FlushModeType flushMode )
    {

    }

    public FlushModeType getFlushMode()
    {
      return null;
    }

    public void lock( final Object entity, final LockModeType lockMode )
    {

    }

    public void lock( final Object entity, final LockModeType lockMode, final Map<String, Object> properties )
    {

    }

    public void refresh( final Object entity )
    {

    }

    public void refresh( final Object entity, final Map<String, Object> properties )
    {

    }

    public void refresh( final Object entity, final LockModeType lockMode )
    {

    }

    public void refresh( final Object entity, final LockModeType lockMode, final Map<String, Object> properties )
    {

    }

    public void clear()
    {

    }

    public void detach( final Object entity )
    {

    }

    public boolean contains( final Object entity )
    {
      return false;
    }

    public LockModeType getLockMode( final Object entity )
    {
      return null;
    }

    public void setProperty( final String propertyName, final Object value )
    {

    }

    public Map<String, Object> getProperties()
    {
      return null;
    }

    public Query createQuery( final String qlString )
    {
      return null;
    }

    public <T> TypedQuery<T> createQuery( final CriteriaQuery<T> criteriaQuery )
    {
      return null;
    }

    public <T> TypedQuery<T> createQuery( final String qlString, final Class<T> resultClass )
    {
      return null;
    }

    public Query createNamedQuery( final String name )
    {
      return null;
    }

    public <T> TypedQuery<T> createNamedQuery( final String name, final Class<T> resultClass )
    {
      return null;
    }

    public Query createNativeQuery( final String sqlString )
    {
      return null;
    }

    public Query createNativeQuery( final String sqlString, final Class resultClass )
    {
      return null;
    }

    public Query createNativeQuery( final String sqlString, final String resultSetMapping )
    {
      return null;
    }

    public void joinTransaction()
    {

    }

    public <T> T unwrap( final Class<T> cls )
    {
      return null;
    }

    public Object getDelegate()
    {
      return null;
    }

    public void close()
    {

    }

    public boolean isOpen()
    {
      return false;
    }

    public EntityTransaction getTransaction()
    {
      return null;
    }

    public EntityManagerFactory getEntityManagerFactory()
    {
      return null;
    }

    public CriteriaBuilder getCriteriaBuilder()
    {
      return null;
    }

    public Metamodel getMetamodel()
    {
      return null;
    }
  }
}
