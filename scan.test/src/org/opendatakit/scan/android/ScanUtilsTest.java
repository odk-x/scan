package org.opendatakit.scan.android;

import android.test.AndroidTestCase;

public class ScanUtilsTest extends AndroidTestCase {
  private static final String APP_NAME = "tables";

  public void testValuesMatch() {
	String appName = ScanUtils.getODKAppName();
	assertEquals(appName, APP_NAME);
  }

}
