package com.pangdata.apps.monitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pangdata.apps.monitor.runner.CmdType;
import com.pangdata.apps.monitor.runner.CommandCallback;
import com.pangdata.apps.monitor.runner.CommandRunner;
import com.pangdata.apps.monitor.util.OsCheck;
import com.pangdata.apps.monitor.util.OsCheck.OSType;
import com.pangdata.apps.monitor.util.OsMonitorUtils;
import com.pangdata.sdk.Pang;
import com.pangdata.sdk.mqtt.PangMqtt;
import com.pangdata.sdk.util.PangProperties;


public class OsMonitor {

  private static final Logger logger = LoggerFactory.getLogger(OsMonitor.class);

  public static boolean test;

  private static Pang pang;

  private static String charset;

  private static ExecutorService executor;
  private static boolean run;
  
  public static void main(String[] args) throws Exception {
    pang = new PangMqtt();

    // Check OS
    OSType ost = OsCheck.getOperatingSystemType();
    logger.info("Os is {}.", ost.name());

    // Get terminal charset
    charset = OsMonitorUtils.getCharset();
    logger.info("Charset is {}.", charset);

    String commandFilename = (String) PangProperties.getProperty(ConfigConstants.PANG_CONF);
    if (commandFilename == null || commandFilename.trim().length() == 0) {
      if (ost == OSType.Windows)
        commandFilename = "windows.properties";
      else if (ost == OSType.Linux) {
        commandFilename = "linux.properties";
      }
    }

    logger.info("Command file is {}.", commandFilename);
    if (commandFilename == null || commandFilename.trim().length() == 0) {
      throw new IllegalStateException("commandFilename is null");
    }
    // Get commands & options
    Properties commandProperties = OsMonitorUtils.getProperties(commandFilename);
    final Map<String, Object> keyMap = OsMonitorUtils.propertiesToMap(commandProperties);

    runCmds(keyMap);

    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      
      @Override
      public void run() {
        run = false;
        if(executor != null) {
          executor.shutdown();
        }
      }
    }));
 
    if (!PangProperties.isEnabledToSend()) {
      long time = 10000;
      logger.debug("Running mode is test. {} seconds will be waited", time);
      try {
        TimeUnit.SECONDS.sleep(time);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private static String name;

  private static void runCmds(final Map<String, Object> keyMap) {
    final Map<String, String> cmdMap =
        (Map<String, String>) keyMap.get(ConfigConstants.COMMAND_PREFIX);

    if (cmdMap != null) {
      int size = cmdMap.size();
      
      executor = Executors.newFixedThreadPool(size, new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r) {

          Thread t = new Thread() {
            public void run() {
              r.run();
            }
          };
          t.setName(name);
          return t;
        }
      });

      Set<String> cmdKeys = cmdMap.keySet();

      for (final String key : cmdKeys) {
        String command = cmdMap.get(key);
        Map configMap = (Map) keyMap.get(key);
        if (configMap == null) {
          configMap = new HashMap<String, Object>();
        }
        CmdType ct = CmdType.cmd;

        String cmdType = (String) configMap.get("cmdType");
        if(cmdType != null) {
          ct = CmdType.valueOf(cmdType);
        }
        if(ct == CmdType.cmd) {
          name = ct.name() + "-" + key;
        } else {
          name = key + "-" + configMap.get("topn");
        }
        Executor e = new Executor(ct, configMap, command, key);
        executor.submit(e);
      }
    }
  }
  
  static CommandCallback cb = new CommandCallback() {
    
    @Override
    public void call(Map<String, Object> data) {
      if (data == null || data.size() == 0) {
        logger.warn("No data to send");
        return;
      }
      logger.debug("result : {}", data);

      OsMonitorUtils.changeDataToNumber(data);
      logger.info("send data : " + data);
      
      if (!OsMonitor.test) {
        pang.sendData(data);
      }
    }
  };
  
  static class Executor implements Runnable {

    private CommandRunner runner;
   
    int period;

    public Executor(CmdType cmdType, Map<String, Object> configMap, String command, String key) {
      OSType os = OsCheck.getOperatingSystemType();
      String periodString = (String) configMap.get(ConfigConstants.period);
      if (periodString != null && !periodString.isEmpty()) {
        period = Integer.valueOf(periodString) * 1000;
      } else {
        period = (int) PangProperties.getPeriod();
      }
      if (period <= 0) {
        throw new IllegalStateException("Period is not set. key:" + key);
      }
      
      this.runner = new CommandRunner(cb, cmdType, OsMonitorUtils.toCommand(command, os),
          configMap, charset);
      
    }
    
    @Override
    public void run() {
      run = true;
      while(run) {
        try {
          runner.run();
        } catch (Exception e) {
          logger.error("Runner has an error", e);
        }
        try {
          TimeUnit.MILLISECONDS.sleep(period);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

  }
}
