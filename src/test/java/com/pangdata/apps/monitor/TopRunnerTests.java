package com.pangdata.apps.monitor;

import java.io.IOException;

import org.junit.Test;

public class TopRunnerTests {

  @Test
  public void top() throws Exception {
    OsMonitor osMonitor = new OsMonitor();
    osMonitor.main(new String[]{});
  }
}
