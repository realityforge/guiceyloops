package org.realityforge.guiceyloops.server;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Status;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class TestUserTransactionTest
{
  @Test
  public void basicOperation()
    throws Exception
  {
    final EntityManager em = mock( EntityManager.class );
    final EntityTransaction tr = mock( EntityTransaction.class );

    final TestUserTransaction transaction = new TestUserTransaction( em );

    doReturn( tr ).when( em ).getTransaction();
    transaction.begin();
    verify( tr ).begin();

    transaction.commit();
    verify( tr ).commit();

    transaction.rollback();
    verify( tr ).rollback();

    transaction.setRollbackOnly();
    verify( tr ).setRollbackOnly();

    doReturn( true ).when( tr ).isActive();
    assertEquals( transaction.getStatus(), Status.STATUS_ACTIVE );
  }
}
