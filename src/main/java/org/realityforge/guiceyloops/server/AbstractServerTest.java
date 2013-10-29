package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.icegreen.greenmail.util.GreenMail;
import java.util.ArrayList;
import javax.annotation.Nullable;
import javax.naming.Context;
import javax.persistence.EntityManager;
import javax.transaction.TransactionSynchronizationRegistry;
import org.realityforge.guiceyloops.JEETestingModule;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractServerTest
{
  private Injector _injector;

  @BeforeMethod
  public void preTest()
    throws Exception
  {
    _injector = Guice.createInjector( getModules() );
    startDbCleaner();
    setupTransactionSynchronizationRegistry();
    startupMailServer();
  }

  @AfterMethod
  public void postTest()
  {
    shutdownMailServer();
    shutdownTransactionSynchronizationRegistry();
    finishDbCleaner();
    _injector = null;
  }

  protected void startDbCleaner()
  {
    try
    {
      getService( DbCleaner.class ).start();
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
      getService( DbCleaner.class ).finish();
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
  {
    try
    {
      shutdownTransactionSynchronizationRegistry();
      final Context context = TestInitialContextFactory.getContext().createSubcontext( "java:comp" );
      context.bind( "TransactionSynchronizationRegistry", getService( TestTransactionSynchronizationRegistry.class ) );
    }
    catch ( final Throwable t )
    {
      //Ignored. Probably as the classes for the naming or transaction extensions are not on the classpath
    }
  }

  protected void shutdownTransactionSynchronizationRegistry()
  {
    TestInitialContextFactory.reset();
  }

  protected Module[] getModules()
  {
    final ArrayList<Module> modules = new ArrayList<Module>();
    modules.add( new JEETestingModule() );
    addModule( modules, getTestModule() );
    addModule( modules, getEntityModule() );
    addModule( modules, getMailTestModule() );
    return modules.toArray( new Module[ modules.size() ] );
  }

  protected final void addModule( final ArrayList<Module> modules, @Nullable final Module module )
  {
    if ( null != module )
    {
      modules.add( module );
    }
  }

  @Nullable
  protected Module getMailTestModule()
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
  protected ServerTestModule getDefaultTestModule()
  {
    return new ServerTestModule();
  }

  protected final <T> T s( final Class<T> type )
  {
    // Flush the entity manager prior to invoking the service. Ensures that the service method can
    // find all created artifacts
    flush();
    return getService( type );
  }

  protected final <T> T s( final String name, final Class<T> type )
  {
    // Flush the entity manager prior to invoking the service. Ensures that the service method can
    // find all created artifacts
    flush();
    return getService( name, type );
  }

  protected final void resetTransactionSynchronizationRegistry()
  {
    ( (TestTransactionSynchronizationRegistry) s( TransactionSynchronizationRegistry.class ) ).clear();
  }

  protected final void flush()
  {
    em().flush();
  }

  protected final void clear()
  {
    em().clear();
  }

  protected final <T> T refresh( final T entity )
  {
    em().refresh( entity );
    return entity;
  }

  protected final EntityManager em()
  {
    return getService( EntityManager.class );
  }

  private <T> T getService( final Class<T> type )
  {
    return getInjector().getInstance( type );
  }

  private <T> T getService( final String name, final Class<T> type )
  {
    return getInjector().getInstance( Key.get( type, Names.named( name ) ) );
  }

  protected <T> T toObject( final Class<T> type, final Object object )
  {
    return InjectUtil.toObject( type, object );
  }

  protected final Injector getInjector()
  {
    return _injector;
  }
}
