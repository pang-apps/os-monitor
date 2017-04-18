package com.pangdata.apps.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pangdata.sdk.PangException;

public class CommandRunner extends TimerTask {
  private static final Logger logger = LoggerFactory.getLogger(CommandRunner.class);

  private String charset;
  private String[] commands;
  private CommandCallback commandCallback;

  private boolean completed;
  private boolean timeoutOccured;

  protected String hangmsg;
  
  final int commandTimeout = 5000;

  public CommandRunner() {}

  public CommandRunner(String[] commands) {
    this.commands = commands;
  }

  public CommandRunner(String[] commands, CommandCallback commandCallback) {
    this.commands = commands;
    this.commandCallback = commandCallback;
  }

  public CommandRunner(String[] commands, CommandCallback commandCallback, String charset) {
    this.commands = commands;
    this.commandCallback = commandCallback;
    this.charset = charset;
  }

  public CommandRunner(String[] commands, String charset) {
    this.commands = commands;
    this.charset = charset;
  }

  public String byProcessBuilder(final String[] command) throws IOException {

    final Process process = new ProcessBuilder(command).start();
    final Timer timeoutTimer = new Timer("Timer-Timeout checker");
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
      process.waitFor();
      completed = true;
    } catch (InterruptedException e) {
      throw new PangException("The process has timed out. command:" + command[2]);
    }
    
    timeoutTimer.cancel();
    if (timeoutOccured) {
      logger.error("The process has timed out. command:" + command[2]);
      System.exit(-1);
      throw new PangException("The process has timed out. Command:" + command[2]);
    }

    String result = null;
    try (InputStream psout = process.getInputStream()) {
      result = copy(psout);
    } catch (IOException e) {
      throw new PangException(e);
    }

    InputStream errorStream = process.getErrorStream();
    String errorMessage = copy(errorStream);
    process.destroy();
    
    if (errorMessage != null && !errorMessage.isEmpty()) {
      // logger.error("Command got error stream. command:{}, message:{}", command, errorMessage);
      throw new PangException("Command got error stream: " + command[2] + "\n" + errorMessage);
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
        result.append(System.getProperty("line.separator"));
        if (count < 1000) {
          logger.debug(String.format("Output[%d] %s", count, line));
          count++;
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
    logger.debug("run");
    String result = null;
    try {
      result = byProcessBuilder(commands);
    } catch (IOException e) {
      logger.error("IOException occured", e);
    }
    commandCallback.call(result);
  }

  public String excute() {
    String result = null;
    try {
      result = byProcessBuilder(commands);
    } catch (IOException e) {
      logger.error("Got IO exception", e);
    }
    logger.trace("result:{}", result);
    return result;
  }

}
