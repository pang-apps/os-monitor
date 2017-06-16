package com.pangdata.apps.monitor.example.centos7;

import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.pangdata.apps.monitor.OsMonitorTestUtils;
import com.pangdata.apps.monitor.parser.CommonResultParser;
import com.pangdata.apps.monitor.util.OsMonitorUtils;

public class PartitionSizeMonitorTests {
  
  private static final String TEST_DATA = "Filesystem     1G-blocks  Used Available Use% Mounted on\n" +
"/dev/xvda1            20     5        15  24% /\n" +
"devtmpfs               4     0         4   0% /dev\n" +
"tmpfs                  4     0         4   0% /dev/shm\n" +
"tmpfs                  4     1         4  11% /run\n" +
"tmpfs                  4     0         4   0% /sys/fs/cgroup\n" +
"/dev/xvdb1           985     8       927   1% /data\n" +
"tmpfs                  1     0         1   0% /run/user/1003\n" +
"tmpfs                  1     0         1   0% /run/user/1007";
  
  private static final String PROPERTIES = ""
      + "cmd.df = df --block-size=1M\n"
      + "df.period = 3600\n"
      + "df.key.1 = xvda1\n"
      + "df.key.2 = xvdb1\n"
      + "df.value.1.keyCol = 0\n"
      + "df.value.1.valueCol = 2\n"
      + "df.value.1.devicename = disk-used-{}\n"
      + "df.value.2.keyCol = 0\n"
      + "df.value.2.valueCol = 3\n"
      + "df.value.2.devicename = disk-available-{}\n"
      + "";
  
  @Test
  public void partitionSizeParsingTest() throws Exception {
    Properties properties = new Properties();
    OsMonitorTestUtils.initProperties(properties, PROPERTIES);
    Map<String, Object> configMap = OsMonitorUtils.propertiesToMap(properties);
    CommonResultParser excutor = new CommonResultParser((Map) configMap.get("df"));
    Map sendPartitionData = excutor.parse(TEST_DATA);
    System.out.println(sendPartitionData);
  }

}
