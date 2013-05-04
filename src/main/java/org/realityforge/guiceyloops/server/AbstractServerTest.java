package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
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
    getService( DbCleaner.class ).start();
    try
    {
      TestInitialContextFactory.reset();
      final Context context = TestInitialContextFactory.getContext().createSubcontext( "java:comp" );
      context.bind( "TransactionSynchronizationRegistry", getService( TestTransactionSynchronizationRegistry.class ) );
    }
    catch ( final Throwable t )
    {
      //Ignored. Probably as the classes for the naming or transaction extensions are not on the classpath
    }
  }

  @AfterMethod
  public void postTest()
  {
    getService( DbCleaner.class ).finish();
  }

  protected Module[] getModules()
  {
    return new Module[] {
      getTestModule(),
      getEntityModule(),
      new JEETestingModule()
    };
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
      return new ServerTestModule();
    }
  }

  protected final <T> T s( final Class<T> type )
  {
    // Flush the entity manager prior to invoking the service. Ensures that the service method can
    // find all created artifacts
    flush();
    return getService( type );
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

  protected final Injector getInjector()
  {
    return _injector;
  }
}
