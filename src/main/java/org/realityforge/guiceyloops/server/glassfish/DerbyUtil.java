package org.realityforge.guiceyloops.server.glassfish;

import java.io.OutputStream;

/**
 * Ugly class to silence the silly derby logs.
 */
public final class DerbyUtil
{
  private DerbyUtil()
  {
  }

  public static void configureNullLogger()
  {
    System.setProperty( "derby.stream.error.field", DerbyUtil.class.getName() + ".DEV_NULL" );
  }

  @SuppressWarnings( "UnusedDeclaration" )
  public static final OutputStream DEV_NULL = new OutputStream()
  {
    public void write( int b )
    {
    }
  };
}