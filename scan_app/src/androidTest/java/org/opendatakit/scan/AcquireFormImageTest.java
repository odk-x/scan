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

import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.activities.MainActivity;
import org.opendatakit.scan.android.utils.ScanUtils;

import android.app.Activity;
import android.app.Instrumentation;
import android.provider.MediaStore;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.hamcrest.Matcher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AcquireFormImageTest {

  @Rule
  public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(
      MainActivity.class);

  //block external intents
  @Before
  public void stubAllExternalIntents() {
    extendIdleWaitTimeout();

    intending(not(isInternal()))
        .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));
  }

  @Test
  public void scanNewForm_cancel() {
    //get list of outputs before "Scan New Form"
    String[] photoNames = getPhotoNames();

    //Click "Scan New Form" and cancel
    //Cancel is handled by intent stubbing ( stubAllExternalIntents() )
    onView(withId(R.id.ScanButton)).perform(click());

    //Open "View Scanned Forms"
    onView(withId(R.id.ViewFormsButton)).perform(click());

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

  @Test
  public void scanNewForm_launchCamera() {
    //Click "Scan New Form" and cancel
    //Cancel is handled by intent stubbing ( stubAllExternalIntents() )
    onView(withId(R.id.ScanButton)).perform(click());

    intended(hasAction(MediaStore.ACTION_IMAGE_CAPTURE));
  }

  @Test
  public void processSavedImage_launchChooser() {
    //Click "Process Saved Image"
    onView(withId(R.id.ProcessImageButton)).perform(click());

    intended(hasAction("org.openintents.action.PICK_FILE"));
  }

  @Test
  public void processImageFolder_launchChooser() {
    //Click "Process Image Folder"
    onView(withId(R.id.ProcessFolderButton)).perform(click());

    intended(hasAction("org.openintents.action.PICK_DIRECTORY"));
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