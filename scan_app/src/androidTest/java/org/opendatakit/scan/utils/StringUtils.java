package org.opendatakit.scan.utils;

import android.app.Instrumentation;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;

public class StringUtils {
  /**
   * Returns the String with Id stringId using an ActivityTestRule
   *
   * @param rule     ActivityTestRule to get String from
   * @param stringId Id of String to retrieve
   * @return Returns the String
   */
  public static String getString(ActivityTestRule rule, int stringId) {
    return rule.getActivity().getResources().getString(stringId);
  }

  public static String getString(IntentsTestRule rule, int stringId) {
    return rule.getActivity().getResources().getString(stringId);
  }
}
