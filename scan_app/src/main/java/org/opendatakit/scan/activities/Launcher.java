package org.opendatakit.scan.activities;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import org.opendatakit.activities.BaseLauncherActivity;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.utilities.ODKFileUtils;

import java.util.Collections;

public class Launcher extends BaseLauncherActivity {

  /**
   * Used for logging
   */
  @SuppressWarnings("unused")
  private static final String TAG = Launcher.class.getName();

  // the app name
  private String mAppName;

  @Override
  public String getAppName() {
    return mAppName;
  }

  protected void setAppSpecificPerms(){
    Collections.addAll(appSpecific_Required_Permissions, new String[] {Manifest.permission.CAMERA});
  }

  /**
   * Restores saved state if possible. Then it looks at the table name that it was launched for
   * in the intent, and verifies that it matches the URI that tables was launched with. If they
   * match, it sets the app name based on the URI. It then makes sure that the device has the
   * right folders created, has the right dependencies installed (services and io file manager),
   * and if everything is good, it makes an intent to launch the TableManager to the requested table
   *
   * @param savedInstanceState the bundle packed by onSaveInstanceState
   */
  @Override
  public void onCreateWithPermission(Bundle savedInstanceState) {
    Intent intent = this.getIntent();
    Bundle extras = intent.getExtras();

    this.mAppName = extras == null ? null : extras.getString(IntentConsts.INTENT_KEY_APP_NAME);
    if (this.mAppName == null) {
      this.mAppName = ODKFileUtils.getOdkDefaultAppName();
    }

    // ensuring directories exist
    ODKFileUtils.verifyExternalStorageAvailability();
    ODKFileUtils.assertDirectoryStructure(this.mAppName);

    Intent i = new Intent(this, MainActivity.class);

    if (extras != null) {
      i.putExtras(extras);
    }

    i.putExtra(IntentConsts.INTENT_KEY_APP_NAME, this.mAppName);
    startActivity(i);
    finish();
  }

  /**
   * We have to have this method because we implement DatabaseConnectionListener
   */
  @Override
  public void databaseAvailable() {
  }

  /**
   * We have to have this method because we implement DatabaseConnectionListener
   */
  @Override
  public void databaseUnavailable() {
  }


}
