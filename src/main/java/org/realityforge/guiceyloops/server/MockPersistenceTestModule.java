package org.realityforge.guiceyloops.server;

import javax.persistence.EntityManager;

public class MockPersistenceTestModule
  extends AbstractPersistenceTestModule
{
  private final String _persistenceUnit;

  public MockPersistenceTestModule( final String persistenceUnit )
  {
    _persistenceUnit = persistenceUnit;
  }

  @Override
  protected void configure()
  {
    super.configure();
    bindMock( EntityManager.class, getPersistenceUnitName() );
  }

  @Override
  protected String getPersistenceUnitName()
  {
    return _persistenceUnit;
  }
}
