package com.pangdata.apps.monitor.type;

import java.util.Map;

public abstract class TypeExcutor {
  
  public abstract Map<String, Object> excute(Map config, String data);
  
}
