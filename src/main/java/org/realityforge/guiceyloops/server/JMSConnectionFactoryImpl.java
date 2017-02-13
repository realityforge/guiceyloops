package org.realityforge.guiceyloops.server;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import javax.jms.JMSConnectionFactory;

public class JMSConnectionFactoryImpl
  implements JMSConnectionFactory, Serializable
{
  private final String value;

  public JMSConnectionFactoryImpl( final String value )
  {
    this.value = value;
  }

  public String value()
  {
    return value;
  }

  public int hashCode()
  {
    // This is specified in java.lang.Annotation.
    return ( 127 * "value".hashCode() ) ^ value.hashCode();
  }

  public boolean equals( Object o )
  {
    if ( !( o instanceof JMSConnectionFactory ) )
    {
      return false;
    }

    JMSConnectionFactory other = (JMSConnectionFactory) o;
    return value.equals( other.value() );
  }

  public String toString()
  {
    return "@" + JMSConnectionFactory.class.getName() + "(value=" + value + ")";
  }

  public Class<? extends Annotation> annotationType()
  {
    return JMSConnectionFactory.class;
  }

  private static final long serialVersionUID = 0;
}
