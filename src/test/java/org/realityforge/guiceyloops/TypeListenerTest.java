package org.realityforge.guiceyloops;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class TypeListenerTest
{
  @Test
  public void ensureThatTheCorrectFieldsAreInjected()
      throws Exception
  {
    final ComponentA instance = getInjectedComponent();
    assertFieldNotInjected( instance, "_componentB" );
    assertFieldNotInjected( instance, "_componentBViaPersistenceContext" );
    assertFieldInjected( instance, "_componentBViaEJB" );
    assertFieldInjected( instance, "_componentBViaResource" );
    assertFieldInjected( instance, "_entityManagerBViaPersistenceContext" );
  }

  private void assertFieldNotInjected( final ComponentA instance, final String fieldName )
      throws Exception
  {
    assertNull( getFieldValue( instance, fieldName ) );
  }

  private void assertFieldInjected( final ComponentA instance, final String fieldName )
      throws Exception
  {
    assertNotNull( getFieldValue( instance, fieldName ) );
  }

  private static Object getFieldValue( final ComponentA instance, final String fieldName )
      throws Exception
  {
    final Field field = instance.getClass().getDeclaredField( fieldName );
    field.setAccessible( true );
    return field.get( instance );
  }

  private static ComponentA getInjectedComponent()
  {
    final Injector injector = createInjector();
    return injector.getInstance( ComponentA.class );
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

    //Should not be injected
    @PersistenceContext
    private ComponentB _componentBViaPersistenceContext;

    @PersistenceContext
    private EntityManager _entityManagerBViaPersistenceContext;
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

    public <T> T find( final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode, final Map<String, Object> properties )
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
