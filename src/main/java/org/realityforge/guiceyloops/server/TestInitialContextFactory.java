/*
 * Copyright (C) The Spice Group. All rights reserved.
 *
 * This software is published under the terms of the Spice
 * Software License version 1.1, a copy of which has been included
 * with this distribution in the LICENSE.txt file.
 */
package org.realityforge.guiceyloops.server;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import org.realityforge.spice.jndikit.DefaultNameParser;
import org.realityforge.spice.jndikit.DefaultNamespace;
import org.realityforge.spice.jndikit.memory.MemoryContext;

/**
 * Utility in-memory JNDI context useful for testing.
 */
public class TestInitialContextFactory
  implements InitialContextFactory
{
  private static MemoryContext c_context;

  public Context getInitialContext( final Hashtable environment )
    throws NamingException
  {
    return getContext();
  }

  public static MemoryContext getContext()
  {
    return c_context;
  }

  public static void reset()
  {
    System.setProperty( "java.naming.factory.initial", TestInitialContextFactory.class.getName() );
    final DefaultNamespace namespace = new DefaultNamespace( new DefaultNameParser() );
    c_context = new MemoryContext( namespace, new Hashtable(), null );
  }

  public static void clear()
  {
    System.getProperties().remove( "java.naming.factory.initial" );
    c_context = null;
  }
}

