package org.realityforge.guiceyloops.shared;

import com.google.inject.Module;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class AbstractSharedTestTest
{
  public static interface Service1
  {
  }

  public static interface Service2
  {
  }

  public static class Component1
    implements Service1, Service2
  {
    int _myField;
  }

  public static class MyServerTest
    extends AbstractSharedTest
  {
    @Override
    protected Module getDefaultTestModule()
    {
      return new AbstractModule()
      {
        @Override
        protected void configure()
        {
          multiBind( Component1.class, Service1.class, Service2.class );
        }
      };
    }
  }

  @Test
  public void postTestNullsInjector()
    throws Exception
  {
    final MyServerTest test = new MyServerTest();
    test.preTest();
    assertNotNull( test.getInjector() );
    test.postTest();
    assertNull( test.getInjector() );
  }

  @Test
  public void toObject()
    throws Exception
  {
    final MyServerTest test = new MyServerTest();
    test.preTest();

    assertEquals( test.toObject( Component1.class, test.s( Service1.class ) ),
                  test.toObject( Component1.class, test.s( Service1.class ) ) );
  }

  @Test
  public void setField()
    throws Exception
  {
    final MyServerTest test = new MyServerTest();
    test.preTest();
    final Component1 component1 = test.toObject( Component1.class, test.s( Service1.class ) );
    assertEquals( component1._myField, 0 );
    test.setField( component1, "_myField", 42 );
    assertEquals( component1._myField, 42 );
  }
}
