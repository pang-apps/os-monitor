package com.pangdata.apps.monitor.example.centos7;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.pangdata.apps.monitor.OsMonitor;
import com.pangdata.apps.monitor.OsMonitorTestUtils;
import com.pangdata.apps.monitor.parser.CommonResultParser;
import com.pangdata.apps.monitor.util.OsMonitorUtils;

public class VmstatTests {
  public final String TEST_DATA = 
      "procs -----------memory---------- ---swap-- -----io---- -system-- ------cpu-----\n"+
 "r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa st\n"+
 "0  0 656652 138072     24 984352    0    0     3     9    2    1  1  1 99  0  0\n";
  
  private static final String PROPERTIES = ""
      + "cmd.vmstat = vmstat\n"
      + "vmstat.offset.from = 2\n"
      + "vmstat.value.3.valueCol = 3\n"
      + "vmstat.value.3.devicename = mem-free\n"
      + "vmstat.value.5.valueCol = 5\n"
      + "vmstat.value.5.devicename = mem-cache\n"
      + "vmstat.value.12.valueCol = 12\n"
      + "vmstat.value.12.devicename = cpu-us\n"
      + "vmstat.value.13.valueCol = 13\n"
      + "vmstat.value.13.devicename = cpu-sy\n"
      + "";

  
  @Test
  public void topParsingTest() throws Exception {
    Properties properties = new Properties();
    OsMonitorTestUtils.initProperties(properties, PROPERTIES);
    Map<String, Object> configMap = OsMonitorUtils.propertiesToMap(properties);
    CommonResultParser excutor = new CommonResultParser((Map) configMap.get("vmstat"));
    Map sendPartitionData = excutor.parse(TEST_DATA);
    System.out.println(sendPartitionData);
  }
}
