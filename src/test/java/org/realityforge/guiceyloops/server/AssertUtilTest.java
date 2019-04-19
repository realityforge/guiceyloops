package org.realityforge.guiceyloops.server;

import javax.annotation.Nonnull;
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

    assertNotCDI( TypeWithFinalPublic.class, TypeWithFinalPublic.class );
    assertNotCDI( TypeWithFinalProtected.class, TypeWithFinalProtected.class );
    assertNotCDI( TypeWithFinalPrivate.class, TypeWithFinalPrivate.class );
    assertNotCDI( TypeWithFinalPackage.class, TypeWithFinalPackage.class );

    assertNotCDI( TypeWithFinalPublic.class, TypeWithFinalPublic2.class );
    assertNotCDI( TypeWithFinalProtected.class, TypeWithFinalProtected2.class );
    assertNotCDI( TypeWithFinalPrivate.class, TypeWithFinalPrivate2.class );
    assertNotCDI( TypeWithFinalPackage.class, TypeWithFinalPackage2.class );
  }

  private void assertNotCDI( @Nonnull final Class<?> declaredType, @Nonnull final Class<?> type )
  {
    try
    {
      AssertUtil.assertNoFinalMethodsForCDI( type );
    }
    catch ( final Throwable e )
    {
      final String expected =
        "Method m1 on " +
        declaredType.getName() +
        " is final and thus not compatible with CDI expected [false] but found [true]";
      assertEquals( e.getMessage(), expected );
      return;
    }
    fail( "Expected to fail " + type.getName() + "as bad CDI type" );
  }
}
