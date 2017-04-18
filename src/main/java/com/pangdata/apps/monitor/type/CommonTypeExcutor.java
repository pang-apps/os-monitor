package com.pangdata.apps.monitor.type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pangdata.apps.monitor.ConfigConstants;

public class CommonTypeExcutor extends TypeExcutor {
  private final static Logger logger = LoggerFactory.getLogger(CommonTypeExcutor.class);

  public Map<String, Object> excute(Map config, String result) {
    Map<String, Object> resultMap = new HashMap<String, Object>();
    
    Set<String> keys = getList(config, ConfigConstants.key);
    Set<String> excludes = getList(config, ConfigConstants.exclude);
    
    Map offsetMap = (Map) config.get(ConfigConstants.offset);
    int offsetFrom = 0;
    int offsetTo = 0;
    if(offsetMap != null) {
      if(offsetMap.get(ConfigConstants.from) != null) {
        offsetFrom = Integer.valueOf((String)offsetMap.get(ConfigConstants.from));
      }
      if(offsetMap.get(ConfigConstants.to) != null) {
        offsetFrom = Integer.valueOf((String)offsetMap.get(ConfigConstants.to));
      }
    }
    
    String[] lines = result.split("\n");
    
    
    String maxColString = (String) config.get(ConfigConstants.maxCol);
    int maxCol = 0;
    if(maxColString!=null && !maxColString.isEmpty()) {
      maxCol = Integer.valueOf(maxColString);
      logger.debug("Max column : {}", maxCol);
    }
    
    Map<String, Object> valuesMap = (Map<String, Object>) config.get(ConfigConstants.value);
    for(String k:valuesMap.keySet()) {
      Map<String, Object> valueMap = (Map<String, Object>) valuesMap.get(k);
      logger.debug("value map : {}", valueMap);
      int keyColumn = 0;
      if(valueMap.get(ConfigConstants.keyCol) != null) {
        keyColumn = Integer.valueOf((String)valueMap.get(ConfigConstants.keyCol));
      }
      int valueColumn = Integer.valueOf((String)valueMap.get(ConfigConstants.valueCol));
      String devicename = (String)valueMap.get(ConfigConstants.devicename);
      Boolean keyStrict = Boolean.valueOf((String)valueMap.get(ConfigConstants.keystrict));
      
      for(int i=offsetFrom; i<(offsetTo==0?lines.length:offsetTo); i++) {
        logger.trace("lines[{}]:{}", i, lines[i]);
        String[] split2;
        if(maxCol==0) {
          split2 = lines[i].trim().split("\\s+");
        } else {
          split2 = lines[i].trim().split("\\s+", maxCol);
        }
        if(!(split2.length > keyColumn && split2.length > valueColumn)) {
          continue;
        }
        
        if(keys == null) {
          if(excludes != null) {
            if(keyStrict) {
              if(excludes.contains(split2[keyColumn])) {
                continue;
              }
            } else {
              boolean skip = false;
              for(String exclude:excludes) {
                if(split2[keyColumn].indexOf(exclude) >= 0) {
                  skip = true;
                  break;
                }
              }
              if(skip) {
                continue;
              }
            }
          }
          resultMap.put(getDevicename(devicename, split2[keyColumn]), split2[valueColumn]);
        } else {
          if(keyStrict) {
            if(keys.contains(split2[keyColumn])) {
              logger.debug(lines[i]);
              resultMap.put(getDevicename(devicename, split2[keyColumn]), split2[valueColumn]);
            }
          } else {
            for(String key:keys) {
              if(split2[keyColumn].indexOf(key) >= 0) {
                logger.debug(lines[i]);
                resultMap.put(getDevicename(devicename, key), split2[valueColumn]);
                break;
              }
            }
          }
        }
      }
    }

    logger.debug("result : {}", resultMap);
    return resultMap;
  }

  private Set<String> getList(Map config, String keyName) {
    Object keyObject = config.get(keyName);
    Set<String> partitions = new HashSet<String>();
    if(keyObject == null) {
      return null;
    } else if(keyObject instanceof Map) {
      Map<String, Object> keyMap = (Map<String, Object>) keyObject;
      Set<String> keySet = keyMap.keySet();
      for(String key : keySet) {
        partitions.add((String) keyMap.get(key));
      }
    } else if(keyObject instanceof String){
      String[] split = ((String) keyObject).split(", ");
      for(String k:split) {
        partitions.add(k);
      }
    } else {
      return null;
    }
    return partitions;
  }

  private String getDevicename(String devicename, String...args) {
    for(String arg:args) {
      devicename = devicename.replace("{}", arg);
    }
    devicename = replaceSpecialCharacter(devicename);
    devicename = replaceSpaceCharacter(devicename);
    
    return devicename;
  }

  public static String replaceSpaceCharacter(String devicename) {
    return devicename.replaceAll("[\\s+]", "");
  }

  public static String replaceSpecialCharacter(String devicename) {
    return devicename.replaceAll("[^a-zA-Z0-9_-]", "-");
  }

}