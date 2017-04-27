package com.pangdata.apps.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pangdata.apps.monitor.type.TypeExcutor;
import com.pangdata.apps.monitor.type.TypeExcutorFactory;
import com.pangdata.sdk.Pang;
import com.pangdata.sdk.mqtt.PangMqtt;
import com.pangdata.sdk.util.PangProperties;


public class OsMonitor {

  private static final Logger logger = LoggerFactory.getLogger(OsMonitor.class);

  private static TypeExcutorFactory typeExcutorFactory = new TypeExcutorFactory();
  
  private static AtomicBoolean running = new AtomicBoolean();

  public static void main(String[] args) throws Exception {
    // Init Pang client
    final Pang pang = new PangMqtt();
    Properties properties = PangProperties.getProperties();

    // Check OS
    final PangOsType os = OsMonitorUtils.getOsType();
    logger.debug("Os is {}.", os);

    // Get terminal charset
    final String charset = OsMonitorUtils.getCharset(os);
    logger.debug("Charset is {}.", charset);

    final String prefix = properties.getProperty(ConfigConstants.PANG_PREFIX);
    logger.debug("Prefix is {}.", prefix);

    String defaultPeriodString = properties.getProperty(ConfigConstants.PANG_PERIOD);
    final int defaultPeriod = getDefaultPeriod(defaultPeriodString);

    String commandFilename = properties.getProperty(ConfigConstants.PANG_CONF);
    // Get commands & options
    Properties commandProperties = OsMonitorUtils.getProperties(commandFilename);
    final Map<String, Object> keyMap = OsMonitorUtils.propertiesToMap(commandProperties);
    final Map<String, String> cmdMap =
        (Map<String, String>) keyMap.get(ConfigConstants.COMMAND_PREFIX);
    
    Set<String> cmdKeys = cmdMap.keySet();
    
    for(String key:cmdMap.keySet()) {
      Map configMap = (Map)keyMap.get(key);
      if(configMap == null) {
        configMap = new HashMap<String, Object>();
      }
      String command = cmdMap.get(key);
      excuteCommandTimer(pang, configMap, command , os, charset, prefix, defaultPeriod, key);
    }

  }

  private static int getDefaultPeriod(String defaultPeriodString) {
    int defaultPeriod = 0;
    if (defaultPeriodString != null && !defaultPeriodString.isEmpty()) {
      defaultPeriod = Integer.valueOf(defaultPeriodString);
      logger.debug("Default period is {} seconds.", defaultPeriod);
    }
    return defaultPeriod;
  }

  private static void excuteCommandTimer(final Pang pang, final Map configMap, String command,
      PangOsType os, String charset, final String prefix, int defaultPeriod, String key) {

    Timer timer = new Timer("Timer-cmd-"+key);
    String periodString = (String) configMap.get(ConfigConstants.period);
    int period;
    if (periodString != null && !periodString.isEmpty()) {
      period = Integer.valueOf(periodString);
    } else {
      period = defaultPeriod;
    }
    if (period <= 0) {
      throw new IllegalStateException("Period is not set. key:" + key);
    }


    CommandCallback commandCallback = new CommandCallback() {

      public void call(String result) {
        TypeExcutor typeExcutor =
            typeExcutorFactory.getTypeExcutor((String) configMap.get(ConfigConstants.TYPE));
        Map<String, Object> data = typeExcutor.excute(configMap, result);
        if (prefix != null && !prefix.isEmpty()) {
          data = OsMonitorUtils.concatPrefix(data, prefix);
        }
        logger.info("send data : " + data);
        OsMonitorUtils.changeDataToNumber(data);
        pang.sendData(data);
      }

    };
    timer.scheduleAtFixedRate(new CommandRunner(OsMonitorUtils.toCommand(command, os), commandCallback, charset), 1000, period * 1000);
  }

}
