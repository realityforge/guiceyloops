package org.realityforge.guiceyloops.server;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public final class JaxbUtil
{
  private JaxbUtil()
  {
  }

  @SuppressWarnings( "unchecked" )
  public static String marshall( @Nonnull final Object instance, @Nonnull final Class... additionalTypes )
    throws Exception
  {
    return marshall( instance.getClass(), instance, additionalTypes );
  }

  @SuppressWarnings( "unchecked" )
  public static String marshall( @Nonnull final Class type,
                                 @Nonnull final Object instance,
                                 @Nonnull final Class... additionalTypes )
    throws Exception
  {
    final Marshaller marshaller = toJaxbContext( type, additionalTypes ).createMarshaller();
    final StringWriter writer = new StringWriter();
    marshaller.marshal( instance, writer );
    return writer.toString();
  }

  @SuppressWarnings( "unchecked" )
  public static <T> T unmarshall( @Nonnull final Class<T> type,
                                  @Nonnull final String text,
                                  @Nonnull final Class... additionalTypes )
    throws Exception
  {
    final Unmarshaller marshaller = toJaxbContext( type, additionalTypes ).createUnmarshaller();
    return (T) marshaller.unmarshal( new StringReader( text ) );
  }

  private static <T> JAXBContext toJaxbContext( final Class<T> type, final Class[] additionalTypes )
    throws JAXBException
  {
    return JAXBContext.newInstance( toTypes( type, additionalTypes ) );
  }

  private static Class[] toTypes( final Class type, final Class[] additionalTypes )
  {
    final ArrayList<Class> types = new ArrayList<>();
    types.add( type );
    types.addAll( Arrays.asList( additionalTypes ) );

    return types.toArray( new Class[ 0 ] );
  }
}
