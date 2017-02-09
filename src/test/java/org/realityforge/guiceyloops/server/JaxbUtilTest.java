package org.realityforge.guiceyloops.server;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class JaxbUtilTest
{
  private static final String OUTPUT_FORMAT =
    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><attribute id=\"22\" value=\"Hihi\"/>";
  private static final AttributeDTO ATTRIBUTE = new AttributeDTO( 22, "Hihi" );

  @XmlRootElement( name = "attribute" )
  @XmlType( name = "AttributeDTO", propOrder = { "id", "value" } )
  public static class AttributeDTO
    implements Serializable
  {
    private static final long serialVersionUID = 1;

    @XmlAttribute( name = "id", required = true )
    private int id;
    @XmlAttribute( name = "value" )
    private String value;

    public AttributeDTO( final int id, final String value )
    {
      this.id = id;
      this.value = value;
    }

    public AttributeDTO()
    {
    }

    public int getId()
    {
      return id;
    }

    public String getValue()
    {
      return value;
    }
  }

  @Test
  public void roundTrip()
    throws Exception
  {
    final AttributeDTO value = JaxbUtil.unmarshall( AttributeDTO.class, JaxbUtil.marshall( ATTRIBUTE ) );
    assertEquals( value.getId(), ATTRIBUTE.getId() );
    assertEquals( value.getValue(), ATTRIBUTE.getValue() );
  }

  @Test
  public void unmarshall()
    throws Exception
  {
    final AttributeDTO value = JaxbUtil.unmarshall( AttributeDTO.class, OUTPUT_FORMAT );
    assertEquals( value.getId(), ATTRIBUTE.getId() );
    assertEquals( value.getValue(), ATTRIBUTE.getValue() );
  }

  @Test
  public void marshall()
    throws Exception
  {
    assertEquals( JaxbUtil.marshall( ATTRIBUTE ), OUTPUT_FORMAT );
  }
}
