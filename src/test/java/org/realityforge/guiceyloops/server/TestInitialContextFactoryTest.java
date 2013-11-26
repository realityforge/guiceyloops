package org.realityforge.guiceyloops.server;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NoInitialContextException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public final class TestInitialContextFactoryTest
{
  @BeforeMethod
  @AfterMethod
  public void clearContext()
  {
    TestInitialContextFactory.clear();
  }

  @Test( expectedExceptions = NoInitialContextException.class )
  public void getContext_withoutReset()
    throws Exception
  {
    System.setProperty( "java.naming.factory.initial", "" );
    new InitialContext( new Properties() ).lookup( "X" );
  }

  @Test
  public void getContext_withReset()
    throws Exception
  {
    TestInitialContextFactory.reset();
    final Object object = new Object();
    TestInitialContextFactory.getContext().bind( "foo", object );
    final Context initialContext = new InitialContext( new Properties() );
    assertEquals( initialContext.lookup( "foo" ), object );
  }
}
