package com.pangdata.apps.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRunner extends TimerTask {
  private static final Logger logger = LoggerFactory.getLogger(CommandRunner.class);

  private String charset;
  private String[] commands;
  private CommandCallback commandCallback;

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

  public String byProcessBuilder(String[] command) throws IOException, InterruptedException {
    Process process = new ProcessBuilder(command).start();

    process.waitFor();

    String result = null;
    try (InputStream psout = process.getInputStream()) {
      result = copy(psout);
    }
    return result;
  }

  //TODO Check error stream. Refer batch source.
  public String copy(InputStream input) throws IOException {

    InputStreamReader isr = null;
    if (charset != null) {
      isr = new InputStreamReader(input, charset);
    } else {
      isr = new InputStreamReader(input);
    }
    BufferedReader br = new BufferedReader(isr);
    String line = null;
    StringBuilder result = new StringBuilder();
    try {
      int count = 0;
      while ((line = br.readLine()) != null) {
        result.append(line);
        result.append(System.getProperty("line.separator"));
        // if (count < 1000) {
        // logger.debug(String.format("Output[%d] %s", count, line));
        // count++;
        // }
      }
    } catch (IOException e) {
      logger.error("Got a stream error");
    } finally {
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
    try {
      String result = null;
      try {
        result = byProcessBuilder(commands);
      } catch (Exception e) {
        logger.error("Caught error when excuting commands. {}", commands, e);
      }
      commandCallback.call(result);
    } catch (Exception e) {
      logger.error("Exception occured", e);
    }
  }

  public String excute() {
    String result = null;
    try {
      result = byProcessBuilder(commands);
    } catch (Throwable e) {
      logger.error("Caught error when excuting commands. {}", commands, e);
    }
    logger.trace("result:{}", result);
    return result;
  }

}
