/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Preversoft
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.pangdata.apps.monitor;

import java.util.Map;
import java.util.Properties;
import java.util.Timer;

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
  
  public static void main(String[] args) throws Exception {
    //Init Pang client
    Pang pang = new PangMqtt();
    Properties properties = PangProperties.getProperties();
    
    //Check OS
    PangOsType os = OsMonitorUtils.getOsType();
    logger.debug("Os is {}.", os);
    
    //Get terminal charset
    String charset = OsMonitorUtils.getCharset(os);
    logger.debug("Charset is {}.", charset);
    
    String prefix = properties.getProperty(ConfigConstants.PANG_PREFIX);
    logger.debug("Prefix is {}.", prefix);
    
    String defaultPeriodString = properties.getProperty(ConfigConstants.PANG_PERIOD);
    int defaultPeriod = 0;
    if(defaultPeriodString !=null && !defaultPeriodString.isEmpty()) {
      defaultPeriod = Integer.valueOf(defaultPeriodString);
      logger.debug("Default period is {} seconds.", defaultPeriod);
    }

    String commandFilename = properties.getProperty(ConfigConstants.PANG_CONF);
    //Get commands & options
    Properties commandProperties = OsMonitorUtils.getProperties(commandFilename);
    Map<String, Object> keyMap = OsMonitorUtils.propertiesToMap(commandProperties);
    Map<String, String> cmdMap = (Map<String, String>) keyMap.get(ConfigConstants.COMMAND_PREFIX);

    for(String key : cmdMap.keySet()) {
      excuteCommandTimer(pang, (Map) keyMap.get(key), cmdMap.get(key), os, charset, prefix, defaultPeriod, key);
    }
  }

  private static void excuteCommandTimer(final Pang pang, final Map configMap, String command, PangOsType os, String charset, final String prefix, int defaultPeriod, String key) {

    Timer timer = new Timer();
    String periodString = (String) configMap.get(ConfigConstants.period);
    int period;
    if(periodString != null && !periodString.isEmpty()) {
      period = Integer.valueOf(periodString);
    } else {
      period = defaultPeriod;
    }
    if(period <= 0) {
      throw new IllegalStateException("Period is not set. key:" + key);
    }
    
    
    CommandCallback commandCallback = new CommandCallback() {
      
      public void call(String result) {
        TypeExcutor typeExcutor = typeExcutorFactory.getTypeExcutor((String) configMap.get(ConfigConstants.TYPE));
        Map<String, Object> data = typeExcutor.excute(configMap, result);
        if(prefix!=null && !prefix.isEmpty()) {
          data = OsMonitorUtils.concatPrefix(data, prefix);
        }
        logger.info("send data : " + data);
        OsMonitorUtils.changeDataToNumber(data);
        pang.sendData(data);
      }
      
    };
    timer.schedule(new CommandRunner(OsMonitorUtils.toCommand(command, os), commandCallback, charset), 1000, period*1000);
  }

}