package com.pangdata.apps.monitor.type;

import java.util.HashMap;
import java.util.Map;

public class TypeExcutorFactory {
  private Map<String, TypeExcutor> typeExcutorMap = new HashMap<String, TypeExcutor>();
  private CommonTypeExcutor commonExcutor;
  
  public TypeExcutorFactory() {
    commonExcutor = new CommonTypeExcutor();
    typeExcutorMap.put("common", commonExcutor );
  }

  public TypeExcutor getTypeExcutor(String type) {
    if(type == null) {
      return commonExcutor;
    }
    return typeExcutorMap.get(type);
  }
}
