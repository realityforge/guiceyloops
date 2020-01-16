package org.realityforge.guiceyloops.shared;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.DatatypeConverter;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ValueUtilTest
{
  @Test
  public void resetWillResetSeeds()
  {
    ValueUtil.setSeed( 23 );
    ValueUtil.reset();
    final boolean originalBoolean = ValueUtil.randomBoolean();
    final int originalInt = ValueUtil.randomInt();
    final long originalLong = ValueUtil.randomLong();
    final float originalFloat = ValueUtil.randomFloat();
    final double originalDouble = ValueUtil.randomDouble();
    final int originalID = ValueUtil.nextID();

    ValueUtil.reset();

    final boolean nextBoolean = ValueUtil.randomBoolean();
    final int nextInt = ValueUtil.randomInt();
    final long nextLong = ValueUtil.randomLong();
    final float nextFloat = ValueUtil.randomFloat();
    final double nextDouble = ValueUtil.randomDouble();
    final int nextID = ValueUtil.nextID();

    assertEquals( nextBoolean, originalBoolean );
    assertEquals( nextInt, originalInt );
    assertEquals( nextLong, originalLong );
    assertEquals( nextFloat, originalFloat );
    assertEquals( nextDouble, originalDouble );
    assertEquals( nextID, originalID );
  }

  @Test
  public void trunc()
  {
    assertEquals( new Date( 12000 ), ValueUtil.trunc( new Date( 12345 ) ) );
  }

  @Test
  public void now()
  {
    final long time = ValueUtil.now().getTime();
    assertEquals( ( time / 1000L ) * 1000, time );
    assertTrue( Math.abs( System.currentTimeMillis() - time ) < 1500 );
  }

  @Test
  public void randomString()
  {
    final String v1 = ValueUtil.randomString();
    final String v2 = ValueUtil.randomString();
    assertNotEquals( v1, v2 );

    assertEquals( ValueUtil.randomString( 3 ).length(), 3 );

    final String prefix = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRS";
    final String v3 = ValueUtil.randomString( prefix );
    assertEquals( v3.length(), 50 );
    assertTrue( v3.startsWith( prefix ) );
  }

  @Test
  public void randomEmail()
  {
    final String v1 = ValueUtil.randomEmail();
    final String v2 = ValueUtil.randomEmail();
    assertNotEquals( v1, v2 );
    assertTrue( v1.endsWith( "@example.com" ) );
    assertTrue( v2.endsWith( "@example.com" ) );

    final String domain = "stocksoftware.com.au";
    final String v3 = ValueUtil.randomEmail( domain );
    final String v4 = ValueUtil.randomEmail( domain );
    assertNotEquals( v3, v4 );
    assertTrue( v3.endsWith( "@stocksoftware.com.au" ) );
    assertTrue( v4.endsWith( "@stocksoftware.com.au" ) );
  }

  @Test
  public void dates()
  {
    assertDate( ValueUtil.createDate( 2015, 1, 1 ), "2015-01-01T00:00:00+11:00" );
    assertDate( ValueUtil.createDate( 2015, 1, 1, 13, 20, 10 ), "2015-01-01T13:20:10+11:00" );

    assertDate( ValueUtil.addDays( ValueUtil.createDate( 2015, 1, 1 ), 1 ), "2015-01-02T00:00:00+11:00" );
    assertDate( ValueUtil.addDays( ValueUtil.createDate( 2015, 1, 4 ), -2 ), "2015-01-02T00:00:00+11:00" );

    assertDate( ValueUtil.addHours( ValueUtil.createDate( 2015, 1, 1 ), 3 ), "2015-01-01T03:00:00+11:00" );
    assertDate( ValueUtil.addHours( ValueUtil.createDate( 2015, 1, 1 ), 24 ), "2015-01-02T00:00:00+11:00" );

    assertDate( ValueUtil.addMinutes( ValueUtil.createDate( 2015, 1, 1 ), 3 ), "2015-01-01T00:03:00+11:00" );
    assertDate( ValueUtil.addMinutes( ValueUtil.createDate( 2015, 1, 1 ), 60 ), "2015-01-01T01:00:00+11:00" );

    final Date today = ValueUtil.today();
    final Date tomorrow = ValueUtil.tomorrow();
    final Date yesterday = ValueUtil.yesterday();

    final GregorianCalendar c = new GregorianCalendar( TimeZone.getTimeZone( "Australia/Melbourne" ) );
    c.setTime( today );
    assertEquals( c.get( Calendar.HOUR_OF_DAY ), 0 );
    assertEquals( c.get( Calendar.MINUTE ), 0 );
    assertEquals( c.get( Calendar.SECOND ), 0 );
    assertEquals( c.get( Calendar.MILLISECOND ), 0 );

    final long daysInMillis = TimeUnit.DAYS.toMillis( 1 );
    assertTrue( yesterday.getTime() + daysInMillis - today.getTime() < 1500 );
    assertTrue( tomorrow.getTime() - daysInMillis - today.getTime() < 1500 );
  }

  private void assertDate( final Date date, final String expected )
  {
    assertEquals( asString( date ), expected, "Date: " + date );
  }

  private String asString( final Date date )
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone( TimeZone.getTimeZone( "Australia/Melbourne" ) );
    calendar.setTimeInMillis( date.getTime() );
    return DatatypeConverter.printDateTime( calendar );
  }
}
