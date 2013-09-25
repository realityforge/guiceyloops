package org.realityforge.guiceyloops.server;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.icegreen.greenmail.util.GreenMail;

public class GreenMailTestModule
  extends AbstractModule
{
  @Override
  protected void configure()
  {
    bind( GreenMail.class ).in( Singleton.class );
  }
}
