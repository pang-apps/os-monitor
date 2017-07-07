package com.pangdata.apps.monitor;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class OsMonitorTests {
  
  @Test
  public void startMainTest() throws Exception {
    OsMonitor osMonitor = new OsMonitor();
    osMonitor.main(new String[]{});
    TimeUnit.SECONDS.sleep(10000);
  }
}
