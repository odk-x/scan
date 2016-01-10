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

import android.content.res.AssetManager;
import android.graphics.Color;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.junit.*;
import org.junit.runner.RunWith;

import org.opendatakit.common.android.utilities.ODKFileUtils;
import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.activities.MainActivity;
import org.opendatakit.scan.android.activities.ViewScannedForms;
import org.opendatakit.scan.android.utils.ScanUtils;
import org.opendatakit.scan.utils.EspressoUtils;
import org.opendatakit.scan.utils.ODKMatcher;

import java.io.*;
import java.util.Date;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewScannedFormsTest {
  private static final String ASSET_DIR = "scan_data";
  private static final String SCAN_DATA = ODKFileUtils.getDataFolder(ScanUtils.appName);
  private static final String TEMPLATE_NAME = "example";

  private String[] expectedEntries;

  //Need to enter from MainActivity! Otherwise viewForms_DisplayProcessedForm() would not work
  @Rule
  public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(MainActivity.class);

  @BeforeClass
  public static void classSetUp() throws IOException {
    //populate dummy data
    copyAssets(InstrumentationRegistry.getContext().getAssets(), ASSET_DIR, SCAN_DATA);
  }

  @AfterClass
  public static void classCleanUp() throws IOException {
    //delete dummy data
    deleteAssets(InstrumentationRegistry.getContext().getAssets(), ASSET_DIR, SCAN_DATA);
  }

  @Before
  public void setup() {
    EspressoUtils.adjustIdleWaitTimeout();
    EspressoUtils.templateSetup(TEMPLATE_NAME, true);

    //Enter "View Scanned Forms"
    onView(withId(R.id.ViewFormsButton)).perform(click());

    if (this.expectedEntries == null) {
      this.expectedEntries = getPhotoNames();
    }
  }

  @After
  public void cleanUp() {
    EspressoUtils.templateSetup(TEMPLATE_NAME, false);
  }

  @Test
  public void viewForms_numOfEntries() {
    onView(withId(android.R.id.list))
        .check(matches(ODKMatcher.withSize(this.expectedEntries.length)));
  }

  @Test
  public void viewForms_displayTemplateName() {
    for (int i = 0; i < this.expectedEntries.length; i++) {
      //Check if template name matches directory name
      onData(anything()).atPosition(i).onChildView(withId(R.id.templateName)).check(matches(withText(this
          .expectedEntries[i].split("_")[0])));
    }
  }

  @Test
  public void viewForms_displayEntriesPhotoStatus() {
    for (int i = 0; i < this.expectedEntries.length; i++) {
      String photoName = this.expectedEntries[i];

      int color = Color.RED;
      if (new File(ScanUtils.getJsonPath(photoName)).exists()) {
        color = Color.GREEN;
      } else if (new File(ScanUtils.getAlignedPhotoPath(photoName)).exists()) {
        color = Color.YELLOW;
      }

      onData(anything()).atPosition(i).onChildView(withId(R.id.photoStatus)).check(
          matches(ODKMatcher.withTextColor(color)));
    }
  }

  @Test
  public void viewForms_displayDateTime() {
    for (int i  = 0; i < this.expectedEntries.length; i++) {
      String dateTime = new Date(new File(ScanUtils.getPhotoPath(this.expectedEntries[i]))
          .lastModified()).toString();

      onData(anything()).atPosition(i).onChildView(withId(R.id.createdTime)).check(matches
          (withText(dateTime)));
    }
  }

  @Test
  public void viewForms_DisplayProcessedForm() {
    //Iterate through all dummy data
    //If item is green -> launch DisplayProcessedForm
    //If tem is yellow or red -> stay in ViewScannedForms
    for (int i = 0; i < this.expectedEntries.length; i++) {
      String photoName = this.expectedEntries[i];

      int color = Color.RED;
      if (new File(ScanUtils.getJsonPath(photoName)).exists()) {
        color = Color.GREEN;
      } else if (new File(ScanUtils.getAlignedPhotoPath(photoName)).exists()) {
        color = Color.YELLOW;
      }

      //Press ith item
      onData(anything()).atPosition(i).perform(click());

      //Land on display process form if entry is green, stay on form list if otherwise
      if (color == Color.GREEN) {
        onView(withId(R.id.saveBtn)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.transcribeBtn)).check(matches(isCompletelyDisplayed()));
        Espresso.pressBack();
      } else {
        intended(hasComponent(ViewScannedForms.class.getName()));
      }
    }
  }

  /**
   * Traverses "output" directory to find all expected entries of scanned forms
   *
   * @return A String[] of the entries
   */
  private static String[] getPhotoNames() {
    return new File(ScanUtils.getOutputDirPath()).list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (new File(dir, name)).isDirectory();
      }
    });
  }

  /**
   * Copies sourceDir from assets directory to ODKScan directory, recursively.
   *
   * @param assetMngr
   * @param sourceDir
   * @param targetDir
   * @throws IOException
   */
  private static void copyAssets(AssetManager assetMngr, String sourceDir, String targetDir) throws
      IOException {
    String[] fileList = assetMngr.list(sourceDir);

    for (String s : fileList) {
      String newDir = sourceDir + File.separator + s;
      String[] subDirFileList = assetMngr.list(newDir);

      if (subDirFileList.length == 0) {
        copyFile(assetMngr.open(newDir),
            new FileOutputStream(new File(targetDir + File.separator + newDir)));
      } else {
        new File(targetDir + File.separator + newDir).mkdir();
        copyAssets(assetMngr, newDir, targetDir);
      }
    }
  }

  /**
   * Deletes sourceDir of assets directory from ODKScan directory, recursively.
   *
   * @param assetMngr
   * @param sourceDir
   * @param targetDir
   * @throws IOException
   */
  private static void deleteAssets(AssetManager assetMngr, String sourceDir, String targetDir)
      throws IOException {
    String[] fileList = assetMngr.list(sourceDir);

    for (String s : fileList) {
      String newDir = sourceDir + File.separator + s;
      String[] subDirFileList = assetMngr.list(newDir);

      if (subDirFileList.length == 0) {
        new File(targetDir + File.separator + newDir).delete();
      } else {
        deleteAssets(assetMngr, newDir, targetDir);
        new File(targetDir + File.separator + newDir).delete();
      }
    }
  }

  /**
   * Copies an InputStream to an OutputStream
   *
   * @param in
   * @param out
   * @throws IOException
   */
  private static void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;

    while ((read = in.read(buffer)) > 0) {
      out.write(buffer, 0, read);
    }
  }
}