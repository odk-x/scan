/*
 * Copyright (C) 2012 University of Washington
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
package org.opendatakit.scan.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.bubblebot.jni.Processor;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opendatakit.scan.R;
import org.opendatakit.scan.activities.DisplayProcessedFormActivity;
import org.opendatakit.scan.activities.DisplayStatusActivity;
import org.opendatakit.scan.utils.ScanUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;

/**
 * This service invokes the cpp image processing code to run in the background.
 * It creates a notification that it's processing an image and updates it when it completes.
 */
public class ProcessFormsService extends IntentService {

  private static final String LOG_TAG = "ODKScan ProcessForms";
  private static final String NOTIFICATION_APP_TITLE = "ODK Scan";

  public ProcessFormsService() {
    super("ProcessFormsService");
  }

  @Override
  public void onHandleIntent(Intent intent) {
    Log.i(LOG_TAG, "Handling Intent to process form");

    // Retrieve input parameters
    final Bundle extras = intent.getExtras();
    if (extras == null) {
      Log.e(LOG_TAG, this.getString(R.string.error_background_exception));
      Log.e(LOG_TAG, this.getString(R.string.error_missing_intent));
      return;
    }
    final String[] templatePaths = extras.getStringArray("templatePaths");
    if (templatePaths == null) {
      Log.e(LOG_TAG, this.getString(R.string.error_background_exception));
      Log.e(LOG_TAG, this.getString(R.string.error_missing_template_paths));
      return;
    }
    String photoName = extras.getString("photoName", null);
    if (photoName == null) {
      Log.e(LOG_TAG, this.getString(R.string.error_missing_photo_name));
    }
    final int requestCode = extras.getInt("opCode", -1);
    if (requestCode == -1) {
      Log.e(LOG_TAG, this.getString(R.string.error_background_exception));
      Log.e(LOG_TAG, this.getString(R.string.error_missing_op_code));
      return;
    }
    String uriString = extras.getString("uri", null);
    Uri uri = null;
    if (uriString == null) {
      Log.d(LOG_TAG, this.getString(R.string.error_missing_uri));
    } else {
      uri = Uri.parse(uriString);
    }
    final boolean isRecursive = extras.getBoolean("isRecursive", false);

    // Switch on the types of form processing requests. No matter which is chosen we will create
    // our JSONConfig and run the processForm() method
    JSONObject configJSON;
    File destFile;
    try {
      switch (requestCode) {
      case R.integer.new_image:
        Log.i(LOG_TAG, this.getString(R.string.acquired_from_camera));

        if (photoName == null) {
          Log.e(LOG_TAG, this.getString(R.string.error_photo_name_not_found));
          return;
        }

        // Verify that the new picture exists
        destFile = new File(ScanUtils.getPhotoPath(photoName));
        if (!destFile.exists()) {
          Log.e(LOG_TAG, this.getString(R.string.error_file_creation));
          return;
        }
        extras.putString("photoName", photoName);

        try {
          configJSON = prepareConfig(templatePaths, photoName);
        } catch (Exception e) {
          Log.e(LOG_TAG, this.getString(R.string.error_failed_create_config));
          return;
        }

        processForm(extras, configJSON);
        break;

      case R.integer.existing_image:
        Log.d(LOG_TAG, this.getString(R.string.acquired_from_file_picker));

        if (uri == null) {
          Log.e(LOG_TAG, this.getString(R.string.error_uri_not_found));
          return;
        }

        // Verify that the new file exists and is an image
        Log.d(LOG_TAG, "File Uri Selected: " + uri.toString());
        File sourceFile = new File(uri.getPath());
        if (!sourceFile.exists() || !ScanUtils.imageFilter.accept(sourceFile, uri.toString())) {
          Log.d(LOG_TAG, this.getString(R.string.error_finding_file));
          return;
        }

        photoName = ScanUtils.setPhotoName(templatePaths);
        ScanUtils.prepareOutputDir(photoName);
        extras.putString("photoName", photoName);

        // Copy the new file into the Scan file system
        destFile = new File(ScanUtils.getPhotoPath(photoName));
        FileUtils.copyFile(sourceFile, destFile);

        try {
          configJSON = prepareConfig(templatePaths, photoName);
        } catch (Exception e) {
          Log.e(LOG_TAG, this.getString(R.string.error_failed_create_config));
          return;
        }

        processForm(extras, configJSON);
        break;

      case R.integer.image_directory:
        Log.d(LOG_TAG, this.getString(R.string.acquired_from_folder_picker));

        if (uri == null) {
          Log.e(LOG_TAG, this.getString(R.string.error_uri_not_found));
          return;
        }

        // Validate the directory
        File dir = new File(uri.getPath());
        if (!dir.exists() || !dir.isDirectory()) {
          Log.e(LOG_TAG, this.getString(R.string.error_finding_dir));
          return;
        }
        processImagesInFolder(dir, isRecursive, templatePaths, extras);

        break;

      default:
        Log.e(LOG_TAG, this.getString(R.string.error_acquire_bad_return));
        return;
      }
    } catch (Exception e) {
      Log.e(LOG_TAG, e.toString());
      return;
    }
  }

  /**
   * Prepare the JSONConfig parameter that passes arguments to the C++ layer
   *
   * @param templatePaths Paths to the possible form definitions and templates
   * @param photoName     Name and  path of the photo to process
   * @return The JSON to pass to the computer vision code
   * @throws Exception
   */
  private JSONObject prepareConfig(String[] templatePaths, String photoName) throws Exception {

    String inputPath = ScanUtils.getPhotoPath(photoName);
    String outputPath = ScanUtils.getOutputPath(photoName);

    //This is the configuration JSON passed into ODKScan-core
    //see: scan/bubblebot_lib/jni/ODKScan-core/processViaJSON.md
    JSONObject config = new JSONObject();
    config.put("trainingDataDirectory", ScanUtils.getTrainingExampleDirPath());
    config.put("trainingModelDirectory", ScanUtils.getTrainedModelDir());
    config.put("inputImage", inputPath);
    config.put("outputDirectory", outputPath);
    config.put("templatePaths", new JSONArray(Arrays.asList(templatePaths)));

    return config;
  }

  /**
   * Recursively process all images in a folder and then search subfolders
   */
  private void processImagesInFolder(File dir, boolean isRecursive, String[] templatePaths,
      Bundle extras) {
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }

    String photoName;

    // Process all the images in the folder
    for (File curr : dir.listFiles(ScanUtils.imageFilter)) {
      try {
        String pre_aligned_name = curr.getName();
        if (!pre_aligned_name.contains("_photo.jpg")) {
          Log.d(LOG_TAG, "Skipping image: " + pre_aligned_name);
          continue;
        }

        photoName = ScanUtils.setPhotoName(templatePaths);
        ScanUtils.prepareOutputDir(photoName);
        JSONObject configJSON = prepareConfig(templatePaths, photoName);
        extras.putString("photoName", photoName);

        Log.d(LOG_TAG, "Found pre-algned image: " + pre_aligned_name);
        String clientID = pre_aligned_name.substring(0, pre_aligned_name.indexOf("_photo"));

        // Copy the new file into the Scan file system
        File destFile = new File(ScanUtils.getPhotoPath(photoName));
        FileUtils.copyFile(curr, destFile);
        PrintWriter clientIDFile = new PrintWriter(
            ScanUtils.getOutputPath(photoName) + "/clientID.txt");
        clientIDFile.print(clientID);
        clientIDFile.close();

        Log.d(LOG_TAG, "Acquired form: " + ScanUtils.getPhotoPath(photoName));
        processForm(extras, configJSON);

      } catch (Exception e) {
        Log.e(LOG_TAG, "Error processing image: " + curr.getPath());
        Log.e(LOG_TAG, e.toString());
        continue;
      }
    }

    // If we are not recursing, we are done
    if (!isRecursive) {
      return;
    }

    // Recurse through each subdirectory
    for (File currDir : dir.listFiles(ScanUtils.subdirFilter)) {
      processImagesInFolder(currDir, isRecursive, templatePaths, extras);
    }

  }

  /**
   * The main function of this method is to pass arguments to the C++ layer and block while it
   * handles the actual processing and computer vision. See the bubblebot_lib tree for that code.
   * <p/>
   * But most of the code in this method is just sending notifications about the progress of the
   * processing
   *
   * @param extras     Parameters to be passed to the pending intent that is launched via
   *                   notification when the processing is finished
   * @param configJSON Parameters to be passed to the C++ layer
   */
  private void processForm(Bundle extras, JSONObject configJSON) {
    final Context context = getApplicationContext();
    final int notificationId = (int) (Math.random() * 9999999);
    final NotificationManager notificationManager = (NotificationManager) getSystemService(
        Context.NOTIFICATION_SERVICE);

    // Send a notification that we have begun processing the form
    Intent waitingIntent = new Intent(context, DisplayStatusActivity.class);
    waitingIntent.putExtras(extras);

    Notification beginNotification = new Notification.Builder(context)
        .setContentTitle(NOTIFICATION_APP_TITLE)
        .setSmallIcon(R.drawable.ic_schedule_white_24dp)
        .setContentText(this.getString(R.string.begin_processing))
        .setContentIntent(PendingIntent.getActivity(context, notificationId, waitingIntent, 0))
        .setWhen(System.currentTimeMillis()).build();
    notificationManager.notify(notificationId, beginNotification);

    // Send the config to the cpp, start processing and block until completion
    Log.i(LOG_TAG, "Using data = " + configJSON);
    final Processor mProcessor = new Processor();//ScanUtils.appFolder
    String jsonString = mProcessor.processViaJSON(configJSON.toString());

    // Check for errors in parsing
    JSONObject resultJSON = null;
    String errorMessage = "";
    try {
      resultJSON = new JSONObject(jsonString);
      errorMessage = resultJSON.optString("errorMessage");
    } catch (JSONException e) {
      Log.i(LOG_TAG, "Unparsable JSON: " + jsonString);
    }

    Notification resultNotification;
    if (errorMessage.length() == 0 && resultJSON != null) {
      // Construct a notification that we have finished processing the form
      Intent finishedIntent = new Intent(context, DisplayProcessedFormActivity.class);
      extras.putString("result", jsonString);
      extras.putString("templatePath", resultJSON.optString("templatePath"));
      finishedIntent.putExtras(extras);

      resultNotification = new Notification.Builder(context).setContentTitle(NOTIFICATION_APP_TITLE)
          .setSmallIcon(R.drawable.ic_done_white_24dp)
          .setContentText(this.getString(R.string.finished_processing))
          .setContentIntent(PendingIntent.getActivity(context, notificationId, finishedIntent, 0))
          .setWhen(System.currentTimeMillis()).build();

    } else {
      // Construct a notification that we had an error processing the form
      Intent errorIntent = new Intent(context, DisplayStatusActivity.class);
      extras.putString("result", jsonString);
      errorIntent.putExtras(extras);

      resultNotification = new Notification.Builder(context).setContentTitle(NOTIFICATION_APP_TITLE)
          .setSmallIcon(R.drawable.ic_error_white_24dp)
          .setContentText(this.getString(R.string.error_processing))
          .setContentIntent(PendingIntent.getActivity(context, notificationId, errorIntent, 0))
          .setWhen(System.currentTimeMillis()).build();
    }

    resultNotification.flags |= Notification.FLAG_AUTO_CANCEL;
    notificationManager.notify(notificationId, resultNotification);
  }

}