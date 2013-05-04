package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import javax.inject.Singleton;
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
    bind( DbCleaner.class ).in( Singleton.class );
    bind( EntityManager.class ).toInstance( _entityManager );
    bind( String[].class ).annotatedWith( Names.named( DbCleaner.TABLE_NAME_KEY ) ).toInstance( _tables );
  }
}
