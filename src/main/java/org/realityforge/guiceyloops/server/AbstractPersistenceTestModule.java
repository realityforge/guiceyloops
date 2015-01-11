package org.realityforge.guiceyloops.server;

import org.realityforge.guiceyloops.shared.AbstractModule;

public abstract class AbstractPersistenceTestModule
  extends AbstractModule
{
  protected void configure()
  {
  }

  /**
   * @return the name of the persistence unit under test.
   */
  protected abstract String getPersistenceUnitName();
}
