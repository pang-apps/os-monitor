package com.pangdata.apps.monitor.parser;

import java.util.Map;

import com.pangdata.apps.monitor.parser.top.TopResultParser;
import com.pangdata.apps.monitor.runner.CmdType;

public class ParserFactory {
  
  public static ResultParser getTypeExcutor(CmdType cmdType, Map config) {
    if(cmdType == CmdType.cmd) {
      return new CommonResultParser(config);
    }
    else if(cmdType == CmdType.top) {
      return new TopResultParser(config);
    }
    return null;
  }
}
