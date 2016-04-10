package org.realityforge.guiceyloops.server;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.annotation.Nonnull;
import static org.testng.Assert.*;

/**
 * Specializes assertions.
 */
public final class AssertUtil
{
  private AssertUtil()
  {
  }

  /**
   * Assert that there is no declared method that is final and thus incompatible with being a CDI bean.
   */
  public static void assertNoFinalMethodsForCDI( @Nonnull final Class clazz )
  {
    final Method[] methods = clazz.getDeclaredMethods();
    for ( final Method method : methods )
    {
      if ( Object.class != method.getDeclaringClass() )
      {
        final String message =
          "Method " + method.getName() + " on " + clazz.getName() + " is final and thus not compatible with CDI";
        assertFalse( Modifier.isFinal( method.getModifiers() ), message );
      }
    }
  }
}
