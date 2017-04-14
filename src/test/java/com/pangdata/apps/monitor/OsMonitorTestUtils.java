package com.pangdata.apps.monitor;

import java.util.Properties;

public class OsMonitorTestUtils {
  
  public static void initProperties(Properties properties, String propertiesString) {
    String[] split = propertiesString.split("\n");
    for(String prop:split) {
      String[] split2 = prop.split("=");
      if(split2[0].startsWith("#") || split2.length!=2 || split2[1] == null) {
        continue;
      }
      properties.setProperty(split2[0].trim(), split2[1].trim());
    }
  }

}
