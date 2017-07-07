package com.pangdata.apps.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

public class CommonTests {

  private void registerDevices(Object data) {
    if(data instanceof Map) {
      Map<String, Object> map = (Map<String, Object>)data; 
      
      Iterator<Entry<String, Object>> devices = map.entrySet().iterator();
      while(devices.hasNext()) {
        Entry<String, Object> next = devices.next();
        Object value = next.getValue();
        if(value instanceof Map) {
          value = ((Map)value).get("value");
        }
        registerDevices(next.getKey(), value);
      }
    } else {
      List<Map<String, Object>> list = (List<Map<String, Object>>)data;
      for(Map<String, Object> map : list) {
        Iterator<Entry<String, Object>> devices = map.entrySet().iterator();
        while(devices.hasNext()) {
          Entry<String, Object> next = devices.next();
          registerDevices(next.getKey(), next.getValue());
        }
      }
    }
  }

  private void registerDevices(String key, Object value) {
    System.out.println(key + " - " + value.toString());
  }
  
  @Test
  public void mapTest() {
    Map<String, Object> data = new HashMap<String, Object>();
    
    Map<String, Object> devicenameMap = new HashMap<String, Object>();
    devicenameMap.put("value", "123");
    devicenameMap.put("timestamp", 1469514903000l);
    data.put("devicename", devicenameMap);
     
    Map<String, Object> devicename2Map = new HashMap<String, Object>();
    devicename2Map.put("value", "345");
    devicename2Map.put("timestamp", 1469514903000l);
    data.put("devicename2", devicename2Map);

    registerDevices(data);
  }
  
  @Test
  public void map2Test() {
    Map<String, Object> data = new HashMap<String, Object>();
    
    data.put("devicename", 1);
    data.put("devicename2", 2);
    registerDevices(data);
  }
  
  @Test
  public void listTest() {
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    
    Map<String, Object> data1 = new HashMap<String, Object>();
    data1.put("devicename", 1);
    data1.put("devicename2", 2);
    data1.put("_timestamp", 1469514903000l);
    dataList.add(data1);
     
    Map<String, Object> data2 = new HashMap<String, Object>();
    data2.put("devicename", 2);
    data2.put("devicename2", 3);
    data2.put("_timestamp", 1469514913000l);
    dataList.add(data1);
     
    Map<String, Object> data3 = new HashMap<String, Object>();
    data3.put("devicename", 3);
    data3.put("devicename2", 4);
    data3.put("_timestamp", 1469514923000l);
    dataList.add(data1);
    registerDevices(dataList);
  }
}
