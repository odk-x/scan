package org.opendatakit.scan.utils;

import android.support.test.espresso.IdlingPolicies;

import java.util.concurrent.TimeUnit;

public class EspressoUtils {
   private static int TIMEOUT = 10;

   public static void adjustEspressoIdleWaitTimeout() {
     IdlingPolicies.setMasterPolicyTimeout(TIMEOUT, TimeUnit.MINUTES);
   }
}
