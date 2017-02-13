package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
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
    assertFieldInjected( instance, ComponentA.class, "_componentBViaResourceLookup" );
    assertFieldInjected( instance, ComponentA.class, "_entityManagerBViaPersistenceContext" );
    assertFieldInjected( instance,
                         ComponentA.class,
                         "_entityManagerBViaPersistenceContextWithKey",
                         TestEntityManager2.class );
    assertFieldInjected( instance, ComponentA.class, "_webServiceType" );
    assertFieldInjected( instance, ComponentA.class, "_context", TestJMSContext.class );
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
    assertFieldInjected( instance, ComponentA.class, "_componentBViaResourceLookup" );
    assertFieldInjected( instance, ComponentA.class, "_entityManagerBViaPersistenceContext" );
    assertFieldInjected( instance,
                         ComponentA.class,
                         "_entityManagerBViaPersistenceContextWithKey",
                         TestEntityManager2.class );
    assertFieldInjected( instance, ComponentA.class, "_webServiceType" );
    assertFieldInjected( instance, ComponentA.class, "_context", TestJMSContext.class );
  }

  private void assertFieldNotInjected( final Object instance, final Class declaringType, final String fieldName )
    throws Exception
  {
    assertNull( getFieldValue( instance, declaringType, fieldName ) );
  }

  private void assertFieldInjected( final Object instance, final Class declaringType, final String fieldName )
    throws Exception
  {
    assertFieldInjected( instance, declaringType, fieldName, null );
  }

  private void assertFieldInjected( final Object instance,
                                    final Class declaringType,
                                    final String fieldName,
                                    @Nullable final Class<?> expectedType )
    throws Exception
  {
    final Object value = getFieldValue( instance, declaringType, fieldName );
    assertNotNull( value );
    if ( null != expectedType )
    {
      assertTrue( expectedType.isInstance( value ) );
    }
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
      bind( JMSContext.class ).to( TestJMSContext2.class );
      bind( JMSContext.class ).annotatedWith( Names.named( "jms/Blah" ) ).to( TestJMSContext.class );
      bind( EntityManager.class ).to( TestEntityManager.class );
      bind( EntityManager.class ).annotatedWith( Names.named( "X" ) ).toInstance( new TestEntityManager2() );
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

    @Resource(lookup = "some/resource/name")
    private ComponentB _componentBViaResourceLookup;

    @Inject
    @JMSConnectionFactory( "jms/Blah" )
    private JMSContext _context;

    //Should not be injected
    @PersistenceContext
    private ComponentB _componentBViaPersistenceContext;

    @PersistenceContext
    private EntityManager _entityManagerBViaPersistenceContext;

    @PersistenceContext(unitName = "X")
    private EntityManager _entityManagerBViaPersistenceContextWithKey;

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

  public static class TestEntityManager2
    extends TestEntityManager
  {
  }

  public static class TestEntityManager
    implements EntityManager
  {
    @Override
    public Query createQuery( final CriteriaUpdate updateQuery )
    {
      return null;
    }

    @Override
    public Query createQuery( final CriteriaDelete deleteQuery )
    {
      return null;
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery( final String name )
    {
      return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery( final String procedureName )
    {
      return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery( final String procedureName, final Class... resultClasses )
    {
      return null;
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery( final String procedureName,
                                                            final String... resultSetMappings )
    {
      return null;
    }

    @Override
    public boolean isJoinedToTransaction()
    {
      return false;
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph( final Class<T> rootType )
    {
      return null;
    }

    @Override
    public EntityGraph<?> createEntityGraph( final String graphName )
    {
      return null;
    }

    @Override
    public EntityGraph<?> getEntityGraph( final String graphName )
    {
      return null;
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs( final Class<T> entityClass )
    {
      return null;
    }

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

  static final class TestJMSContext
    implements JMSContext
  {
    @Override
    public JMSContext createContext( final int sessionMode )
    {
      return null;
    }

    @Override
    public JMSProducer createProducer()
    {
      return null;
    }

    @Override
    public String getClientID()
    {
      return null;
    }

    @Override
    public void setClientID( final String clientID )
    {
    }

    @Override
    public ConnectionMetaData getMetaData()
    {
      return null;
    }

    @Override
    public ExceptionListener getExceptionListener()
    {
      return null;
    }

    @Override
    public void setExceptionListener( final ExceptionListener listener )
    {
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }

    @Override
    public void setAutoStart( final boolean autoStart )
    {
    }

    @Override
    public boolean getAutoStart()
    {
      return false;
    }

    @Override
    public void close()
    {
    }

    @Override
    public BytesMessage createBytesMessage()
    {
      return null;
    }

    @Override
    public MapMessage createMapMessage()
    {
      return null;
    }

    @Override
    public Message createMessage()
    {
      return null;
    }

    @Override
    public ObjectMessage createObjectMessage()
    {
      return null;
    }

    @Override
    public ObjectMessage createObjectMessage( final Serializable object )
    {
      return null;
    }

    @Override
    public StreamMessage createStreamMessage()
    {
      return null;
    }

    @Override
    public TextMessage createTextMessage()
    {
      return null;
    }

    @Override
    public TextMessage createTextMessage( final String text )
    {
      return null;
    }

    @Override
    public boolean getTransacted()
    {
      return false;
    }

    @Override
    public int getSessionMode()
    {
      return 0;
    }

    @Override
    public void commit()
    {
    }

    @Override
    public void rollback()
    {
    }

    @Override
    public void recover()
    {
    }

    @Override
    public JMSConsumer createConsumer( final Destination destination )
    {
      return null;
    }

    @Override
    public JMSConsumer createConsumer( final Destination destination, final String messageSelector )
    {
      return null;
    }

    @Override
    public JMSConsumer createConsumer( final Destination destination,
                                       final String messageSelector,
                                       final boolean noLocal )
    {
      return null;
    }

    @Override
    public Queue createQueue( final String queueName )
    {
      return null;
    }

    @Override
    public Topic createTopic( final String topicName )
    {
      return null;
    }

    @Override
    public JMSConsumer createDurableConsumer( final Topic topic, final String name )
    {
      return null;
    }

    @Override
    public JMSConsumer createDurableConsumer( final Topic topic,
                                              final String name,
                                              final String messageSelector,
                                              final boolean noLocal )
    {
      return null;
    }

    @Override
    public JMSConsumer createSharedDurableConsumer( final Topic topic, final String name )
    {
      return null;
    }

    @Override
    public JMSConsumer createSharedDurableConsumer( final Topic topic, final String name, final String messageSelector )
    {
      return null;
    }

    @Override
    public JMSConsumer createSharedConsumer( final Topic topic, final String sharedSubscriptionName )
    {
      return null;
    }

    @Override
    public JMSConsumer createSharedConsumer( final Topic topic,
                                             final String sharedSubscriptionName,
                                             final String messageSelector )
    {
      return null;
    }

    @Override
    public QueueBrowser createBrowser( final Queue queue )
    {
      return null;
    }

    @Override
    public QueueBrowser createBrowser( final Queue queue, final String messageSelector )
    {
      return null;
    }

    @Override
    public TemporaryQueue createTemporaryQueue()
    {
      return null;
    }

    @Override
    public TemporaryTopic createTemporaryTopic()
    {
      return null;
    }

    @Override
    public void unsubscribe( final String name )
    {
    }

    @Override
    public void acknowledge()
    {
    }
  }

  static final class TestJMSContext2
    implements JMSContext
  {
    @Override
    public JMSContext createContext( final int sessionMode )
    {
      return null;
    }

    @Override
    public JMSProducer createProducer()
    {
      return null;
    }

    @Override
    public String getClientID()
    {
      return null;
    }

    @Override
    public void setClientID( final String clientID )
    {
    }

    @Override
    public ConnectionMetaData getMetaData()
    {
      return null;
    }

    @Override
    public ExceptionListener getExceptionListener()
    {
      return null;
    }

    @Override
    public void setExceptionListener( final ExceptionListener listener )
    {
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }

    @Override
    public void setAutoStart( final boolean autoStart )
    {
    }

    @Override
    public boolean getAutoStart()
    {
      return false;
    }

    @Override
    public void close()
    {
    }

    @Override
    public BytesMessage createBytesMessage()
    {
      return null;
    }

    @Override
    public MapMessage createMapMessage()
    {
      return null;
    }

    @Override
    public Message createMessage()
    {
      return null;
    }

    @Override
    public ObjectMessage createObjectMessage()
    {
      return null;
    }

    @Override
    public ObjectMessage createObjectMessage( final Serializable object )
    {
      return null;
    }

    @Override
    public StreamMessage createStreamMessage()
    {
      return null;
    }

    @Override
    public TextMessage createTextMessage()
    {
      return null;
    }

    @Override
    public TextMessage createTextMessage( final String text )
    {
      return null;
    }

    @Override
    public boolean getTransacted()
    {
      return false;
    }

    @Override
    public int getSessionMode()
    {
      return 0;
    }

    @Override
    public void commit()
    {
    }

    @Override
    public void rollback()
    {
    }

    @Override
    public void recover()
    {
    }

    @Override
    public JMSConsumer createConsumer( final Destination destination )
    {
      return null;
    }

    @Override
    public JMSConsumer createConsumer( final Destination destination, final String messageSelector )
    {
      return null;
    }

    @Override
    public JMSConsumer createConsumer( final Destination destination,
                                       final String messageSelector,
                                       final boolean noLocal )
    {
      return null;
    }

    @Override
    public Queue createQueue( final String queueName )
    {
      return null;
    }

    @Override
    public Topic createTopic( final String topicName )
    {
      return null;
    }

    @Override
    public JMSConsumer createDurableConsumer( final Topic topic, final String name )
    {
      return null;
    }

    @Override
    public JMSConsumer createDurableConsumer( final Topic topic,
                                              final String name,
                                              final String messageSelector,
                                              final boolean noLocal )
    {
      return null;
    }

    @Override
    public JMSConsumer createSharedDurableConsumer( final Topic topic, final String name )
    {
      return null;
    }

    @Override
    public JMSConsumer createSharedDurableConsumer( final Topic topic, final String name, final String messageSelector )
    {
      return null;
    }

    @Override
    public JMSConsumer createSharedConsumer( final Topic topic, final String sharedSubscriptionName )
    {
      return null;
    }

    @Override
    public JMSConsumer createSharedConsumer( final Topic topic,
                                             final String sharedSubscriptionName,
                                             final String messageSelector )
    {
      return null;
    }

    @Override
    public QueueBrowser createBrowser( final Queue queue )
    {
      return null;
    }

    @Override
    public QueueBrowser createBrowser( final Queue queue, final String messageSelector )
    {
      return null;
    }

    @Override
    public TemporaryQueue createTemporaryQueue()
    {
      return null;
    }

    @Override
    public TemporaryTopic createTemporaryTopic()
    {
      return null;
    }

    @Override
    public void unsubscribe( final String name )
    {
    }

    @Override
    public void acknowledge()
    {
    }
  }
}
