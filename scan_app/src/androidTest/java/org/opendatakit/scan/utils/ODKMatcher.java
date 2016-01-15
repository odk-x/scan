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

package org.opendatakit.scan.utils;

import android.support.test.espresso.matcher.BoundedMatcher;

import android.widget.ListView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import android.support.test.internal.util.Checks;
import android.view.View;
import android.widget.TextView;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

public class ODKMatcher {
  public static Matcher<View> withTextColor(final int color) {
    return new BoundedMatcher<View, TextView>(TextView.class) {
      private int textColor;

      @Override
      public boolean matchesSafely(TextView view) {
        this.textColor = view.getCurrentTextColor();
        return color == this.textColor;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Text color should be: " + color + "; Got: " + this.textColor);
      }
    };
  }

  public static Matcher<View> withSize(final int size) {
    return new BoundedMatcher<View, ListView>(ListView.class) {
      private int listSize;

      @Override
      public void describeTo(Description description) {
        description.appendText("List size should be: " + size + "; Got: " + this.listSize);
      }

      @Override
      protected boolean matchesSafely(ListView item) {
        this.listSize = item.getCount();

        return this.listSize == size;
      }
    };
  }
}
