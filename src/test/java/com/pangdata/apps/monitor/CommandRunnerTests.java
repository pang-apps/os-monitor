package com.pangdata.apps.monitor;

import java.io.IOException;

import org.junit.Test;

import com.pangdata.apps.monitor.runner.CommandRunner;
import com.pangdata.apps.monitor.util.OsCheck;
import com.pangdata.apps.monitor.util.OsMonitorUtils;
import com.pangdata.apps.monitor.util.OsCheck.OSType;
import com.pangdata.sdk.PangException;

public class CommandRunnerTests {

  @Test
  public void normalCommandExcuteTest() throws IOException {
    OSType osType = OsCheck.getOperatingSystemType();
    // String charset = OsMonitorUtils.getCharset(osType);
    String charset = "MS949";
    String command = "dir";
    String result = new CommandRunner(OsMonitorUtils.toCommand(command, osType), charset).excute();
    System.out.println("Command:" + command);
    System.out.println("Result:" + result);
  }

  @Test(expected = PangException.class)
  public void illegalCommandExcuteTest() throws IOException {
    OSType osType = OsCheck.getOperatingSystemType();
    // String charset = OsMonitorUtils.getCharset(osType);
    String charset = "MS949";
    String command = "asdfasdf";
    String result = new CommandRunner(OsMonitorUtils.toCommand(command, osType), charset).excute();
    System.out.println("Command:" + command);
    System.out.println("Result:" + result);
  }

  @Test
  public void hangCommandExcuteTest() throws IOException {
    OSType osType = OsCheck.getOperatingSystemType();
    // String charset = OsMonitorUtils.getCharset(osType);
    String charset = "MS949";
    String command = "cmd";
    String result = new CommandRunner(OsMonitorUtils.toCommand(command, osType), charset).excute();
    System.out.println("Command:" + command);
    System.out.println("Result:" + result);
  }
}
