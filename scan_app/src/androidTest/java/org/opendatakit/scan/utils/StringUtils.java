package org.opendatakit.scan.utils;

import android.support.test.rule.ActivityTestRule;

public class StringUtils {
  public static String getString(ActivityTestRule rule, int stringId) {
      return rule.getActivity().getResources().getString(stringId);
  }
}
