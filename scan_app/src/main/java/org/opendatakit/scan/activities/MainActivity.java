/*
 * Copyright (C) 2014 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.scan.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.opendatakit.activities.BaseActivity;
import org.opendatakit.activities.IInitResumeActivity;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.consts.RequestCodeConsts;
import org.opendatakit.dependencies.DependencyChecker;
import org.opendatakit.fragment.AboutMenuFragment;
import org.opendatakit.listener.DatabaseConnectionListener;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.scan.R;
import org.opendatakit.scan.application.Scan;
import org.opendatakit.scan.fragments.InitializationFragment;
import org.opendatakit.scan.fragments.InstructionsFragment;
import org.opendatakit.scan.fragments.MainMenuFragment;
import org.opendatakit.scan.fragments.ScanPreferencesFragment;
import org.opendatakit.scan.utils.ScanUtils;
import org.opendatakit.utilities.RuntimePermissionUtils;

public class MainActivity extends BaseActivity
    implements DatabaseConnectionListener, IInitResumeActivity {

  private static final String TAG = "ODKScan MainActivity";
  private static final String CURRENT_FRAGMENT = "currentFragment";

  public enum ScreenType {
    INITIALIZATION_SCREEN,
    MAIN_MENU_SCREEN,
    ABOUT_SCREEN,
    SETTINGS_SCREEN,
    INSTRUCTIONS_SCREEN
  };

  protected static final int REQUIRED_PERMISSIONS_REQ_CODE = 0;

  protected static final String[] REQUIRED_PERMISSIONS = new String[] {
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
  };

  protected Bundle savedInstanceState;

  /**
   * The active screen -- retained state
   */
  ScreenType activeScreenType = ScreenType.MAIN_MENU_SCREEN;

  /**
   * used to determine whether we need to change the menu (action bar)
   * because of a change in the active fragment.
   */
  private ScreenType lastMenuType = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.savedInstanceState = savedInstanceState;

    boolean dependable = DependencyChecker.checkDependencies(this);
    if (!dependable) { // dependencies missing
      return;
    }

    // 1. check if Services has the right permissions
    //      if not, launch Services
    // 2. check if this app has the right permissions

    if (!RuntimePermissionUtils.checkPackageAllPermission(
            this, IntentConsts.Services.PACKAGE_NAME, REQUIRED_PERMISSIONS)) {
      Intent launchIntent = new Intent();
      launchIntent.setComponent(
              new ComponentName(IntentConsts.Services.PACKAGE_NAME, IntentConsts.Services.MAIN_ACTIVITY));
      launchIntent.setAction(Intent.ACTION_VIEW);
      launchIntent.putExtra(IntentConsts.INTENT_KEY_PERMISSION_ONLY, true);

      startActivityForResult(launchIntent, RequestCodeConsts.RequestCodes.LAUNCH_MAIN_ACTIVITY);
    } else {
      if (!RuntimePermissionUtils.checkSelfAllPermission(this, REQUIRED_PERMISSIONS)) {
        ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS_REQ_CODE);
      } else {

        onCreateWithPermission(savedInstanceState);
      }
    }


  }

  protected void onCreateWithPermission(Bundle savedInstanceState) {

    this.setContentView(R.layout.activity_main_activity);

    if (savedInstanceState != null) {
      // if we are restoring, assume that initialization has already occurred.
      activeScreenType = ScreenType.valueOf(savedInstanceState.containsKey(CURRENT_FRAGMENT) ?
              savedInstanceState.getString(CURRENT_FRAGMENT) :
              activeScreenType.name());
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode != REQUIRED_PERMISSIONS_REQ_CODE) {
      return;
    }

    AlertDialog.Builder builder =
            RuntimePermissionUtils.createPermissionRationaleDialog(this, requestCode, permissions);

    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      onCreateWithPermission(savedInstanceState);
    } else {
      if (RuntimePermissionUtils.shouldShowAnyPermissionRationale(this, permissions)) {
        builder
                .setMessage(R.string.required_permission_rationale)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                  }
                })
                .show();
      } else {
        Toast
                .makeText(this, R.string.required_permission_perm_denied, Toast.LENGTH_LONG)
                .show();
        finish();
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode != RequestCodeConsts.RequestCodes.LAUNCH_MAIN_ACTIVITY) {
      return;
    }

    if (resultCode != Activity.RESULT_OK) {
      System.exit(0); // cannot properly shutdown without Services having proper permissions
    }

    if (!RuntimePermissionUtils.checkSelfAllPermission(this, REQUIRED_PERMISSIONS)) {
      ActivityCompat.requestPermissions(
              this, REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS_REQ_CODE);
    } else {
      onCreateWithPermission(savedInstanceState);
    }
  }


  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putString(CURRENT_FRAGMENT, activeScreenType.name());
  }

  @Override
  protected void onResume() {
    super.onResume();

    boolean dependable = DependencyChecker.checkDependencies(this);
    if (!dependable) { // dependencies missing
      return;
    }

    // 1. check if Services has the right permissions
    //      if not, launch Services
    // 2. check if this app has the right permissions

    if (!RuntimePermissionUtils.checkPackageAllPermission(
            this, IntentConsts.Services.PACKAGE_NAME, REQUIRED_PERMISSIONS)) {
      Intent launchIntent = new Intent();
      launchIntent.setComponent(
              new ComponentName(IntentConsts.Services.PACKAGE_NAME, IntentConsts.Services.MAIN_ACTIVITY));
      launchIntent.setAction(Intent.ACTION_VIEW);
      launchIntent.putExtra(IntentConsts.INTENT_KEY_PERMISSION_ONLY, true);

      startActivityForResult(launchIntent, RequestCodeConsts.RequestCodes.LAUNCH_MAIN_ACTIVITY);
    } else {
      if (!RuntimePermissionUtils.checkSelfAllPermission(this, REQUIRED_PERMISSIONS)) {
        ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS_REQ_CODE);
      } else {

        swapScreens(activeScreenType);
      }
    }


  }

  @Override
  public void onPostResume() {
    super.onPostResume();
    Scan.getInstance().establishDatabaseConnectionListener(this);
  }

  @Override
  public void databaseAvailable() {
    FragmentManager mgr = this.getFragmentManager();
    int idxLast = mgr.getBackStackEntryCount() - 1;
    if (idxLast >= 0) {
      BackStackEntry entry = mgr.getBackStackEntryAt(idxLast);
      Fragment newFragment = null;
      newFragment = mgr.findFragmentByTag(entry.getName());
      if (newFragment instanceof DatabaseConnectionListener) {
        ((DatabaseConnectionListener) newFragment).databaseAvailable();
      }
    }
  }

  @Override
  public void databaseUnavailable() {
    FragmentManager mgr = this.getFragmentManager();
    int idxLast = mgr.getBackStackEntryCount() - 1;
    if (idxLast >= 0) {
      BackStackEntry entry = mgr.getBackStackEntryAt(idxLast);
      Fragment newFragment = null;
      newFragment = mgr.findFragmentByTag(entry.getName());
      if (newFragment instanceof DatabaseConnectionListener) {
        ((DatabaseConnectionListener) newFragment).databaseUnavailable();
      }
    }
  }

  private void popBackStack() {
    FragmentManager mgr = getFragmentManager();
    int idxLast = mgr.getBackStackEntryCount() - 2;
    if (idxLast < 0) {
      Intent result = new Intent();
      this.setResult(RESULT_OK, result);
      finish();
    } else {
      BackStackEntry entry = mgr.getBackStackEntryAt(idxLast);
      swapScreens(ScreenType.valueOf(entry.getName()));
    }
  }

  @Override
  public void initializationCompleted() {
    popBackStack();
  }

  @Override
  public void onBackPressed() {
    popBackStack();
  }

  public ScreenType getCurrentScreenType() {
    return activeScreenType;
  }

  public void swapScreens(ScreenType newScreenType) {
    WebLogger.getLogger(getAppName()).i(TAG, "swapScreens: Transitioning from " +
        ((activeScreenType == null) ? "-none-" : activeScreenType.name()) +
        " to " + newScreenType.name());
    FragmentManager mgr = this.getFragmentManager();
    FragmentTransaction trans = null;
    Fragment newFragment = null;
    switch (newScreenType) {
    case MAIN_MENU_SCREEN:
      newFragment = mgr.findFragmentByTag(newScreenType.name());
      if (newFragment == null) {
        newFragment = new MainMenuFragment();
      }
      break;
    case ABOUT_SCREEN:
      newFragment = mgr.findFragmentByTag(newScreenType.name());
      if (newFragment == null) {
        newFragment = new AboutMenuFragment();
      }
      break;
    case INITIALIZATION_SCREEN:
      newFragment = mgr.findFragmentByTag(newScreenType.name());
      if (newFragment == null) {
        newFragment = new InitializationFragment();
      }
      break;
    case SETTINGS_SCREEN:
      newFragment = mgr.findFragmentByTag(newScreenType.name());
      if (newFragment == null) {
        newFragment = new ScanPreferencesFragment();
      }
      break;
    case INSTRUCTIONS_SCREEN:
      newFragment = mgr.findFragmentByTag(newScreenType.name());
      if (newFragment == null) {
        newFragment = new InstructionsFragment();
      }
      break;
    default:
      throw new IllegalStateException("Unexpected default case");
    }

    boolean matchingBackStackEntry = false;
    for (int i = 0; i < mgr.getBackStackEntryCount(); ++i) {
      BackStackEntry e = mgr.getBackStackEntryAt(i);
      WebLogger.getLogger(ScanUtils.getODKAppName())
          .i(TAG, "BackStackEntry[" + i + "] " + e.getName());
      if (e.getName().equals(newScreenType.name())) {
        matchingBackStackEntry = true;
      }
    }

    if (matchingBackStackEntry) {
      if (trans != null) {
        WebLogger.getLogger(ScanUtils.getODKAppName())
            .e(TAG, "Unexpected active transaction when popping " + "state!");
        trans = null;
      }
      // flush backward, to the screen we want to go back to
      activeScreenType = newScreenType;
      mgr.popBackStackImmediate(activeScreenType.name(), 0);
    } else {
      // add transaction to show the screen we want
      if (trans == null) {
        trans = mgr.beginTransaction();
      }
      activeScreenType = newScreenType;
      trans.replace(R.id.activity_main_activity, newFragment, activeScreenType.name());
      trans.addToBackStack(activeScreenType.name());
    }

    // and see if we should re-initialize...
    if ((activeScreenType != ScreenType.INITIALIZATION_SCREEN) && Scan.getInstance()
        .shouldRunInitializationTask(getAppName())) {
      WebLogger.getLogger(getAppName())
          .i(TAG, "swapToFragmentView -- calling clearRunInitializationTask");
      // and immediately clear the should-run flag...
      Scan.getInstance().clearRunInitializationTask(getAppName());
      // OK we should swap to the InitializationFragment view
      // this will skip the transition to whatever screen we were trying to
      // go to and will instead show the InitializationFragment view. We
      // restore to the desired screen via the setFragmentToShowNext()
      //
      // NOTE: this discards the uncommitted transaction.
      if (trans != null) {
        trans.commit();
      }
      swapScreens(ScreenType.INITIALIZATION_SCREEN);
    } else {
      if (trans != null) {
        trans.commit();
      }
      invalidateOptionsMenu();
    }
  }

  private void changeOptionsMenu(Menu menu) {
    MenuInflater menuInflater = this.getMenuInflater();

    if (activeScreenType == ScreenType.MAIN_MENU_SCREEN) {
      menuInflater.inflate(R.menu.scan_manager, menu);
    }
    lastMenuType = activeScreenType;

    ActionBar actionBar = getActionBar();
    actionBar.show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    changeOptionsMenu(menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    if (lastMenuType != activeScreenType) {
      changeOptionsMenu(menu);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;
    String appName = getAppName();
    WebLogger.getLogger(appName).d(TAG, "[onOptionsItemSelected] selecting an item");

    switch (item.getItemId()) {
    case R.id.menu_scan_about:
      swapScreens(ScreenType.ABOUT_SCREEN);
      return true;
    case R.id.menu_scan_instructions:
      swapScreens(ScreenType.INSTRUCTIONS_SCREEN);
      return true;
    case R.id.menu_scan_preferences:
      swapScreens(ScreenType.SETTINGS_SCREEN);
      return true;
    case R.id.processImage:
      intent = new Intent(getApplication(), AcquireFormImageActivity.class);
      intent.putExtra("acquisitionMethod", R.integer.pick_file);
      intent.putExtra("intentRequestCode", R.integer.scan_main_menu);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      return true;
    case R.id.processFolder:
      intent = new Intent(getApplication(), AcquireFormImageActivity.class);
      intent.putExtra("acquisitionMethod", R.integer.pick_directory);
      intent.putExtra("intentRequestCode", R.integer.scan_main_menu);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public String getAppName() {
    return ScanUtils.getODKAppName();
  }

}
