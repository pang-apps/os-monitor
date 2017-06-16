package com.pangdata.apps.monitor.runner;

import java.util.Map;


public interface CommandCallback {
  void call(Map<String, Object> data);
}
