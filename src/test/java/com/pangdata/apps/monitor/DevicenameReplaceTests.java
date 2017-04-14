package com.pangdata.apps.monitor;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.junit.Test;

import com.pangdata.apps.monitor.type.CommonTypeExcutor;

public class DevicenameReplaceTests {
  
  @Test
  public void spaceReplaceTest() {
    String devicename1 = "devicename ";
    String devicename2 = "devicename  ";
    String replaceSpaceCharacter1 = CommonTypeExcutor.replaceSpaceCharacter(devicename1);
    String replaceSpaceCharacter2 = CommonTypeExcutor.replaceSpaceCharacter(devicename2);
    
    System.out.println(replaceSpaceCharacter1);
    System.out.println(replaceSpaceCharacter2);
  }
  
  @Test
  public void regexTest() {
    String devicename = "devicename ";
    String replaceAll = devicename.replaceAll("[\\s+]", "");
    System.out.println(devicename);
    System.out.println(replaceAll);
  }
  
  @Test
  public void test() {

    System.out.println("Default Charset=" + Charset.defaultCharset());
    System.setProperty("file.encoding", "Latin-1");
    System.out.println("file.encoding=" + System.getProperty("file.encoding"));
    System.out.println("Default Charset=" + Charset.defaultCharset());
    System.out.println("Default Charset in Use=" + getDefaultCharSet());
  }

  private static String getDefaultCharSet() {
      OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
      String enc = writer.getEncoding();
      return enc;
  }
  
  @Test
  public void aaa() {
    String charset = "x-windows-949";
    
    System.out.println(java.nio.charset.Charset.availableCharsets());
    System.out.println(java.nio.charset.Charset.isSupported(charset));

    String fecon = System.getProperty("file.encoding");
    Charset encoding = Charset.defaultCharset();
    System.out.println(fecon);
    System.out.println(encoding);
//    System.out.println(org.apache.commons.la);
//    System.out.println(java.nio.charset.Charset.isSupported(charset));
  }
}
