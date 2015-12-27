package org.opendatakit.scan.utils;

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
}
