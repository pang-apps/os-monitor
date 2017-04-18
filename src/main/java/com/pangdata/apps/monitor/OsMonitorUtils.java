package com.pangdata.apps.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pangdata.sdk.util.NumberUtils;
import com.pangdata.sdk.util.SdkUtils;

public class OsMonitorUtils {
  private static final String CHCP = "chcp";
  private static final String ECHO_LANG = "locale";
  private static final Logger logger = LoggerFactory.getLogger(OsMonitorUtils.class);
  private static final String LINUX_COMMAND_PREFIX = "/bin/sh -c ";
  private static final String WINDOWS_COMMAND_PREFIX = "cmd /c ";
  private static Properties props;
  
  public static PangOsType getOsType() {
    String osName = System.getProperty("os.name");
    
    if(osName == null || osName.isEmpty()) {
      throw new IllegalStateException("Cannot find os name from system properties.");
    }
    
    if(osName.toLowerCase().contains("windows")) {
      return PangOsType.Windows;
    } else if(osName.toLowerCase().contains("linux")) {
      return PangOsType.Linux;
    }
    
    throw new IllegalStateException("Os name is unclassified." + osName);
  }

  public static String[] toCommand(String command, PangOsType os) {
    
    if(os == PangOsType.Linux) {
      return new String[]{"/bin/sh", "-c", command};
    } else if(os == PangOsType.Windows) {
      return new String[]{"cmd", "/c", command};
    } else {
      throw new IllegalArgumentException("Os is not supported. " + os);
    }
  }

  public static String getCharset(PangOsType os) {
    String osName = System.getProperty("os.name");
    Charset encoding = Charset.defaultCharset();
    if(os == PangOsType.Linux) {
      String lang = new CommandRunner(toCommand(ECHO_LANG, os)).excute();
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
    } else if(os == PangOsType.Windows) {
      String chcpResult = new CommandRunner(toCommand(CHCP, os), "MS949").excute();
      logger.trace("chcp command result : {}", chcpResult);
      String[] split = chcpResult.split(" ");
      String charset = split[split.length-1].trim();
      logger.trace("Parsed charset : {}", charset);

      if(isNumber(charset)) {
        charset = "MS"+charset;
      }
      return charset;
    }
    
    throw new IllegalArgumentException("Os is not supported. " + os);
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
      throw new IOException("Could not load the file " + filename + " in your classpath");
    }
    Properties props = new Properties();
    props.load(is);
    is.close();
    
    return props;
  }

  public static Map<String, Object> concatPrefix(Map<String, Object> data, String prefix) {
    HashMap<String, Object> hashMap = new HashMap<String, Object>();
    for(String key : data.keySet()) {
      hashMap.put(prefix+"_"+key, data.get(key));
    }
    return hashMap;
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
          cursor.put(split[i], properties.get(keyObject));
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
