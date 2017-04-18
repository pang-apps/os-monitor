package com.pangdata.apps.monitor;

import org.junit.Assert;
import org.junit.Test;

public class OsMonitorUtilsTests {
  
  private static final String thisCharset = "MS949";
  PangOsType thisOsType = PangOsType.Windows;
  
  @Test
  public void getOsTypeTest() {
    PangOsType osType = OsMonitorUtils.getOsType();
    
    Assert.assertEquals(thisOsType, osType);
  }
  
  @Test
  public void getCharsetTest() {
    PangOsType osType = OsMonitorUtils.getOsType();
    String charset = OsMonitorUtils.getCharset(osType);
    
    Assert.assertEquals(thisCharset, charset);
  }

}
