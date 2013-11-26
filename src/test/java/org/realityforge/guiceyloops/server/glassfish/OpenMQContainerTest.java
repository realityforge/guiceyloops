package org.realityforge.guiceyloops.server.glassfish;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class OpenMQContainerTest
{
  @Test
  public void basicWorkflow()
    throws Exception
  {
    final OpenMQContainer container = new OpenMQContainer();

    container.start();

    final Connection connection = container.createConnection();
    assertNotNull( connection );

    // now create a session and a producer and consumer in the normal way
    final Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
    final Queue queue = session.createQueue( "exampleQueue" );
    final MessageConsumer consumer = session.createConsumer( queue );
    final MessageProducer producer = session.createProducer( queue );

    // send a message to the queue in the normal way
    final String txMessage = "This is a message";
    producer.send( session.createTextMessage( txMessage ) );

    // receive a message from the queue in the normal way
    connection.start();

    final String rxMessage = ( (TextMessage) consumer.receive( 10 ) ).getText();

    assertEquals( rxMessage, txMessage );

    connection.close();
    container.stop();
  }
}
