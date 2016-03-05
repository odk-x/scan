package org.opendatakit.scan;

import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.scan.activities.MainActivity;
import org.opendatakit.scan.utils.ScanUtils;
import org.opendatakit.scan.utils.EspressoUtils;

import java.io.File;
import java.util.regex.Pattern;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.matcher.ViewMatchers.*;

/**
 * This class contains UI tests regarding AcquireFormImageActivity that doesn't
 * use intent stubbing.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AcquireFormImageTest2 {
  private static final int WAIT_TIME = 5000; //5 secs
  private static final int PROCESS_WAIT_TIME = 120000; //2 min
  private static final String SHUTTER_DESC = "Shutter";
  private static final String CHECK_DESC = "Done";
  private static final String PHOTO_INTENT_NAME = "IMAGE_CAPTURE";
  private static final String TEMPLATE_NAME = "example";
  private static final String AOSP_CAMERA_PKGNAME = "com.android.camera";
  private static final String AOSP_CAMERA_CHECK_ID = "btn_done";

  private UiDevice mDevice;
  private boolean runCleanup;

  @Rule
  public IntentsTestRule<MainActivity> mActivityRule = new IntentsTestRule<>(MainActivity.class);

  @Before
  public void setup() {
    EspressoUtils.adjustIdleWaitTimeout();

    this.mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    this.runCleanup = true;
  }

  @After
  public void cleanup() {
  }

  @Test
  public void scanNewForm() throws InterruptedException {
    this.runCleanup = false;

    while (true) {
      //Press Scan New Form
      onView(withId(R.id.ScanButton)).perform(click());

      Thread.sleep(WAIT_TIME); //Wait for camera to be ready

      //Press shutter
      mDevice.findObject(By.descStartsWith(SHUTTER_DESC)).click();
      Thread.sleep(WAIT_TIME); //Wait for image to be captured

      //Choose image
      if (mDevice.hasObject(By.res(AOSP_CAMERA_PKGNAME, AOSP_CAMERA_CHECK_ID))) {
        mDevice.findObject(By.res(AOSP_CAMERA_PKGNAME, AOSP_CAMERA_CHECK_ID)).click();
      } else {
        mDevice.findObject(By.desc(CHECK_DESC)).click();
      }
    }
  }

  private void deleteDir(File dir) {
    if (dir.isDirectory()) {
      for (File f : dir.listFiles()) {
        deleteDir(f);
      }
    }
    dir.delete();
  }
}
