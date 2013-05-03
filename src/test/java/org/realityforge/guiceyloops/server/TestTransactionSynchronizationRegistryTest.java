package org.realityforge.guiceyloops.server;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TestTransactionSynchronizationRegistryTest
{
  @Test
  public void putResource_getResource_clear()
    throws Exception
  {
    final TestTransactionSynchronizationRegistry registry =
      new TestTransactionSynchronizationRegistry();
    assertNull( registry.getResource( "foo" ) );
    final Object foo = new Object();
    registry.putResource( "foo", foo );
    assertEquals( registry.getResource( "foo" ), foo );
    registry.clear();
    assertNull( registry.getResource( "foo" ) );
  }
}
