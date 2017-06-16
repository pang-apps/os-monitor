package com.pangdata.apps.monitor;

import org.junit.Assert;
import org.junit.Test;

import com.pangdata.apps.monitor.util.OsCheck;
import com.pangdata.apps.monitor.util.OsMonitorUtils;
import com.pangdata.apps.monitor.util.OsCheck.OSType;

public class OsMonitorUtilsTests {
  
  private static final String thisCharset = "MS949";
  OSType thisOsType = OSType.Windows;
  
  @Test
  public void getOsTypeTest() {
    OSType osType = OsCheck.getOperatingSystemType();
    
    Assert.assertEquals(thisOsType, osType);
  }
  
  @Test
  public void getCharsetTest() {
    String charset = OsMonitorUtils.getCharset();
    
    Assert.assertEquals(thisCharset, charset);
  }

}
