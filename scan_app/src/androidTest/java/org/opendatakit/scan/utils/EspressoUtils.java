package org.opendatakit.scan.utils;

import android.support.test.espresso.IdlingPolicies;

import java.util.concurrent.TimeUnit;

public class EspressoUtils {
  private static final int TIMEOUT = 10;

  /**
   * Adjusts Espresso idle wait time to avoid timeout
   * <p>
   * Timeout is adjusted to {@value #TIMEOUT} minutes.
   * </p>
   */
  public static void adjustIdleWaitTimeout() {
    IdlingPolicies.setMasterPolicyTimeout(TIMEOUT, TimeUnit.MINUTES);
  }
}
