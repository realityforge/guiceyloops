package org.realityforge.guiceyloops.server.glassfish;

import java.io.File;
import java.net.InetAddress;
import org.realityforge.guiceyloops.server.TestUtil;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class GlassFishContainerTest
{
  @Test
  public void basicWorkflow()
    throws Exception
  {
    final GlassFishContainer container = new GlassFishContainer();
    final int port = container.getPort();

    assertEquals( container.getBaseHttpURL(),
                  "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port );

    File databaseFile = null;
    try
    {
      container.start();

      container.createCustomResource( "Foo", "Bar" );
      assertTrue( container.execute( "list-custom-resources" ).contains( "\n    Foo\n" ) );

      databaseFile = TestUtil.setupDatabase();
//      container.createJdbcResource( "jdbc/Foo", "Bar" );

    }
    finally
    {
      if ( null != databaseFile && !databaseFile.delete() )
      {
        databaseFile.deleteOnExit();
      }

      container.stop();
    }
  }
}
