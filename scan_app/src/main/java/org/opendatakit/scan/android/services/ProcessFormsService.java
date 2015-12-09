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
package org.opendatakit.scan.android.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.bubblebot.jni.Processor;
import org.json.JSONException;
import org.json.JSONObject;
import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.activities.DisplayProcessedFormActivity;
import org.opendatakit.scan.android.activities.DisplayStatusActivity;

/**
 * This service invokes the cpp image processing code to run in the background.
 * It creates a notification that it's processing an image and updates it when it completes.
 */
public class ProcessFormsService extends IntentService {

  private static final String LOG_TAG = "ODKScan";
  private static final String NOTIFICATION_APP_TITLE = "ODK Scan";

  public ProcessFormsService() {
    super("ProcessFormsService");
  }

  @Override
  public void onHandleIntent(Intent intent) {

    final Context context = getApplicationContext();
    final int notificationId = (int) (Math.random() * 9999999);
    final NotificationManager notificationManager = (NotificationManager) getSystemService(
        Context.NOTIFICATION_SERVICE);

    // Retrieve input parameters
    final Bundle extras = intent.getExtras();
    if (extras == null) {
      Log.e(LOG_TAG, this.getString(R.string.error_background_exception));
      Log.e(LOG_TAG, this.getString(R.string.error_missing_intent));
    }
    final String configJSON = extras.getString("config");
    if (configJSON == null) {
      Log.e(LOG_TAG, this.getString(R.string.error_background_exception));
      Log.e(LOG_TAG, this.getString(R.string.error_missing_config));
    }

    // Send a notification that we have begun processing the form
    Intent waitingIntent = new Intent(context, DisplayStatusActivity.class);
    waitingIntent.putExtras(extras);

    Notification beginNotification = new Notification.Builder(context)
        .setContentTitle(NOTIFICATION_APP_TITLE)
        .setSmallIcon(android.R.drawable.status_bar_item_background)
        .setContentText(this.getString(R.string.begin_processing))
        .setContentIntent(PendingIntent.getActivity(context, 0, waitingIntent, 0))
        .setWhen(System.currentTimeMillis()).build();
    notificationManager.notify(notificationId, beginNotification);

    // Send the config to the cpp, start processing and block until completion
    Log.i(LOG_TAG, "Using data = " + configJSON);
    final Processor mProcessor = new Processor();//ScanUtils.appFolder
    String jsonString = mProcessor.processViaJSON(configJSON);

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
          .setSmallIcon(android.R.drawable.stat_notify_more)
          .setContentText(this.getString(R.string.finished_processing))
          .setContentIntent(PendingIntent.getActivity(context, notificationId, finishedIntent, 0))
          .setWhen(System.currentTimeMillis()).build();

    } else {
      // Construct a notification that we had an error processing the form
      Intent errorIntent = new Intent(context, DisplayStatusActivity.class);
      extras.putString("result", jsonString);
      errorIntent.putExtras(extras);

      resultNotification = new Notification.Builder(context).setContentTitle(NOTIFICATION_APP_TITLE)
          .setSmallIcon(android.R.drawable.stat_notify_error)
          .setContentText(this.getString(R.string.error_processing))
          .setContentIntent(PendingIntent.getActivity(context, notificationId, errorIntent, 0))
          .setWhen(System.currentTimeMillis()).build();
    }

    resultNotification.flags |= Notification.FLAG_AUTO_CANCEL;
    notificationManager.notify(notificationId, resultNotification);

  }

}