/*
 * Copyright (C) 2015 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.scan;

import android.support.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import android.support.test.internal.util.Checks;
import android.view.View;
import android.widget.TextView;

public class ColorMatcher {
  public static Matcher<View> withTextColor(final int color) {
    Checks.checkNotNull(color);

    return new BoundedMatcher<View, TextView>(TextView.class) {
      @Override
      public boolean matchesSafely(TextView view) {
        return color == view.getCurrentTextColor();
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("With text color: " + color);
      }
    };
  }
}
