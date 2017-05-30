package org.realityforge.guiceyloops.server;

import org.realityforge.guiceyloops.shared.ValueUtil;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ValueUtilTest
{
  @Test
  public void setSeedAllowsStableIDGenerationg()
  {
    ValueUtil.setSeed( 13 );
    ValueUtil.reset();

    final String s1 = ValueUtil.randomString();

    ValueUtil.setSeed( 13 );
    ValueUtil.reset();

    final String s2 = ValueUtil.randomString();

    assertEquals( s1, s2 );
  }
}
