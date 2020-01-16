package org.realityforge.guiceyloops.server;

import javax.transaction.Status;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TestTransactionSynchronizationRegistryTest
{
  @Test
  public void putResource_getResource_clear()
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

  @Test
  public void getTransactionStatus()
  {
    final TestTransactionSynchronizationRegistry registry =
      new TestTransactionSynchronizationRegistry();
    assertEquals( Status.STATUS_ACTIVE, registry.getTransactionStatus() );
    assertFalse( registry.getRollbackOnly() );

    registry.setRollbackOnly();

    assertEquals( Status.STATUS_MARKED_ROLLBACK, registry.getTransactionStatus() );
    assertTrue( registry.getRollbackOnly() );

    registry.setTransactionStatus( Status.STATUS_NO_TRANSACTION );

    assertEquals( Status.STATUS_NO_TRANSACTION, registry.getTransactionStatus() );
  }
}
