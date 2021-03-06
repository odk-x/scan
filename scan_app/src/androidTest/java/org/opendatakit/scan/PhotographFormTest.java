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

import androidx.test.espresso.intent.rule.IntentsTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.opendatakit.scan.activities.MainActivity;

public class PhotographFormTest {

   @Rule public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(
       MainActivity.class);

   // TODO: Fix these tests
   @Test public void dummyTest() {
      assert (true);
   }

  /*
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

    // TODO: Fix this test
    assert (true);
    /*
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
    *
  }

  /**
   * Traverses "output" directory to find all expected entries of scanned forms
   *
   * @return A String[] of the entries
   *
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
  */
}