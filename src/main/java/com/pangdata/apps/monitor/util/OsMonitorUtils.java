package com.pangdata.apps.monitor.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pangdata.apps.monitor.runner.CommandRunner;
import com.pangdata.apps.monitor.util.OsCheck.OSType;
import com.pangdata.sdk.util.NumberUtils;
import com.pangdata.sdk.util.PangProperties;
import com.pangdata.sdk.util.SdkUtils;

public class OsMonitorUtils {
  private static final String CHCP = "chcp";
  private static final String ECHO_LANG = "locale";
  private static final Logger logger = LoggerFactory.getLogger(OsMonitorUtils.class);
  private static final String LINUX_COMMAND_PREFIX = "/bin/sh -c ";
  private static final String WINDOWS_COMMAND_PREFIX = "cmd /c ";
  private static Properties props;
  
  public static String[] toCommand(String command, OsCheck.OSType os) {
    
    if(os == OSType.Linux) {
      return new String[]{"/bin/sh", "-c", command};
    } else if(os == OSType.Windows) {
      return new String[]{"cmd", "/c", command};
    } else {
      throw new IllegalArgumentException("Os is not supported. " + os);
    }
  }

  public static String getCharset() {
    Charset encoding = Charset.defaultCharset();
    OSType ost = OsCheck.getOperatingSystemType();
    if(ost == OsCheck.OSType.Linux) {
      String lang = new CommandRunner(toCommand(ECHO_LANG, ost)).excute();
      String[] lines = lang.split(System.lineSeparator());
      Map<String, String> localeMap = new HashMap<String, String>();
      for(String line:lines) {
        String[] split = line.split("=");
        if(split.length == 2) {
          localeMap.put(split[0], split[1].trim());
        }
      }
      String charset = localeMap.get("LANG");
      String[] split = charset.split("\\.");
      if(split.length == 2) {
        return split[1];
      } else {
        return split[0];
      }
    } else if(ost == OsCheck.OSType.Windows) {
      String chcpResult = new CommandRunner(toCommand(CHCP, ost), "MS949").excute();
      logger.trace("chcp command result : {}", chcpResult);
      String[] split = chcpResult.split(" ");
      String charset = split[split.length-1].trim();
      logger.trace("Parsed charset : {}", charset);

      if(isNumber(charset)) {
        charset = "MS"+charset;
      }
      return charset;
    }
    
    throw new IllegalArgumentException("Os is not supported. " + ost);
  }
  
  public static boolean isNumber(String number) {
    try {
      Double.parseDouble(number);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }
  
  public static synchronized Properties getProperties(String filename) throws IOException {
    if(props != null) {
      return props;
    }

    InputStream is = SdkUtils.class.getResourceAsStream("/"+filename);
    if(is == null) {
      String msg = "Could not load the file " + filename + " in your classpath";
      logger.error(msg);
      throw new IOException(msg);
    }
    Properties props = new Properties();
    props.load(is);
    is.close();
    
    return props;
  }

  public static Map<String, Object> propertiesToMap(Properties properties) {
    Map<String, Object> commandMap = new HashMap<String, Object>();
    
    Set<Object> keySet = properties.keySet();
    for(Object keyObject : properties.keySet()) {
      String[] split = ((String) keyObject).split("\\.");
      Map<String, Object> cursor = commandMap;
      
      for(int i=0; i<split.length; i++) {
        //last index
        if(i == split.length-1) {
          Object object = properties.get(keyObject);
          cursor.put(split[i], object);
          break;
        }
        
        Map<String, Object> object = (Map<String, Object>) cursor.get(split[i]);
        if(object == null) {
          HashMap<String, Object> newMap = new HashMap<String, Object>();
          cursor.put(split[i], newMap);
          cursor = newMap;
        } else {
          cursor = object;
        }
      }
    }
    logger.debug("Config map : {}", commandMap);
    
    return commandMap;
  }

  public static void changeDataToNumber(Map<String, Object> data) {
    for(String key:data.keySet()) {
      Object value = data.get(key);
      if(value==null) {
        continue;
      } else if(value.getClass() != String.class) {
        continue;
      }
      
      Object changedValue = NumberUtils.toObject((String) value);
      data.put(key, changedValue);
    }
  }
}
