package org.realityforge.guiceyloops.server;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

@SuppressWarnings( "unused" )
public class AssertUtilTest
{
  public static class TypeWithoutFinal
  {
    public void m1()
    {
    }

    protected void m2()
    {
    }

    private void m3()
    {
    }

    void m4()
    {
    }
  }

  public static class TypeWithoutFinal2
    extends TypeWithoutFinal
  {
  }

  public static class TypeWithFinalPublic
  {
    public final void m1()
    {
    }
  }

  public static class TypeWithFinalPublic2
    extends TypeWithFinalPublic
  {
  }

  public static class TypeWithFinalProtected
  {
    protected final void m1()
    {
    }
  }

  public static class TypeWithFinalProtected2
    extends TypeWithFinalProtected
  {
  }

  public static class TypeWithFinalPrivate
  {
    @SuppressWarnings( "FinalPrivateMethod" )
    private final void m1()
    {
    }
  }

  public static class TypeWithFinalPrivate2
    extends TypeWithFinalPrivate
  {
  }

  public static class TypeWithFinalPackage
  {
    final void m1()
    {
    }
  }

  public static class TypeWithFinalPackage2
    extends TypeWithFinalPackage
  {
  }

  @Test
  public void assertNoFinalMethodsForCDI()
  {
    AssertUtil.assertNoFinalMethodsForCDI( TypeWithoutFinal.class );
    AssertUtil.assertNoFinalMethodsForCDI( TypeWithoutFinal2.class );

    assertNotCDI( TypeWithFinalPublic.class );
    assertNotCDI( TypeWithFinalProtected.class );
    assertNotCDI( TypeWithFinalPrivate.class );
    assertNotCDI( TypeWithFinalPackage.class );

    assertNotCDI( TypeWithFinalPublic2.class );
    assertNotCDI( TypeWithFinalProtected2.class );
    assertNotCDI( TypeWithFinalPrivate2.class );
    assertNotCDI( TypeWithFinalPackage2.class );
  }

  private void assertNotCDI( final Class<?> type )
  {
    try
    {
      AssertUtil.assertNoFinalMethodsForCDI( type );
    }
    catch ( final Throwable e )
    {
      final String expected =
        "Method m1 on " +
        type.getName() +
        " is final and thus not compatible with CDI expected [false] but found [true]";
      final String actual = e.getMessage();
      assertEquals( actual, expected );
    }
  }
}
