package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.icegreen.greenmail.util.GreenMail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.naming.Context;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.TransactionSynchronizationRegistry;
import org.realityforge.guiceyloops.JEETestingModule;
import org.realityforge.guiceyloops.shared.AbstractSharedTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

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
    super.preTest();
    startDbCleaner();
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
    shutdownEntityManager();
    super.postTest();
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
      s( GreenMail.class ).start();
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
    try
    {
      shutdownTransactionSynchronizationRegistry();
      final Context context = TestInitialContextFactory.getContext().createSubcontext( "java:comp" );
      context.bind( "TransactionSynchronizationRegistry", getInstance( TransactionSynchronizationRegistry.class ) );
    }
    catch ( final NoClassDefFoundError e )
    {
      //Ignored. Probably as the classes for the naming or transaction extensions are not on the classpath
    }
  }

  protected void shutdownTransactionSynchronizationRegistry()
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

  @Override
  protected Module[] getModules()
  {
    final ArrayList<Module> modules = new ArrayList<Module>();
    Collections.addAll( modules, super.getModules() );
    modules.add( new JEETestingModule() );
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
   * Override this to return the name of the persistence unit that will be flushed between service calls.
   * If not overridden or if the method returns null, EntityManager will be the one not bound to a name.
   */
  @Nullable
  protected String getPrimaryPersistenceUnitName()
  {
    return null;
  }

  protected abstract Module getEntityModule();

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

  /**
   * Return the test module that defines all the services.
   * The user is expected to override this in most sub-classes.
   */
  @Override
  protected ServerTestModule getDefaultTestModule()
  {
    return new ServerTestModule( this );
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
        final EntityManager em = em();
        final EntityTransaction transaction = em.getTransaction();
        // We check for null here to simplify testing with Mock persistence modules
        // If we don't guard against null then we have to ensure every test mocks
        // out the transaction and returns a transaction which is a PITA.
        if ( null != transaction && transaction.isActive() && !transaction.getRollbackOnly() )
        {
          em.flush();
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
    em().clear();
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

  protected final <T> T refresh( final T entity )
  {
    em().refresh( entity );
    return entity;
  }

  protected final EntityManager em()
  {
    final String unitName = getPrimaryPersistenceUnitName();
    if ( null == unitName )
    {
      return getInstance( EntityManager.class );
    }
    else
    {
      return getInstance( unitName, EntityManager.class );
    }
  }
}
