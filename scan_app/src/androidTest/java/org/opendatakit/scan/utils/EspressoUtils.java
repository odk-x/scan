package org.opendatakit.scan.utils;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.CheckedTextView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.opendatakit.scan.android.R;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.PreferenceMatchers.withKey;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

public class EspressoUtils {
  //Timeout related
  private static final int TIMEOUT = 10;
  private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;

  //Template selection related
  private static final String TEMPLATE_PREF_KEY = "select_templates";
  private static final String TEMPLATE = "example";

  /**
   * Adjusts Espresso idle wait time to avoid timeout
   * <p>
   * Timeout is adjusted to {@value #TIMEOUT} minutes.
   * </p>
   */
  public static void adjustIdleWaitTimeout() {
    IdlingPolicies.setMasterPolicyTimeout(TIMEOUT, TIMEOUT_UNIT);
  }

  /**
   * Returns the timeout in milliseconds
   *
   * @return duration in milliseconds
   */
  public static long getTimeoutMillis() {
//    return TIMEOUT_UNIT.toMillis(TIMEOUT);
    return 3000;
  }

  public static void templateSetup(String template, boolean turnOn) {
    //Open Preferences
    onView(withId(R.id.menu_scan_preferences)).perform(click());
    onData(withKey(TEMPLATE_PREF_KEY)).perform(click());

    //Get template selection state
    final boolean[] state = new boolean[1];
    onData(is(template)).check(matches(new BoundedMatcher<View, CheckedTextView>
        (CheckedTextView
        .class) {

      @Override
      public void describeTo(Description description) {}

      @Override
      protected boolean matchesSafely(CheckedTextView item) {
        state[0] = item.isChecked();
        return true;
      }
    }));

    //Toggle template when current state != desired
    if (state[0] != turnOn) {
      onData(is(template)).perform(click());
    }

    //Press OK
    onView(withId(android.R.id.button1)).perform(click());

    //Exit preferences
    Espresso.pressBack();
  }
}
