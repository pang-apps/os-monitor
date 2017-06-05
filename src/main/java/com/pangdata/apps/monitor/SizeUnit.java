package com.pangdata.apps.monitor;

public enum SizeUnit {
 
  KB { 
    public long to(long value) {
      if(value == 0) {
        return 0;
      } else {
        return value/1024l;
      }
    }
  },
  MB {
    @Override
    public long to(long value) {
      if(value == 0) {
        return 0;
      } else {
        return value/1048576l;
      }
    }
  }, GB {
    @Override
    public long to(long value) {
      if(value == 0) {
        return 0;
      } else {
        return value/1073741824l;
      }
    }
  };

  public abstract long to(long value);
}
