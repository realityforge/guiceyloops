package org.realityforge.guiceyloops.server;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import org.realityforge.guiceyloops.shared.AbstractModule;

public class MockPersistenceTestModule
  extends AbstractModule
{
  @Nonnull
  private final String _persistenceUnit;

  public MockPersistenceTestModule( @Nonnull final String persistenceUnit )
  {
    _persistenceUnit = persistenceUnit;
  }

  @Override
  protected void configure()
  {
    bindMock( EntityManager.class, _persistenceUnit );
  }
}
