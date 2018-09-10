package org.realityforge.guiceyloops.server.glassfish;

import com.sun.messaging.jms.management.server.DestinationAttributes;
import com.sun.messaging.jms.management.server.DestinationOperations;
import com.sun.messaging.jms.management.server.DestinationType;
import com.sun.messaging.jms.management.server.MQObjectName;
import java.lang.management.ManagementFactory;
import javax.annotation.Nonnull;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Utility methods for interacting with the OpenMQ server during testing.
 */
public final class OpenMQUtil
{
  private OpenMQUtil()
  {
  }

  /**
   * Method that purges contents of queues.
   * Typically this is invoked at the start of tests to reset state
   */
  public static void purgeQueue( @Nonnull final String destinationName )
    throws Exception
  {
    final ObjectName objectName =
      MQObjectName.createDestinationConfig( DestinationType.QUEUE, destinationName );
    final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    server.invoke( objectName, DestinationOperations.PURGE, new Object[ 0 ], new String[ 0 ] );
  }

  /**
   * Method that purges contents of topics.
   * Typically this is invoked at the start of tests to reset state
   */
  public static void purgeTopic( @Nonnull final String destinationName )
    throws Exception
  {
    final ObjectName objectName =
      MQObjectName.createDestinationConfig( DestinationType.TOPIC, destinationName );
    final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    server.invoke( objectName, DestinationOperations.PURGE, new Object[ 0 ], new String[ 0 ] );
  }

  /**
   * Get an attribute from the jmx bean that monitors specified topic.
   */
  public static <T> T getTopicMonitorAttribute( @Nonnull final String destinationName,
                                                @Nonnull final String attributeName )
    throws Exception
  {

    return getDestinationMonitorAttribute( destinationName, attributeName, false );
  }

  /**
   * Get an attribute from the jmx bean that monitors specified queue.
   */
  public static <T> T getQueueMonitorAttribute( @Nonnull final String destinationName,
                                                @Nonnull final String attributeName )
    throws Exception
  {

    return getDestinationMonitorAttribute( destinationName, attributeName, true );
  }

  /**
   * Retrieve an attribute value from the destination monitoring jmx bean.
   */
  @SuppressWarnings( "unchecked" )
  public static <T> T getDestinationMonitorAttribute( @Nonnull final String destinationName,
                                                      @Nonnull final String attributeName,
                                                      final boolean queue )
    throws Exception
  {
    final ObjectName objectName =
      MQObjectName.createDestinationMonitor( queue ? DestinationType.QUEUE : DestinationType.TOPIC, destinationName );
    final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    return (T) server.getAttribute( objectName, attributeName );
  }

  /**
   * Get the 'NumMsgs' attribute for specified queue.
   */
  public static long getQueueNumMsgs( @Nonnull final String destinationName )
    throws Exception
  {
    return getQueueMonitorAttribute( destinationName, DestinationAttributes.NUM_MSGS );
  }

  /**
   * Get the 'NumMsgsIn' attribute for specified queue.
   */
  public static long getQueueNumMsgsIn( @Nonnull final String destinationName )
    throws Exception
  {
    return getQueueMonitorAttribute( destinationName, DestinationAttributes.NUM_MSGS_IN );
  }

  /**
   * Get the 'NumMsgsOut' attribute for specified queue.
   */
  public static long getQueueNumMsgsOut( @Nonnull final String destinationName )
    throws Exception
  {
    return getQueueMonitorAttribute( destinationName, DestinationAttributes.NUM_MSGS_OUT );
  }
}
