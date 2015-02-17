package org.realityforge.guiceyloops.shared;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.annotation.Nullable;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

public abstract class AbstractSharedTest
{
  private Injector _injector;

  @BeforeMethod
  public void preTest()
    throws Exception
  {
    _injector = Guice.createInjector( getModules() );
  }

  @AfterMethod
  public void postTest()
  {
    _injector = null;
  }

  protected Module[] getModules()
  {
    final ArrayList<Module> modules = new ArrayList<Module>();
    addModule( modules, getTestModule() );
    return modules.toArray( new Module[ modules.size() ] );
  }

  protected final void addModule( final ArrayList<Module> modules, @Nullable final Module module )
  {
    if ( null != module )
    {
      modules.add( module );
    }
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

  /**
   * Return the module used if no per-test module defined.
   *
   * @deprecated Avoid using method and instead override getModules() to add EntityModule(s).
   */
  @Deprecated
  protected Module getDefaultTestModule()
  {
    return null;
  }

  protected <T> T s( final Class<T> type )
  {
    return getInstance( type );
  }

  protected <T> T s( final String name, final Class<T> type )
  {
    return getInstance( name, type );
  }

  protected <T> T getInstance( final Class<T> type )
  {
    return getInjector().getInstance( type );
  }

  protected <T> T getInstance( final String name, final Class<T> type )
  {
    return getInjector().getInstance( Key.get( type, Names.named( name ) ) );
  }

  protected final <I, T extends I> T toObject( final Class<T> type, final I object )
  {
    return InjectUtil.toObject( type, object );
  }

  protected final Injector getInjector()
  {
    return _injector;
  }

  /**
   * Retrieve the field on type specified by name.
   * Guice injected classes may be subclasses so this method searches type hierarchy to get to the field.
   */
  protected final Field getField( final Class<?> type, final String fieldName )
    throws NoSuchFieldException
  {
    Class<?> clazz = type;
    while ( Object.class != clazz )
    {
      try
      {
        final Field field = clazz.getDeclaredField( fieldName );
        field.setAccessible( true );
        return field;
      }
      catch ( final Throwable t )
      {
        clazz = clazz.getSuperclass();
      }
    }
    fail();
    return null;
  }

  protected final void setField( final Object object, final String fieldName, final Object value )
    throws NoSuchFieldException, IllegalAccessException
  {
    getField( object.getClass(), fieldName ).set( object, value );
  }
}
