package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.icegreen.greenmail.util.GreenMail;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.TransactionSynchronizationRegistry;
import org.realityforge.guiceyloops.server.glassfish.OpenMQContainer;
import org.realityforge.guiceyloops.shared.AbstractSharedTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@SuppressWarnings( "WeakerAccess" )
public abstract class AbstractServerTest
  extends AbstractSharedTest
  implements Flushable
{
  private Thread _testThread;
  private boolean _inFlush;

  @BeforeMethod
  public void preTest()
    throws Exception
  {
    resetJndiContext();
    super.preTest();
    startDbCleaner();
    setupBeanManager();
    setupTransactionSynchronizationRegistry();
    startupMailServer();
    _testThread = Thread.currentThread();
  }

  @AfterMethod
  public void postTest()
  {
    shutdownMailServer();
    shutdownTransactionSynchronizationRegistry();
    finishDbCleaner();
    shutdownBeanManager();
    shutdownEntityManager();
    super.postTest();
    clearJndiContext();
    _testThread = null;
  }

  /**
   * Completely shutdown the entity manager.
   */
  private void shutdownEntityManager()
  {
    try
    {
      em().close();
    }
    catch ( final Throwable e )
    {
      //Completely ignorable
    }
  }

  protected void startDbCleaner()
  {
    try
    {
      final List<Binding<DbCleaner>> bindings =
        getInjector().findBindingsByType( TypeLiteral.get( DbCleaner.class ) );
      for ( final Binding<DbCleaner> binding : bindings )
      {
        final DbCleaner cleaner = binding.getProvider().get();
        cleaner.start();
      }
    }
    catch ( final ConfigurationException e )
    {
      // Ignore this as we assume that if DbCleaner is unable to be located
      // then we are probably using a mock EntityManager
    }
  }

  protected void finishDbCleaner()
  {
    try
    {
      final List<Binding<DbCleaner>> bindings =
        getInjector().findBindingsByType( TypeLiteral.get( DbCleaner.class ) );
      for ( final Binding<DbCleaner> binding : bindings )
      {
        final DbCleaner cleaner = binding.getProvider().get();
        cleaner.finish();
      }
    }
    catch ( final ConfigurationException e )
    {
      // Ignore this as we assume that if DbCleaner is unable to be located
      // then we are probably using a mock EntityManager
    }
  }

  protected void startupMailServer()
  {
    if ( enableMailServer() )
    {

      try
      {
        final Object greenMail = s( GreenMail.class );
        GreenMail.class.getMethod( "start" ).invoke( greenMail );
        ensureThreadStarted( (Thread) GreenMail.class.getMethod( "getSmtp" ).invoke( greenMail ) );
        ensureThreadStarted( (Thread) GreenMail.class.getMethod( "getSmtps" ).invoke( greenMail ) );
        ensureThreadStarted( (Thread) GreenMail.class.getMethod( "getImap" ).invoke( greenMail ) );
        ensureThreadStarted( (Thread) GreenMail.class.getMethod( "getImaps" ).invoke( greenMail ) );
        ensureThreadStarted( (Thread) GreenMail.class.getMethod( "getSmtp" ).invoke( greenMail ) );
        ensureThreadStarted( (Thread) GreenMail.class.getMethod( "getPop3" ).invoke( greenMail ) );
        ensureThreadStarted( (Thread) GreenMail.class.getMethod( "getPop3s" ).invoke( greenMail ) );

        //A small sleep to ensure that all listeners have established server sockets
        Thread.sleep( 1 );
      }
      catch ( final IllegalAccessException iae )
      {
        //Ignored
      }
      catch ( final InvocationTargetException ite )
      {
        //Ignored
      }
      catch ( final NoSuchMethodException nsme )
      {
        //Ignored
      }
      catch ( final InterruptedException ie )
      {
        //Ignored
      }
    }
  }

  private void ensureThreadStarted( final Thread thread )
  {
    if ( null != thread )
    {
      //Wait until the thread has started
      while ( !thread.isAlive() )
      {
        Thread.yield();
      }
    }
  }

  protected void shutdownMailServer()
  {
    if ( enableMailServer() )
    {
      s( GreenMail.class ).stop();
    }
  }

  protected boolean enableMailServer()
  {
    return false;
  }

  protected void setupTransactionSynchronizationRegistry()
    throws Exception
  {
    setupJndiSubContext( "java:comp" );
    bindJndiResource( "java:comp/TransactionSynchronizationRegistry", TransactionSynchronizationRegistry.class );
  }

  protected void shutdownTransactionSynchronizationRegistry()
  {
    unbindJndiResource( "java:comp/TransactionSynchronizationRegistry" );
  }

  protected void setupBeanManager()
    throws Exception
  {
    setupJndiSubContext( "java:comp" );
    bindJndiResource( "java:comp/BeanManager", BeanManager.class );
  }

  protected void shutdownBeanManager()
  {
    unbindJndiResource( "java:comp/BeanManager" );
  }

  protected void setupJndiSubContext( @Nonnull final String name )
    throws NamingException
  {
    try
    {
      TestInitialContextFactory.getContext().createSubcontext( name );
    }
    catch ( final NameAlreadyBoundException ne )
    {
      //Already bound. That is fine
    }
    catch ( final NoClassDefFoundError e )
    {
      //Ignored. Probably as the classes for the naming or transaction extensions are not on the classpath
    }
  }

  protected void bindJndiResource( final String key, final Class<?> type )
    throws NamingException
  {
    try
    {
      TestInitialContextFactory.getContext().bind( key, getInstance( type ) );
    }
    catch ( final ConfigurationException ce )
    {
      //Ignored as there is no TransactionSynchronizationRegistry bound
    }
    catch ( final NoClassDefFoundError e )
    {
      //Ignored. Probably as the classes for the naming or transaction extensions are not on the classpath
    }
  }

  protected void unbindJndiResource( final String name )
  {
    try
    {
      final Context context = TestInitialContextFactory.getContext();
      if ( null != context )
      {
        context.unbind( name );
      }
    }
    catch ( final NamingException ne )
    {
      //Not bound. No problem
    }
    catch ( final NoClassDefFoundError e )
    {
      //Ignored. Probably as the classes for the naming or transaction extensions are not on the classpath
    }
  }

  protected void resetJndiContext()
  {
    try
    {
      TestInitialContextFactory.reset();
    }
    catch ( final NoClassDefFoundError e )
    {
      //JNDI code is not present. NO problemo!
    }
  }

  protected void clearJndiContext()
  {
    try
    {
      TestInitialContextFactory.clear();
    }
    catch ( final NoClassDefFoundError e )
    {
      //JNDI code is not present. NO problemo!
    }
  }

  @Override
  protected Module[] getModules()
  {
    final ArrayList<Module> modules = new ArrayList<>();
    Collections.addAll( modules, super.getModules() );
    modules.add( new JEETestingModule() );
    modules.add( new ServerTestModule() );
    addModule( modules, getEntityModule() );
    addModule( modules, getMailTestModule() );
    return modules.toArray( new Module[ modules.size() ] );
  }

  @Nullable
  protected Module getMailTestModule()
  {
    return null;
  }

  /**
   * The name of the primary persistence unit. Should correspond to the unit identified by getPrimaryPersistenceBindName().
   */
  @Nullable
  protected String getPrimaryPersistenceUnitName()
  {
    return null;
  }

  /**
   * Override this to return the name under which the persistence unit is bound. It will be flushed
   * between service calls. If not overridden or if the method returns null, EntityManager will be
   * the one not bound to a name.
   */
  @Nullable
  protected String getPrimaryPersistenceBindName()
  {
    return getPrimaryPersistenceUnitName();
  }

  /**
   * Return the module used to provide entity support.
   *
   * @deprecated Avoid using method and instead override getModules() to add EntityModule(s).
   */
  @Deprecated
  protected Module getEntityModule()
  {
    return null;
  }

  protected Module getTestModule()
  {
    final String testModuleClassname = getClass().getName() + "$TestModule";
    try
    {
      return (AbstractModule) Class.forName( testModuleClassname ).newInstance();
    }
    catch ( final Throwable t )
    {
      return getDefaultTestModule();
    }
  }

  @Override
  protected <T> T s( final TypeLiteral<T> literal )
  {
    flush();
    return super.s( literal );
  }

  @Override
  protected final <T> T s( final Class<T> type )
  {
    // Flush the entity manager prior to invoking the service. Ensures that the service method can
    // find all created artifacts
    flush();
    return super.s( type );
  }

  @Override
  protected final <T> T s( final String name, final Class<T> type )
  {
    // Flush the entity manager prior to invoking the service. Ensures that the service method can
    // find all created artifacts
    flush();
    return super.s( name, type );
  }

  /**
   * Retrieve the EventStub implementation associated with event literal.
   * It is assumed that the event stub is bound in the associated module.
   */
  @Nonnull
  protected final <T> EventStub<T> eventStub( @Nonnull final TypeLiteral<Event<T>> literal )
  {
    return (EventStub<T>) s( literal );
  }

  protected final void resetTransactionSynchronizationRegistry()
  {
    ( (TestTransactionSynchronizationRegistry) s( TransactionSynchronizationRegistry.class ) ).clear();
  }

  public void flush()
  {
    // Unfortunately we have to ensure that it is only the test thread that invokes flush
    // because the proxies generated by guice also proxy the finalize method (!!!) which may result
    // in calling the FlushingInterceptor which calls this method during finalization. This causes
    // th EntityManager context to go into rollback status and randomly fail some tests when they try
    // to perform cleaning
    if ( !_inFlush && _testThread == Thread.currentThread() )
    {
      try
      {
        _inFlush = true;
        final List<Binding<EntityManager>> bindings =
          getInjector().findBindingsByType( TypeLiteral.get( EntityManager.class ) );
        for ( final Binding<EntityManager> binding : bindings )
        {
          final EntityManager em = binding.getProvider().get();
          final EntityTransaction transaction = em.getTransaction();
          // We check for null here to simplify testing with Mock persistence modules
          // If we don't guard against null then we have to ensure every test mocks
          // out the transaction and returns a transaction which is a PITA.
          if ( null != transaction && transaction.isActive() && !transaction.getRollbackOnly() )
          {
            em.flush();
          }
        }
      }
      finally
      {
        _inFlush = false;
      }
    }
  }

  protected final void clear()
  {
    final List<Binding<EntityManager>> bindings =
      getInjector().findBindingsByType( TypeLiteral.get( EntityManager.class ) );
    for ( final Binding<EntityManager> binding : bindings )
    {
      final EntityManager em = binding.getProvider().get();
      em.clear();
    }
  }

  protected final void usesTransaction()
  {
    final List<Binding<DbCleaner>> bindings =
      getInjector().findBindingsByType( TypeLiteral.get( DbCleaner.class ) );
    for ( final Binding<DbCleaner> binding : bindings )
    {
      final DbCleaner cleaner = binding.getProvider().get();
      cleaner.usesTransaction();
    }
  }

  protected final void beginTransaction()
  {
    final List<Binding<EntityManager>> bindings =
      getInjector().findBindingsByType( TypeLiteral.get( EntityManager.class ) );
    for ( final Binding<EntityManager> binding : bindings )
    {
      final EntityManager em = binding.getProvider().get();
      em.getTransaction().begin();
    }
  }

  protected final void commitTransaction()
  {
    final List<Binding<EntityManager>> bindings =
      getInjector().findBindingsByType( TypeLiteral.get( EntityManager.class ) );
    for ( final Binding<EntityManager> binding : bindings )
    {
      final EntityManager em = binding.getProvider().get();
      em.getTransaction().commit();
    }
  }

  protected final void rollbackTransaction()
  {
    final List<Binding<EntityManager>> bindings =
      getInjector().findBindingsByType( TypeLiteral.get( EntityManager.class ) );
    for ( final Binding<EntityManager> binding : bindings )
    {
      final EntityManager em = binding.getProvider().get();
      em.getTransaction().rollback();
    }
  }

  protected final <T> T refresh( final T entity )
  {
    em().refresh( entity );
    return entity;
  }

  @Nonnull
  protected final EntityManager em()
  {
    return em( getPrimaryPersistenceBindName() );
  }

  @Nonnull
  protected final EntityManager em( @Nullable final String unitName )
  {
    if ( null == unitName )
    {
      return getInstance( EntityManager.class );
    }
    else
    {
      return getInstance( unitName, EntityManager.class );
    }
  }

  private final class EntityManagerClearingCallable<T>
    implements Callable<T>
  {
    private final Callable<T> _target;

    EntityManagerClearingCallable( final Callable<T> target )
    {
      _target = target;
    }

    @Override
    public T call()
      throws Exception
    {
      em().clear();
      return _target.call();
    }
  }

  /**
   * Clear EntityManager and wrap in transaction.
   */
  protected final <T> T ctran( @Nonnull final Callable<T> action )
    throws Exception
  {
    return inTransaction( new EntityManagerClearingCallable<>( action ) );
  }

  protected final <T> T ctran( @Nonnull final String entityManagerName,
                               @Nonnull final Callable<T> action )
    throws Exception
  {
    return inTransaction( entityManagerName, new EntityManagerClearingCallable<>( action ) );
  }

  protected final <T> T ctran( @Nonnull final EntityManager entityManager,
                               @Nonnull final Callable<T> action )
    throws Exception
  {
    return inTransaction( entityManager, new EntityManagerClearingCallable<>( action ) );
  }

  protected final <T> T tran( @Nonnull final Callable<T> action )
    throws Exception
  {
    return inTransaction( action );
  }

  protected final <T> T tran( @Nonnull final String entityManagerName,
                              @Nonnull final Callable<T> action )
    throws Exception
  {
    return inTransaction( em( entityManagerName ), action );
  }

  protected final <T> T tran( @Nonnull final EntityManager entityManager,
                              @Nonnull final Callable<T> action )
    throws Exception
  {
    return inTransaction( entityManager, action );
  }

  protected final <T> T inTransaction( @Nonnull final Callable<T> action )
    throws Exception
  {
    return inTransaction( em(), action );
  }

  protected final <T> T inTransaction( @Nonnull final String entityManagerName,
                                       @Nonnull final Callable<T> action )
    throws Exception
  {
    return inTransaction( em( entityManagerName ), action );
  }

  protected final <T> T inTransaction( @Nonnull final EntityManager entityManager,
                                       @Nonnull final Callable<T> action )
    throws Exception
  {
    final EntityTransaction transaction = entityManager.getTransaction();
    try
    {
      transaction.begin();
      return action.call();
    }
    finally
    {
      completeTransaction( transaction );
    }
  }

  private final class EntityManagerClearingRunnable
    implements Runnable
  {
    private final Runnable _target;

    EntityManagerClearingRunnable( final Runnable target )
    {
      _target = target;
    }

    @Override
    public void run()
    {
      em().clear();
      _target.run();
    }
  }

  protected final void ctran( @Nonnull final Runnable action )
  {
    inTransaction( new EntityManagerClearingRunnable( action ) );
  }

  protected final void ctran( @Nonnull final String entityManagerName, @Nonnull final Runnable action )
  {
    inTransaction( entityManagerName, new EntityManagerClearingRunnable( action ) );
  }

  protected final void ctran( @Nonnull final EntityManager entityManager, @Nonnull final Runnable action )
  {
    inTransaction( entityManager, new EntityManagerClearingRunnable( action ) );
  }

  protected final void tran( @Nonnull final Runnable action )
  {
    inTransaction( action );
  }

  protected final void tran( @Nonnull final String entityManagerName, @Nonnull final Runnable action )
  {
    tran( em( entityManagerName ), action );
  }

  protected final void tran( @Nonnull final EntityManager entityManager, @Nonnull final Runnable action )
  {
    inTransaction( entityManager, action );
  }

  protected final void inTransaction( @Nonnull final Runnable action )
  {
    inTransaction( em(), action );
  }

  protected final void inTransaction( @Nonnull final String entityManagerName, @Nonnull final Runnable action )
  {
    inTransaction( em( entityManagerName ), action );
  }

  protected final void inTransaction( @Nonnull final EntityManager entityManager, @Nonnull final Runnable action )
  {
    final EntityTransaction transaction = entityManager.getTransaction();
    try
    {
      transaction.begin();
      action.run();
    }
    finally
    {
      completeTransaction( transaction );
    }
  }

  protected final void completeTransaction( @Nonnull final EntityTransaction transaction )
  {
    if ( transaction.getRollbackOnly() )
    {
      transaction.rollback();
    }
    else
    {
      transaction.commit();
    }
  }

  @Nonnull
  protected QueueBrowser getQueueBrowser( @Nonnull final String resourceName )
  {
    return jmsContext().createBrowser( s( resourceName, Queue.class ) );
  }

  @Nonnull
  protected JMSContext jmsContext()
  {
    return jmsContext( getPrimaryJmsConnectionFactoryName() );
  }

  @Nonnull
  protected String getPrimaryJmsConnectionFactoryName()
  {
    throw new IllegalStateException( "getPrimaryJmsConnectionFactoryName invoked but method not overridden" );
  }

  @Nonnull
  protected JMSContext jmsContext( @Nonnull final String name )
  {
    return getInjector().getInstance( Key.get( JMSContext.class, new JMSConnectionFactoryImpl( name ) ) );
  }

  @Nonnull
  protected OpenMQContainer broker()
  {
    return broker( getPrimaryBrokerName() );
  }

  @Nonnull
  protected String getPrimaryBrokerName()
  {
    throw new IllegalStateException( "getPrimaryBrokerName invoked but method not overridden" );
  }

  @Nonnull
  protected OpenMQContainer broker( @Nonnull final String name )
  {
    return getInstance( name, OpenMQContainer.class );
  }
}
