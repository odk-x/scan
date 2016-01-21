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

package org.opendatakit.scan.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONObject;
import org.opendatakit.common.android.activities.BaseActivity;
import org.opendatakit.scan.utils.JSONUtils;
import org.opendatakit.scan.R;
import org.opendatakit.scan.utils.ScanUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * This activity displays the image of a processed form.
 */
public class DisplayProcessedFormActivity extends BaseActivity {

  private static final String LOG_TAG = "ODKScan DisplayForm";

  public enum RequestCode {
    SAVE,
    TRANSCRIBE;

    public static RequestCode fromInt(int toConvert) {
      switch (toConvert) {
      case 0:
        return SAVE;
      case 1:
        return TRANSCRIBE;
      default:
        return null;
      }
    }

    public static int toInt(RequestCode toConvert) {
      switch (toConvert) {
      case SAVE:
        return 0;
      case TRANSCRIBE:
        return 1;
      default:
        return -1;
      }
    }
  }

  private String photoName;
  private String templatePath;
  WebView myWebView;

  private Bundle extras;

  private boolean morePagesToScan = false;

  private Intent surveyIntent;

  private Intent tablesIntent;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try {
      setContentView(R.layout.processed_form);

      extras = getIntent().getExtras();
      if (extras == null || !extras.containsKey("photoName")) {
        throw new Exception(
            "This activity must be lauched with a photoName specified in the extras.");
      }
      photoName = extras.getString("photoName");

      templatePath = extras.getString("templatePath");
      if (templatePath == null) {
        //Since the template path is not in the extras we'll try to get it from the json output.
        JSONObject outputJSON = JSONUtils
            .parseFileToJSONObject(ScanUtils.getOutputPath(photoName) + "output.json");
        outputJSON.getString("templatePath");
        //templatePath = outputJSON.getString("templatePath");
      }

      Log.i(LOG_TAG, "Enabling buttons and attaching handlers...");

      //How multi-page forms are handled:
      //If there is a nextPage directory in the template directory
      //Scan will assume it is processing a multipage form
      //where the template for the next page is in the nextPage directory.
      //In the extras, the prevTemplatePaths and prevPhotoPaths arrays are passed through
      //subsequent invocations of the Scan activities in order to store
      //so they can be combined into a single xform on the final invocation.
      final File nextPageTemplatePath = new File(templatePath, "nextPage");
      morePagesToScan = nextPageTemplatePath
          .exists(); //TODO: This doesn't work on the "View Scanned forms" path.
      if (morePagesToScan) {
        Button nextPage = (Button) findViewById(R.id.nextPageBtn);
        nextPage.setVisibility(View.VISIBLE);
        nextPage.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            Intent intent = new Intent(getApplication(), AcquireFormImageActivity.class);
            intent.putExtra("acquisitionMethod", R.integer.take_picture);
            String[] templatePaths = { nextPageTemplatePath.toString() };
            intent.putExtra("templatePaths", templatePaths);
            ArrayList<String> prevTemplatePaths = extras.getStringArrayList("prevTemplatePaths");
            ArrayList<String> prevPhotoNames = extras.getStringArrayList("prevPhotoNames");
            if (prevTemplatePaths == null || prevPhotoNames == null) {
              intent.putStringArrayListExtra("prevTemplatePaths",
                  new ArrayList<String>(Arrays.asList(templatePath)));
              intent.putStringArrayListExtra("prevPhotoNames",
                  new ArrayList<String>(Arrays.asList(photoName)));
            } else {
              prevTemplatePaths.add(templatePath);
              prevPhotoNames.add(photoName);
              //Can I modify the array list in extras or do I need to do a put?
            }
            if (prevPhotoNames != null) {
              intent.putExtra("photoName", photoName
                  .replace("\\(page[0-9]+\\)", "(page" + (prevPhotoNames.size() + 1) + ")"));
            } else {
              intent.putExtra("photoName", photoName + "(page2)");
            }
            startActivity(intent);
            finish();
          }
        });
      } else {
        LinearLayout layout = (LinearLayout) findViewById(R.id.save_transcribe);
        layout.setVisibility(View.VISIBLE);

				/* Uncomment for Tables
        tablesIntent = makeTablesIntent();
				*/
        surveyIntent = makeSurveyIntent();

        Button saveData = (Button) findViewById(R.id.saveBtn);
        saveData.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            Log.i(LOG_TAG, "Using template: " + templatePath);
            /* Uncomment if you want Scan to launch Tables
            //TODO: tablesIntent is still null if Tables not installed.
						if(isTablesInstalled) {
							if(tablesIntent.getData() == null) {
								exportToTables(RequestCode.SAVE);
							}
						}
						*/
            //TODO: surveyIntent is still null if Survey not installed.
            if (isSurveyInstalled()) {
              if (surveyIntent.getData() == null) {
                exportToSurvey(RequestCode.SAVE);
              }
            }
          }
        });

        Button transcribeData = (Button) findViewById(R.id.transcribeBtn);
        transcribeData.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            Log.i(LOG_TAG, "Using template: " + templatePath);

						/* Uncomment to Launch Tables
            //TODO: tablesIntent is still null if Tables not installed.
						if(isTablesInstalled) {
							if(tablesIntent.getData() == null) {
								exportToTables(RequestCode.TRANSCRIBE);
							} else {
								//The scan data has already been exported
								//so just start Tables.
								boolean tablesInstalled = isTablesInstalled(tablesIntent);
								if (tablesInstalled) {
									startActivity(tablesIntent);
								}
							}
						}
						*/
            // Launch Survey
            //TODO: surveyIntent is still null if Survey not installed.
            if (isSurveyInstalled()) {
              if (surveyIntent.getData() == null) {
                exportToSurvey(RequestCode.TRANSCRIBE);
              } else {
                //The scan data has already been exported
                //so just start Survey.
                startActivity(surveyIntent);
              }
            }
          }
        });
      }

      ScanUtils.displayImageInWebView((WebView) findViewById(R.id.webview2),
          ScanUtils.getMarkedupPhotoPath(photoName));

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
  }

  /**
   * Creates an intent for launching survey.
   * May return null if survey is not installed.
   *
   * @return
   */
  public Intent makeSurveyIntent() {
    // Initialize the intent that will start Survey.
    Intent intent = getPackageManager().getLaunchIntentForPackage("org.opendatakit.survey.android");

    if (intent != null) {
      intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

      intent.setAction(Intent.ACTION_EDIT);
      intent.addCategory(Intent.CATEGORY_DEFAULT);

      //Start indicates that the form should be launched from the first question
      //rather than the prompt list.
      // Not sure if this start parameter is still necessary in Survey
      intent.putExtra("start", true);
    }

    return intent;
  }

  /**
   * Creates an intent for launching tables.
   * May return null if tables is not installed.
   *
   * @return
   */
  public Intent makeTablesIntent() {
    // Initialize the intent that will start Tables.
    // final String TABLE_DISPLAY_ACTIVITY =
    //     "org.opendatakit.tables.activities.TableDisplayActivity";
    // Intent intent = new Intent(TABLE_DISPLAY_ACTIVITY);

    //Old Way
    Intent intent = getPackageManager().getLaunchIntentForPackage("org.opendatakit.tables");

    if (intent != null) {
      intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.setAction(Intent.ACTION_VIEW);
    }

    return intent;
  }

  /**
   * Checks if Survey is installed
   *
   * @return
   */
  public Boolean isSurveyInstalled() {
    //intent is null when Survey is not installed
    if (surveyIntent == null) {
      // ////////////
      Log.i(LOG_TAG, "Survey is not installed.");
      // ////////////
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("ODK Survey was not found on this device.").setCancelable(false)
          // Take this out until Survey is available on the Google Play Store
          //.setPositiveButton("Install it.", new DialogInterface.OnClickListener() {
          //	public void onClick(DialogInterface dialog,
          //			int id) {
          //		Intent goToMarket = new Intent(Intent.ACTION_VIEW)
          //	    	.setData(Uri.parse("market://details?id=org.odk.survey.android"));
          //		startActivity(goToMarket);
          //		dialog.cancel();
          //	}
          //})
          .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
          });
      AlertDialog alert = builder.create();
      alert.show();
    }

    return surveyIntent != null;
  }

  /**
   * Checks if Tables is installed
   *
   * @return
   */
  public Boolean isTablesInstalled() {
    //intent is null when Tables is not installed
    if (tablesIntent == null) {
      // ////////////
      Log.i(LOG_TAG, "Tables is not installed.");
      // ////////////
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("ODK Tables was not found on this device.").setCancelable(false)
          // Take this out until Tables is available on the Google Play Store
          //.setPositiveButton("Install it.", new DialogInterface.OnClickListener() {
          //	public void onClick(DialogInterface dialog,
          //			int id) {
          //		Intent goToMarket = new Intent(Intent.ACTION_VIEW)
          //	    	.setData(Uri.parse("market://details?id=org.odk.tables.android"));
          //		startActivity(goToMarket);
          //		dialog.cancel();
          //	}
          //})
          .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
          });
      AlertDialog alert = builder.create();
      alert.show();
    }

    return tablesIntent != null;
  }

  @Override
  protected Dialog onCreateDialog(int id, Bundle args) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Exporting...");
    builder.setCancelable(false);
    return builder.create();
  }

  /**
   * Exports the Scan JSON data to Survey.
   * If the requestCode is 3 Survey will be launched
   * after the export activity returns a result.
   *
   * @param requestCode
   */
  public void exportToSurvey(RequestCode requestCode) {
    // TODO: showDialog(0);
    Intent createInstanceIntent = new Intent(getApplication(), JSON2SurveyJSONActivity.class);
    createInstanceIntent.putExtras(extras);
    createInstanceIntent.putExtra("templatePath", templatePath);
    createInstanceIntent.putExtra("photoName", photoName);
    startActivityForResult(createInstanceIntent, RequestCode.toInt(requestCode));
  }

  /**
   * Exports the Scan JSON data to Tables.
   * If the requestCode is 3 Tables will be launched
   * after the export activity returns a result.
   *
   * @param requestCode
   */
  public void exportToTables(RequestCode requestCode) {
    // TODO: showDialog(0);
    Intent createInstanceIntent = new Intent(getApplication(), JSON2SurveyJSONActivity.class);
    createInstanceIntent.putExtras(extras);
    createInstanceIntent.putExtra("templatePath", templatePath);
    createInstanceIntent.putExtra("photoName", photoName);
    startActivityForResult(createInstanceIntent, RequestCode.toInt(requestCode));
  }

  @Override
  protected void onActivityResult(int requestCodeInt, int resultCode, Intent data) {
    // TODO: dismissDialog(0);
    RequestCode requestCode = RequestCode.fromInt(requestCodeInt);

    // Only launch intents if the result was ok
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == RequestCode.SAVE || requestCode == RequestCode.TRANSCRIBE) {
        Button saveData = (Button) findViewById(R.id.saveBtn);
        saveData.setEnabled(false);
        saveData.setText("saved");
        /* Uncomment to launch tables
        tablesIntent.putExtras(data);
				tablesIntent.setData(data.getData()); */
        // Launch survey
        surveyIntent.putExtras(data);
        surveyIntent.setData(data.getData());
      }

      if (requestCode == RequestCode.TRANSCRIBE) {
        //dismissDialog(1);

        //No need to check for Survey or Tables,
        //S or T is always installed if RequestCode
        //is TRANSCRIBE or SAVE

				/* Uncomment to Launch tables
				Log.i(LOG_TAG, "Starting Tables...");
				startActivity(tablesIntent);
				*/
        // Launch survey
        Log.i(LOG_TAG, "Starting Survey...");
        startActivity(surveyIntent);
      }
    }

    super.onActivityResult(requestCodeInt, resultCode, data);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    //if(morePagesToScan) return false;
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.display_processed_form_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    Intent intent;
    int itemId = item.getItemId();
    if (itemId == R.id.scanNewForm) {
      intent = new Intent(getApplication(), AcquireFormImageActivity.class);
      intent.putExtra("acquisitionMethod", R.integer.take_picture);
      intent.putExtra("intentRequestCode", R.integer.scan_main_menu);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
      return true;
    } else if (itemId == R.id.processImage) {
      intent = new Intent(getApplication(), AcquireFormImageActivity.class);
      intent.putExtra("acquisitionMethod", R.integer.pick_file);
      intent.putExtra("intentRequestCode", R.integer.scan_main_menu);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
      return true;
    } else if (itemId == R.id.processFolder) {
      intent = new Intent(getApplication(), AcquireFormImageActivity.class);
      intent.putExtra("acquisitionMethod", R.integer.pick_directory);
      intent.putExtra("intentRequestCode", R.integer.scan_main_menu);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      finish();
      return true;
    } else if (itemId == R.id.startOver) {
      intent = new Intent(getApplication(), MainActivity.class);
      startActivity(intent);
      finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  public void databaseAvailable() {
    // TODO Auto-generated method stub

  }

  public void databaseUnavailable() {
    // TODO Auto-generated method stub

  }

  public String getAppName() {
    return ScanUtils.getODKAppName();
  }
}
