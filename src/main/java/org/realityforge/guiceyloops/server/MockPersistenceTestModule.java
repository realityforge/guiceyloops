package org.realityforge.guiceyloops.server;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

public class MockPersistenceTestModule
  extends AbstractPersistenceTestModule
{
  private final boolean _bindWithoutName;
  private final String _persistenceUnit;
  private final boolean _registerUserTransaction;

  public MockPersistenceTestModule( final String persistenceUnit,
                                    final boolean bindWithoutName,
                                    final boolean registerUserTransaction )
  {
    _bindWithoutName = bindWithoutName;
    _persistenceUnit = persistenceUnit;
    _registerUserTransaction = registerUserTransaction;
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
    if ( _registerUserTransaction )
    {
      registerUserTransaction();
    }
  }

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
