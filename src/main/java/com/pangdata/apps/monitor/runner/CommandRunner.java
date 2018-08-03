package com.pangdata.apps.monitor.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pangdata.apps.monitor.TimeOutException;
import com.pangdata.apps.monitor.parser.ParserFactory;
import com.pangdata.apps.monitor.parser.ResultParser;
import com.pangdata.sdk.PangException;

public class CommandRunner extends TimerTask {
  private static final Logger logger = LoggerFactory.getLogger(CommandRunner.class);

  private String charset;
  private String[] commands;

  private boolean completed;
  private boolean timeoutOccured;

  protected String hangmsg;
  
  // Do not change below timeout.
  int commandTimeout = 10000;

  private Map config;

  private ResultParser resultParser;

  private CommandCallback commandCallback;
  
  public CommandRunner() {}

  public CommandRunner(String[] commands) {
    this.commands = commands;
  }

  public CommandRunner(String[] commands, CommandCallback commandCallback) {
    this.commands = commands;
  }

  public CommandRunner(CommandCallback commandCallback, CmdType cmdType, String[] commands, Map configMap, String charset) {
    resultParser = ParserFactory.getTypeExcutor(cmdType, configMap);
    this.commands = commands;
    this.charset = charset;
    int timeout = 0;
    try {
      timeout = Integer.parseInt((String) configMap.get("timeout"));
    } catch (Exception e) {
    }
    if(timeout > 1) {
      this.commandTimeout = timeout * 1000;
    }
    this.config = configMap;
    this.commandCallback = commandCallback;
  }

  public CommandRunner(String[] commands, String charset) {
    this.commands = commands;
    this.charset = charset;
  }

  public String byProcessBuilder(final String[] command) throws IOException {
    timeoutOccured = false;
    completed = false;
    logger.debug("Cmd: " + Arrays.toString(command));
//    final Process process = Runtime.getRuntime().exec(command);
    final Process process = new ProcessBuilder(command).start();
    final Timer timeoutTimer = new Timer("cmd-time-checker");
    timeoutTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (completed == false) {
          timeoutOccured = true;
        }
        process.destroy();
      }
    }, commandTimeout);

    try {
      logger.debug("Command waiting...");      
      process.waitFor();
      completed = true;
    } catch (InterruptedException e) {
      throw new PangException("The process has timed out. command:" + command[2]);
    } finally {
      timeoutTimer.cancel();
    }
    
    logger.debug("Reading result...");
    String result = null;
    try {
      try (InputStream psout = process.getInputStream()) {
        result = copy(psout);
      } catch (IOException e) {
        throw new PangException(e);
      }
  
      InputStream errorStream = process.getErrorStream();
      String errorMessage = copy(errorStream);
      
      if (errorMessage != null && !errorMessage.isEmpty()) {
        // logger.error("Command got error stream. command:{}, message:{}", command, errorMessage);
        throw new PangException("Command got error stream: " + command[2] + "\n" + errorMessage);
      }
    } catch (PangException e) {
      throw e;
    } finally {
      if(process != null) {
        process.destroy();
      }
    }
    
    if (result == null && timeoutOccured) {
      logger.error("The process has timed out. command:" + command[2]);
      // In the case of timeout occurred. Do not exit. Keep going.
      // System.exit(-1);
      if(process != null) {
        process.destroy();
      }
      throw new TimeOutException("The process has timed out. Command:" + command[2]);
    }

    return result;
  }

  public String copy(InputStream input) {

    InputStreamReader isr = null;
    if (charset != null) {
      try {
        isr = new InputStreamReader(input, charset);
      } catch (UnsupportedEncodingException e) {
        logger.error("Character set is wrong.", e);
        return null;
      }
    } else {
      isr = new InputStreamReader(input);
    }

    BufferedReader br = new BufferedReader(isr);
    String line = null;
    StringBuilder result = new StringBuilder();
    try {
      int count = 0;
      while (br.ready()) {
        line = br.readLine();
        result.append(line);
        result.append("\n");
        if(logger.isDebugEnabled()) {
          if (count < 1000) {
            logger.trace(String.format("Output[%d] %s", count, line));
            count++;
          }
        }
      }
    } catch (IOException e) {
      logger.error("Got a stream error");
    } finally {
      completed = true;
      logger.debug(String.format("Input stream closed"));

      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          logger.error("Error occured to close stream", e);
        }
      }
    }
    return result.toString();
  }

  @Override
  public void run() {
    logger.debug("command started");
    String result = null;
    try {
      result = byProcessBuilder(commands);
    } catch (IOException e) {
      logger.error("Error occurred", e);
    } catch (Exception e) {
      logger.error("Error occurred", e);
    }
    
    logger.debug("command completed");
    Map<String, Object> data = resultParser.parse(result);
    
    commandCallback.call(data);
  }

  public String excute() {
    String result = null;
    try {
      result = byProcessBuilder(commands);
    } catch (IOException e) {
      logger.error("Got IO exception", e);
    } catch (Exception e) {
      logger.error("Error occurred", e);
    }
    logger.trace("result:{}", result);
    return result;
  }
}
