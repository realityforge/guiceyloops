package org.realityforge.guiceyloops.server;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

public class MockPersistenceTestModule
  extends AbstractPersistenceTestModule
{
  private final boolean _bindWithoutName;
  private final String _persistenceUnit;

  public MockPersistenceTestModule( final boolean bindWithoutName, final String persistenceUnit )
  {
    _bindWithoutName = bindWithoutName;
    _persistenceUnit = persistenceUnit;
  }

  @Override
  protected void configure()
  {
    super.configure();
    if ( _bindWithoutName )
    {
      bindMock( EntityManager.class );
    }
    if ( null != _persistenceUnit )
    {
      bindMock( EntityManager.class, _persistenceUnit );
    }
  }

  @Override
  protected void registerUserTransaction()
  {
    bindMock( UserTransaction.class );
  }

  @Override
  protected String getPersistenceUnitName()
  {
    return _persistenceUnit;
  }
}
