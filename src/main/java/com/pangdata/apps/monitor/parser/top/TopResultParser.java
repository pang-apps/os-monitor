package com.pangdata.apps.monitor.parser.top;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pangdata.apps.monitor.ConfigConstants;
import com.pangdata.apps.monitor.parser.CommonResultParser;
import com.pangdata.apps.monitor.util.MapUtil;
import com.pangdata.apps.monitor.util.OsCheck;
import com.pangdata.apps.monitor.util.OsCheck.OSType;
import com.pangdata.sdk.util.NumberUtils;
import com.pangdata.sdk.util.PangProperties;
import com.pangdata.sdk.util.SdkUtils;
import com.pangdata.sdk.util.SizeUnit;

// For windows
// https://msdn.microsoft.com/en-us/library/ms974615.aspx
// https://msdn.microsoft.com/en-us/library/aa394569(VS.85).aspx
// https://msdn.microsoft.com/en-us/library/ms974615.aspx
//https://social.technet.microsoft.com/Forums/windowsserver/en-US/bb722463-a3af-4779-a9e3-c3564ffb8a19/cpu-utilization-command-in-powershell?forum=winserverpowershell
public class TopResultParser extends CommonResultParser{
  private static final Logger logger = LoggerFactory.getLogger(TopResultParser.class);
  
  private CpuCalculator calculator;

  private int monitor_interval = 5000;

  private long lastMonitored = 0;
  
  private long lastCpu = 0;
  
  private long lastSnapshotted= 0;

  private int snapshot_interval = 5000;
  
  private int cpu_interval = 5000;
 
  public TopResultParser(Map config) {
    super(config);
    int monitorinterval = Integer.parseInt((String)config.get("monitorInterval"));
    if(monitorinterval > 1) {
      this.monitor_interval = monitorinterval * 1000;
    }
    
    int snapshotinterval = Integer.parseInt((String)config.get("snapshotInterval"));
    if(snapshotinterval > 1) {
      this.snapshot_interval = snapshotinterval * 1000;
    }
    
    int cpuInterval = Integer.parseInt((String)config.get("cpuInterval"));
    if(cpuInterval > 1) {
      this.cpu_interval = cpuInterval * 1000;
    }
    
    OSType ost = OsCheck.getOperatingSystemType();
    if(ost == OSType.Windows) {
      calculator = new WindowsCpuCalculator();
    } else {
      calculator = new LinuxCpuCalculator();
    }
  }

  @Override
  protected Map<String, Object> doMapping(String[] lines) {
    
    String mappings = (String) config.get(ConfigConstants.TOP_MAPPINGS);
    
    Map<String, Integer> indexes = findColumnIndex(lines[0], mappings);
    Map<String, Map<String, Object>> readValues = readValues(config, indexes, lines);
    if(readValues == null) {
      return null;
    }
    
    Map<Map<String, Object>, Double> calculated = calculator.calculate(readValues);
    if(calculated == null) {
      return null;
    }
    
    calculated = MapUtil.sortByValue(calculated);
    
    int topn = 10;
    try {
      topn = Integer.parseInt((String) config.get("topn"));
    } catch (Exception e) {}
    
    Map<String, Object> result = new HashMap<String, Object>();
    Map<String, Object> monitor_process = new HashMap<String, Object>();
    StringBuilder tops = new StringBuilder();
    
    Iterator<Entry<Map<String, Object>, Double>> iterator = calculated.entrySet().iterator();
    double sum = 0;
    int count = 0;
    
    int snapshot = Integer.parseInt((String)config.get("snapshotOnCpuUsage"));
    Set<String> monitor = PangProperties.getList(config.get("monitor"));
    while(iterator.hasNext()) {
      Entry<Map<String, Object>, Double> process = iterator.next();
      Map<String, Object> detail = process.getKey();
      String name = (String) detail.get("name");
      double cpu = (double)detail.get("cpu");
      // Process Monitoring
      if(monitor != null && monitor.contains(name)) {
        getDetail(monitor_process, detail, name, cpu);
      }
      sum+=cpu;
      if(snapshot > 0) {
        if(count < topn) {
          //          tops.put(name, name+":cpu="+cpu+",mem="+SizeUnit.KB.to((long) detail.get("mem"))+",ioread="+SizeUnit.KB.to((long) detail.get("ioread"))+",iowrite="+SizeUnit.KB.to((long) detail.get("iowrite"))+",tcount="+detail.get("tcount"));
          tops.append("|");
          tops.append(name+"=cpu:"+cpu+"%, mem:"+SizeUnit.KB.to2((long) detail.get("mem"))+"KB, tcount="+detail.get("tcount"));
        }
        count++;        
      }
    }
    
   
    long currentTimeMillis = System.currentTimeMillis();
    sum = NumberUtils.rountTo2decimal(sum);
    if(sum > 100) {
      sum = 100.0;
    }
    //Snapshot
    if(sum>=snapshot && snapshot > 0) {
      if((currentTimeMillis - lastSnapshotted) > this.snapshot_interval) {
        tops.insert(0, "CPU Usage="+sum+"%");
        String devicename = SdkUtils.getDevicename((String) config.get("devicename"));
        String value = tops.toString();
        result.put(devicename, value);
        addDeviceMeta(devicename, value, null, null, null, null);
        lastSnapshotted = currentTimeMillis;
      }
    }
    //Monitor
    if((currentTimeMillis - lastMonitored) > this.monitor_interval) {
      result.putAll(monitor_process);
      lastMonitored = currentTimeMillis;
    }
    //CPU
    if((currentTimeMillis - lastCpu) > this.cpu_interval) {
      String devicename = SdkUtils.getDevicename("cpu"+PangProperties.getConcatenator()+"usage");
      result.put(devicename, sum);
      addDeviceMeta(devicename, sum, null, null, null, null);
      lastCpu = currentTimeMillis;
    }
    return result; 
  }
  
  private void getDetail(Map<String, Object> data, Map<String, Object> detail, String name,
      double cpu) {
    String devicename = SdkUtils.getDevicename(name+PangProperties.getConcatenator()+"cpu");
    addDeviceMeta(devicename, cpu, null, null, null, null);
    data.put(devicename, cpu);
    devicename = SdkUtils.getDevicename(name+PangProperties.getConcatenator()+"tcount");
    addDeviceMeta(devicename, detail.get("tcount"), null, null, null, null);
    data.put(devicename, detail.get("tcount"));
    devicename = SdkUtils.getDevicename(name+PangProperties.getConcatenator()+"mem");
    long to2 = SizeUnit.KB.to2((long) detail.get("mem"));
    data.put(devicename, to2);
    addDeviceMeta(devicename, to2, null, null, null, null);
//    data.put(name+"-ioread", SizeUnit.KB.to((long) detail.get("ioread")));
//    data.put(name+"-iowrite", SizeUnit.KB.to((long) detail.get("iowrite")));
  }

  
  private Map<String, Map<String, Object>> readValues(Map<String, Integer> config, Map<String, Integer> indexes, String[] lines) {
    if(lines.length < 2) {
      logger.warn("No process list data to read");
      return null;
    }
    
    String[] mappedNames = indexes.keySet().toArray(new String[]{});
    
    Map<String, Map<String, Object>> values = new HashMap<String, Map<String, Object>>();
    
    Set<String> excludes = PangProperties.getList(config.get(ConfigConstants.exclude), true);
    int nameIdx = indexes.get("name");
    int cpuIdx = indexes.get("cpu");
    //i = 1. Skip column line
    for(int i=1;i<lines.length;i++) {
      try {
        String[] line = lines[i].trim().split("\\s+");
        if(line.length <= 1) {
          continue;
        }
        String name = line[nameIdx].toLowerCase();
        if(excludes.contains(name)) {
          continue;
        }
        
  
        try {
          Long.parseLong(line[nameIdx+1]);
        } catch (Exception e) {
          String[] tmpLine =  new String[line.length-1];
          boolean merged = false;
          for(int s=0;s<line.length;s++) {
            if(s==(nameIdx+1)) {
              tmpLine[s-1] = line[s-1]+"_"+line[nameIdx+1];
              merged = true;
              continue;
            }
            if(merged) {
              tmpLine[s-1] = line[s];
            } else {
              tmpLine[s] = line[s];
            }
          }
          line = tmpLine;
        }
        
        Map<String, Object> data = new HashMap<String, Object>();
        
        for(int k=0;k<mappedNames.length;k++) {
          String mappedName = mappedNames[k];
          int index = indexes.get(mappedName);
          if(index == nameIdx) {
            data.put(mappedName, line[index]);
          } else {
            data.put(mappedName, Long.parseLong(line[index]));
          }
        }
        
        values.put(line[nameIdx], data);
      } catch(Exception e) {
        logger.error("Read values has error", e);
      }
    }
    return values;
  }

  /*
   * name:Name,cpu:PercentProcessorTime,mem:WorkingSetPrivate,tcount:ThreadCount
   */
  private Map<String, Integer> findColumnIndex(String line, String mappings) {
    
    Map<String /*name*/, Integer/* column index */> indexs = new HashMap<String, Integer>();
    
    String[] pairedNames = mappings.split(",");
    String[] names = line.trim().split("\\s+");    
    for(int i=0;i<pairedNames.length;i++) {
      String[] paired = pairedNames[i].split(":");
      if(paired.length != 2) {
        throw new IllegalArgumentException("Name mappings is not correct. See your config 'top.mappings'");
      }
      boolean found = false;
      for(int j=0;j<names.length;j++) {
        if(paired[1].equalsIgnoreCase(names[j])) {
          indexs.put(paired[0].trim().toLowerCase(), j);
          found = true;
          break;
        }
      }
      if(!found) {
        logger.warn("'top.mappings's target column("+paired[1]+") can not be found in process list");
      }
    }
    return indexs;
  }
  
}
