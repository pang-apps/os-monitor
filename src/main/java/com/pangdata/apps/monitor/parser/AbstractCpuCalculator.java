package com.pangdata.apps.monitor.parser;

import java.util.Map;

import com.pangdata.apps.monitor.parser.top.CpuCalculator;

public abstract class AbstractCpuCalculator implements CpuCalculator {

  
  @Override
  public Map<Map<String, Object>, Double> calculate(Map<String, Map<String, Object>> values) {
    
    return doCalculate(values);
  }

  protected abstract Map<Map<String, Object>, Double> doCalculate(Map<String, Map<String, Object>> values);

}
