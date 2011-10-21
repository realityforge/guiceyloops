package org.realityforge.guiceyloops;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class JEETestingModule
    extends AbstractModule
{
  protected void configure()
  {
    bindListener( Matchers.any(), new JpaTypeListener() );
    bindListener( Matchers.any(), new EjbTypeListener() );
    bindListener( Matchers.any(), new ResourceTypeListener() );
  }
}
