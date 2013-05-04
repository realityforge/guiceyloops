package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import javax.persistence.EntityManager;

class FlushingInterceptorTestModule
  extends AbstractModule
{
  private final EntityManager _entityManager;

  FlushingInterceptorTestModule( final EntityManager entityManager )
  {
    _entityManager = entityManager;
  }

  @Override
  protected void configure()
  {
    bind( EntityManager.class ).toInstance( _entityManager );
  }
}
