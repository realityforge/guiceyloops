package org.realityforge.guiceyloops.server;

import javax.persistence.EntityManager;
import org.realityforge.guiceyloops.shared.AbstractModule;

public class MockPersistenceTestModule
  extends AbstractModule
{
  private final String _persistenceUnit;

  public MockPersistenceTestModule( final String persistenceUnit )
  {
    _persistenceUnit = persistenceUnit;
  }

  @Override
  protected void configure()
  {
    if ( null != _persistenceUnit )
    {
      bindMock( EntityManager.class, _persistenceUnit );
    }
  }
}
