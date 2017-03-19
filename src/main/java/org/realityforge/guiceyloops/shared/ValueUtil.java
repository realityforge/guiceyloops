package org.realityforge.guiceyloops.shared;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A set of utility methods used to create semi-random values used
 * when constructing objects in tests.
 */
public final class ValueUtil
{
  private static final int DEFAULT_MAX_STRING_LENGTH = 50;
  private static final int INITIAL_VALUE = 1;

  private static final AtomicInteger c_currentID = new AtomicInteger( INITIAL_VALUE );
  private static final Random c_random = new Random();
  private static Date c_now;

  private static int c_seed;

  private ValueUtil()
  {
  }

  /**
   * @return a random int.
   */
  public static int randomInt()
  {
    return getRandom().nextInt();
  }

  /**
   * @return a random long.
   */
  public static long randomLong()
  {
    return getRandom().nextLong();
  }

  /**
   * @return a random boolean.
   */
  public static boolean randomBoolean()
  {
    return getRandom().nextBoolean();
  }

  /**
   * @return a random double.
   */
  public static double randomDouble()
  {
    return getRandom().nextDouble();
  }

  /**
   * @return a random float.
   */
  public static float randomFloat()
  {
    return getRandom().nextFloat();
  }

  /**
   * Set value to seed random values. Set to non-zero to specify a particular seed.
   */
  public static void setSeed( final int seed )
  {
    c_seed = seed;
  }

  /**
   * Return the random value generator.
   */
  public static Random getRandom()
  {
    return c_random;
  }

  public static void reset()
  {
    c_now = null;
    c_currentID.set( INITIAL_VALUE );
    if ( 0 != c_seed )
    {
      c_random.setSeed( c_seed );
    }
  }

  /**
   * Retrun a monotonically increasing integer. Only decreases when reset() is invoked.
   */
  public static int nextID()
  {
    return c_currentID.getAndIncrement();
  }

  /**
   * Helper function to set "now" time.
   */
  public static void setNow( final Date now )
  {
    c_now = now;
  }

  /**
   * Current time truncated to database resolution.
   */
  @Nonnull
  public static Date now()
  {
    return trunc( null != c_now ? c_now : new Date() );
  }

  /**
   * Current time as a calendar.
   */
  @Nonnull
  public static Calendar nowAsCalendar()
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone( TimeZone.getTimeZone( "Australia/Melbourne" ) );
    calendar.setTime( now() );
    return calendar;
  }

  /**
   * Truncate specified time to database resolution.
   */
  @Nonnull
  public static Date trunc( @Nonnull final Date date )
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone( TimeZone.getTimeZone( "Australia/Melbourne" ) );
    calendar.setTime( date );
    calendar.set( Calendar.MILLISECOND, 0 );
    return calendar.getTime();
  }

  /**
   * Create a date for specified year month day.
   */
  @Nonnull
  public static Date createDate( final int year, final int month, final int day )
  {
    return createDate( year, month, day, 0, 0, 0 );
  }

  @Nonnull
  public static Date createDate( final int year,
                                 final int month,
                                 final int day,
                                 final int hour,
                                 final int minute,
                                 final int second )
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone( TimeZone.getTimeZone( "Australia/Melbourne" ) );
    calendar.set( Calendar.YEAR, year );
    calendar.set( Calendar.MONTH, month - 1 );
    calendar.set( Calendar.DAY_OF_MONTH, day );
    calendar.set( Calendar.HOUR_OF_DAY, hour );
    calendar.set( Calendar.MINUTE, minute );
    calendar.set( Calendar.SECOND, second );
    calendar.set( Calendar.MILLISECOND, 0 );
    return calendar.getTime();
  }

  @Nonnull
  public static Date today()
  {
    final Calendar calendar = nowAsCalendar();
    calendar.set( Calendar.HOUR_OF_DAY, 0 );
    calendar.set( Calendar.MINUTE, 0 );
    calendar.set( Calendar.SECOND, 0 );
    calendar.set( Calendar.MILLISECOND, 0 );
    return calendar.getTime();
  }

  @Nonnull
  public static Date tomorrow()
  {
    return addDays( today(), 1 );
  }

  @Nonnull
  public static Date yesterday()
  {
    return addDays( today(), -1 );
  }

  @Nonnull
  public static Date addDays( @Nonnull final Date time, final int dayCount )
  {
    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone( TimeZone.getTimeZone( "Australia/Melbourne" ) );
    cal.setTime( time );
    cal.add( Calendar.DAY_OF_YEAR, dayCount );
    return cal.getTime();
  }

  @Nonnull
  public static Date addHours( @Nonnull final Date time, final int count )
  {
    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone( TimeZone.getTimeZone( "Australia/Melbourne" ) );
    cal.setTime( time );
    cal.add( Calendar.HOUR, count );
    return cal.getTime();
  }

  @Nonnull
  public static Date addMinutes( @Nonnull final Date time, final int count )
  {
    final Calendar cal = Calendar.getInstance();
    cal.setTimeZone( TimeZone.getTimeZone( "Australia/Melbourne" ) );
    cal.setTime( time );
    cal.add( Calendar.MINUTE, count );
    return cal.getTime();
  }

  @Nonnull
  public static String randomEmail()
  {
    return randomEmail( "example.com" );
  }

  @Nonnull
  public static String randomEmail( @Nonnull final String domain )
  {
    return concatString( randomString( DEFAULT_MAX_STRING_LENGTH / 4 ), "@" + domain );
  }

  @Nonnull
  public static String randomString()
  {
    return randomString( DEFAULT_MAX_STRING_LENGTH );
  }

  @Nonnull
  public static String randomString( final int maxStringLength )
  {
    return limit( UUID.randomUUID().toString(), maxStringLength );
  }

  @Nonnull
  public static String randomString( @Nullable final String prefix )
  {
    return concatString( prefix, randomString() );
  }

  @Nonnull
  public static String concatString( @Nullable final String prefix, @Nonnull final String suffix )
  {
    return concatString( prefix, suffix, DEFAULT_MAX_STRING_LENGTH );
  }

  @Nonnull
  private static String concatString( @Nullable final String prefix,
                                      @Nonnull final String suffix,
                                      final int maxStringLength )
  {
    return limit( null == prefix ? suffix : prefix + suffix, maxStringLength );
  }

  @Nonnull
  private static String limit( @Nonnull final String string, final int maxStringLength )
  {
    return string.substring( 0, Math.min( maxStringLength, string.length() ) );
  }
}
