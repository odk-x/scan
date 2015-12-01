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

import android.support.test.espresso.IdlingPolicies;
import org.hamcrest.Matcher;
import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.activities.MainMenuActivity;
import org.opendatakit.scan.android.utils.ScanUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.preference.Preference;
import android.provider.MediaStore;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.Instrumentation.ActivityResult;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class) @LargeTest public class PhotographFormTest {

   @Rule public IntentsTestRule<MainMenuActivity> mActivityRule = new IntentsTestRule<>(
       MainMenuActivity.class);

   //block external intents
   @Before public void stubAllExternalIntents() {
      extendIdleWaitTimeout();

      intending(not(isInternal()))
          .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));
   }

   @Test public void scanNewForm_cancel() {
      //get list of outputs before "Scan New Form"
      String[] photoNames = getPhotoNames();

      //Click "Scan New Form" and cancel
      //Cancel is handled by intent stubbing ( stubAllExternalIntents() )
      onView(withId(R.id.ScanButton)).perform(click());
      intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));

      //List of expected entries
      List<Matcher<? super String>> photoList = new ArrayList<>();
      for (String s : photoNames) {
         photoList.add(is(s));
      }

      //Check if there are extra entries
      //If no extra entries exist, no exception will be thrown
      //If extra entries exist, test fails
      //Very ugly but it works
      try {
         onData(not(anyOf(photoList))).check(doesNotExist());
      } catch (RuntimeException e) {
      }
   }

   /**
    * Traverses "output" directory to find all expected entries of scanned forms
    *
    * @return A String[] of the entries
    */
   private String[] getPhotoNames() {
      return new File(ScanUtils.getOutputDirPath()).list(new FilenameFilter() {
         public boolean accept(File dir, String name) {
            return (new File(dir, name)).isDirectory();
         }
      });
   }

   private void extendIdleWaitTimeout() {
      IdlingPolicies.setMasterPolicyTimeout(10, TimeUnit.MINUTES);
   }
}