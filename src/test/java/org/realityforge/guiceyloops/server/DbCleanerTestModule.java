package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import javax.persistence.EntityManager;

class DbCleanerTestModule
  extends AbstractModule
{
  private final EntityManager _entityManager;
  private final String[] _tables;

  DbCleanerTestModule( final EntityManager entityManager, final String[] tables )
  {
    _entityManager = entityManager;
    _tables = tables;
  }

  @Override
  protected void configure()
  {
    bind( EntityManager.class ).toInstance( _entityManager );
    bind( DbCleaner.class ).toInstance( new DbCleaner( _tables, _entityManager ) );
  }
}
