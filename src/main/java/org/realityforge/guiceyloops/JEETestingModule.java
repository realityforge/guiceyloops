package org.realityforge.guiceyloops;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

/**
 * The Guice module that should be added to the injector to add annotation listeners.
 */
public class JEETestingModule
    extends AbstractModule
{
  /**
   * {@inheritDoc}
   */
  protected void configure()
  {
    bindListener( Matchers.any(), new JpaTypeListener() );
    bindListener( Matchers.any(), new EjbTypeListener() );
    bindListener( Matchers.any(), new ResourceTypeListener() );
  }
}
