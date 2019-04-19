package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * The Guice module that should be added to the injector to add annotation listeners.
 */
public class JEETestingModule
  extends AbstractModule
{
  protected void configure()
  {
    bindListener( Matchers.any(), new JpaTypeListener() );
    bindListener( Matchers.any(), new EjbTypeListener() );
    bindListener( Matchers.any(), new ResourceTypeListener() );
    bindListener( Matchers.any(), new WebServiceRefTypeListener() );
  }
}
