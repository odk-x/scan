/*
 * Copyright (C) 2014 University of Washington
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

package org.opendatakit.scan.android.activities;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import android.app.*;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.*;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import org.opendatakit.common.android.activities.BaseActivity;
import org.opendatakit.common.android.activities.IAppAwareActivity;
import org.opendatakit.common.android.activities.IInitResumeActivity;
import org.opendatakit.common.android.activities.ODKActivity;
import org.opendatakit.common.android.utilities.ODKFileUtils;
import org.opendatakit.common.android.utilities.WebLogger;
import org.opendatakit.common.android.views.ICallbackFragment;
import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.application.Scan;
import org.opendatakit.scan.android.fragments.InitializationFragment;
import org.opendatakit.scan.android.utils.ScanUtils;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainMenuActivity extends BaseActivity implements IInitResumeActivity {
  private static final String LOG_TAG = "ODKScan";
  private Fragment mInitFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main_menu); // Setup the UI

    SharedPreferences settings = PreferenceManager
        .getDefaultSharedPreferences(getApplicationContext());

    try {
      PackageInfo packInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      {
        // dynamically construct the main screen version string
        TextView mainMenuMessageLabel = (TextView) findViewById(R.id.version_display);
        mainMenuMessageLabel.setText("version:\n" + packInfo.versionName);
      }
      // check version and run setup if needed
      int storedVersionCode = settings.getInt("version", 0);
      int appVersionCode = packInfo.versionCode;
      if (appVersionCode == 0 || storedVersionCode < appVersionCode) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("version", appVersionCode);
        editor.commit();
      }
    } catch (Exception e) {
      // Display an error dialog if something goes wrong.
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(e.toString()).setCancelable(false)
          .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
              finish();
            }
          });
      AlertDialog alert = builder.create();
      alert.show();
    }

    hookupButtonHandlers();
    updateTemplateText();
  }

  private void hookupButtonHandlers() {

    Button scanForm = (Button) findViewById(R.id.ScanButton);
    scanForm.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(getApplication(), AcquireFormImageActivity.class);
        intent.putExtra("acquisitionMethod", R.integer.take_picture);
        startActivity(intent);
      }
    });

    Button processImage = (Button) findViewById(R.id.ProcessImageButton);
    processImage.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(getApplication(), AcquireFormImageActivity.class);
        intent.putExtra("acquisitionMethod", R.integer.pick_file);
        startActivity(intent);
      }
    });

    Button processFolder = (Button) findViewById(R.id.ProcessFolderButton);
    processFolder.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(getApplication(), AcquireFormImageActivity.class);
        intent.putExtra("acquisitionMethod", R.integer.pick_directory);
        startActivity(intent);
      }
    });

    Button viewForms = (Button) findViewById(R.id.ViewFormsButton);
    viewForms.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(getApplication(), ViewScannedForms.class);
        startActivity(intent);
      }
    });

    Button instructions = (Button) findViewById(R.id.InstructionsButton);
    instructions.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(getApplication(), InstructionsActivity.class);
        startActivity(intent);
      }
    });

    Button settingsButton = (Button) findViewById(R.id.SettingsButton);
    settingsButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(getApplication(), AppSettingsActivity.class);
        startActivity(intent);
      }
    });
  }

  private void updateTemplateText() {
    SharedPreferences settings = PreferenceManager
        .getDefaultSharedPreferences(getApplicationContext());

    TextView templateText = (TextView) findViewById(R.id.TemplateText);

    // If no template is selected, present a warning
    if (!settings.contains("select_templates")) {
      templateText.setText(R.string.no_template);
      templateText.setTextColor(Color.RED);
      return;
    }

    Set<String> templatePaths = settings.getStringSet("select_templates", null);
    if (templatePaths == null || templatePaths.isEmpty()) {
      templateText.setText(R.string.no_template);
      templateText.setTextColor(Color.RED);
      return;
    }

    String templateName = "";
    for (String path : templatePaths) {
      String[] parts = path.split("/");
      templateName += parts[parts.length - 1] + ", ";
    }
    // Remove the trailing comma and space
    templateName = templateName.substring(0, templateName.length() - 2);

    String newScanText = String.format(getString(R.string.template_selected), templateName);

    templateText.setText(Html.fromHtml(newScanText));
    templateText.setTextColor(Color.BLACK);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateTemplateText();
  }

  @Override
  public void onBackPressed() {
    // This override is used in order to avoid going back to the
    // DisplayProcessedFormActivity activity.
    Intent setIntent = new Intent(Intent.ACTION_MAIN);
    setIntent.addCategory(Intent.CATEGORY_HOME);
    setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(setIntent);
  }

  private void initializeFileSystem() {
    /* TODO: Check if we need to initialize
    if (!Scan.getInstance().shouldRunInitializationTask(getAppName())) {
      return;
    }
    */

    // Ensuring ODK directories exist
    ODKFileUtils.verifyExternalStorageAvailability();

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    mInitFragment = new InitializationFragment();
    fragmentTransaction.add(R.id.fragment_container, mInitFragment).commit();

    ODKFileUtils.assertDirectoryStructure(ScanUtils.getODKAppName());

    /* TODO: Prevent initializaiton rerun
    Scan.getInstance().clearRunInitializationTask(getAppName());
    */

  }

  @Override
  public void databaseAvailable() {
    initializeFileSystem();
  }

  @Override
  public void databaseUnavailable() {
    initializeFileSystem();
  }

  @Override
  public void onPostResume() {
    super.onPostResume();
    Scan.getInstance().establishDatabaseConnectionListener(this);
  }

  @Override
  public void initializationCompleted() {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.remove(mInitFragment).commit();

    findViewById(R.id.fragment_container).setVisibility(View.GONE);
  }

  private void changeOptionsMenu(Menu menu) {
    MenuInflater menuInflater = this.getMenuInflater();

    menuInflater.inflate(R.menu.scan_manager, menu);

    ActionBar actionBar = getActionBar();
    actionBar.show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    changeOptionsMenu(menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public String getAppName() {
    return ScanUtils.getODKAppName();
  }

}