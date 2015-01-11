package org.realityforge.guiceyloops.server;

import javax.persistence.EntityManager;
import org.realityforge.guiceyloops.shared.AbstractModule;

public class MockPersistenceTestModule
  extends AbstractModule
{
  private final boolean _bindWithoutName;
  private final String _persistenceUnit;

  public MockPersistenceTestModule( final String persistenceUnit, final boolean bindWithoutName )
  {
    _bindWithoutName = bindWithoutName;
    _persistenceUnit = persistenceUnit;
  }

  @Override
  protected void configure()
  {
    if ( _bindWithoutName )
    {
      bindMock( EntityManager.class );
    }
    if ( null != _persistenceUnit )
    {
      bindMock( EntityManager.class, _persistenceUnit );
    }
  }
}
