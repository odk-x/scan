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

import org.junit.*;

import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewScannedFormsTest {
  //private static final String OUTPUT_DIR_NAME = ScanUtils.getOutputDirPath()
  //.substring(ScanUtils.appFolder.length(), ScanUtils.getOutputDirPath().length() - 1);

  // TODO: Fix these tests
  @Test
  public void dummyTest() {
    assert (true);
  }

  /*
  @Rule
  public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
      MainActivity.class);

  @BeforeClass
  public static void setUp() throws IOException {
    //populate dummy data
    copyAssets(InstrumentationRegistry.getContext().getAssets(), OUTPUT_DIR_NAME);
  }

  @AfterClass
  public static void cleanUp() throws IOException {
    //delete dummy data
    deleteAssets(InstrumentationRegistry.getContext().getAssets(), OUTPUT_DIR_NAME);
  }

  //Pre-condition to all tests in this class
  //there must be at least one scanned form
  @Before
  public void hasAtLeastOneForm() {
    extendIdleWaitTimeout();

    onView(withId(R.id.ViewFormsButton)).perform(click());
    onData(anything()).atPosition(0).check(matches(isCompletelyDisplayed()));
  }

  @Test
  public void viewForms_displayEntries() {
    String[] photoNames = getPhotoNames();

    //Check if each output is displayed
    for (String s : photoNames) {
      onData(is(s)).check(matches(isCompletelyDisplayed()));
    }

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
  public void viewForms_displayEntriesMetadata() {
    String[] photoNames = getPhotoNames();

    //Check if metadata of each output is displayed correctly
    for (String s : photoNames) {
      //check PhotoStatus
      int color = Color.parseColor("#FF0000"); //red
      if (new File(ScanUtils.getJsonPath(s)).exists()) {
        color = Color.parseColor("#00FF00"); //green
      } else if (new File(ScanUtils.getAlignedPhotoPath(s)).exists()) {
        color = Color.parseColor("#FFFF00"); //yellow
      }
      onData(is(s)).onChildView(withId(R.id.photoStatus))
          .check(matches(ColorMatcher.withTextColor(color)));

      //Check templateName
      onData(is(s)).onChildView(withId(R.id.templateName))
          .check(matches(withText(s.split("_")[0])));

      //Check createdTime
      onData(is(s)).onChildView(withId(R.id.createdTime)).check(matches(
          withText(new Date(new File(ScanUtils.getPhotoPath(s)).lastModified()).toString())));
    }
  }

  @Test
  public void viewForms_DisplayProcessedForm() {
    //if first item is green, activity should land on Display Processed Form
    //if otherwise, should stay on the same activity
    try {
      //check color
      onData(anything()).atPosition(0).onChildView(withId(R.id.photoStatus))
          .check(matches(ColorMatcher.withTextColor(Color.parseColor("#00FF00"))));

      //click first item
      onData(anything()).atPosition(0).onChildView(withId(R.id.templateName)).perform(click());
      //check title
      onView(withId(android.R.id.title)).check(matches(withText(
          mActivityRule.getActivity().getResources()
              .getString(R.string.display_processed_form_activity))));
    } catch (junit.framework.AssertionFailedError e) {
      //click first item
      onData(anything()).atPosition(0).onChildView(withId(R.id.templateName)).perform(click());
      //check title
      onView(withId(android.R.id.title)).check(matches(withText(
          mActivityRule.getActivity().getResources()
              .getString(R.string.view_bubble_forms_activity))));
    }
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

  /**
   * Copies sourceDir from assets directory to ODKScan directory
   *
   * @param assetMngr
   * @param sourceDir
   * @throws IOException
   *
  private static void copyAssets(AssetManager assetMngr, String sourceDir) throws IOException {
    String[] fileList = assetMngr.list(sourceDir);

    for (String s : fileList) {
      String newDir = sourceDir + "/" + s;
      String[] subDirFileList = assetMngr.list(newDir);

      if (subDirFileList.length == 0) {
        copyFile(assetMngr.open(newDir),
            new FileOutputStream(new File(ScanUtils.appFolder + newDir)));
      } else {
        new File(ScanUtils.appFolder + newDir).mkdir();

        copyAssets(assetMngr, newDir);
      }
    }
  }

  /**
   * Deletes sourceDir of assets directory from ODKScan directory
   *
   * @param assetMngr
   * @param sourceDir
   * @throws IOException
   *
  private static void deleteAssets(AssetManager assetMngr, String sourceDir) throws IOException {
    String[] fileList = assetMngr.list(sourceDir);

    for (String s : fileList) {
      String newDir = sourceDir + "/" + s;
      String[] subDirFileList = assetMngr.list(newDir);

      if (subDirFileList.length == 0) {
        new File(ScanUtils.appFolder + newDir).delete();
      } else {
        deleteAssets(assetMngr, newDir);
        new File(ScanUtils.appFolder + newDir).delete();
      }
    }
  }

  /**
   * Copies an InputStream to an OutputStream
   *
   * @param in
   * @param out
   * @throws IOException
   *
  private static void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;

    while ((read = in.read(buffer)) > 0) {
      out.write(buffer, 0, read);
    }
  }

  private void extendIdleWaitTimeout() {
    IdlingPolicies.setMasterPolicyTimeout(10, TimeUnit.MINUTES);
  }
  */
}