package com.pangdata.apps.monitor;

import org.junit.Test;

import com.pangdata.sdk.util.SizeUnit;

public class SizeUnitTests {

  @Test
  public void testSize() {
    int cores = Runtime.getRuntime().availableProcessors();
    System.out.println(cores);
    long l = 131419700051833774l;
    System.out.println(l);
    System.out.println(SizeUnit.GB.to(1000000000));
    System.out.println(SizeUnit.MB.to(100000000));
    System.out.println(SizeUnit.KB.to(10000));
  }
}
