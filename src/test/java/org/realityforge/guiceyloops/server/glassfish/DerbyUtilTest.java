package org.realityforge.guiceyloops.server.glassfish;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DerbyUtilTest
{
  @Test
  public void configureNullLogger()
  {
    DerbyUtil.configureNullLogger();
    assertEquals( System.getProperty( "derby.stream.error.field" ),
                  "org.realityforge.guiceyloops.server.glassfish.DerbyUtil.DEV_NULL" );
  }
}
