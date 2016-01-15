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

import org.json.JSONException;
import org.json.JSONObject;
import org.opendatakit.common.android.activities.BaseActivity;
import org.opendatakit.scan.R;
import org.opendatakit.scan.utils.ScanUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * This activity is mainly for error reporting
 * It displays a webview with the photo and a status message below it.
 * The extras bundle can have a "result" key containing some
 * JSON  with the errorMessage and whatever other data the cpp code wants to communicate.
 *
 * @author nathan
 */
public class DisplayStatusActivity extends BaseActivity {

  private static final String LOG_TAG = "ODKScan DisplayStatus";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    displayStatus(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent) {
    // TODO Auto-generated method stub
    super.onNewIntent(intent);
    displayStatus(intent);
  }

  protected void displayStatus(Intent intent) {
    try {
      setContentView(R.layout.status);

      Bundle extras = intent.getExtras();
      if (extras == null) {
        throw new Exception("Missing extras in the bundle.");
      }

      String photoName = extras.getString("photoName");
      Log.i(LOG_TAG, ScanUtils.getPhotoPath(photoName));
      ScanUtils.displayImageInWebView((WebView) findViewById(R.id.webview),
          ScanUtils.getPhotoPath(photoName));

      if (extras.containsKey("result")) {
        JSONObject result = new JSONObject();
        try {
          result = new JSONObject(extras.getString("result"));
        } catch (JSONException e) {
          result.put("errorMessage", "Unparsable JSON: " + extras.getString("result"));
        }

        String errorMessage = result.optString("errorMessage");

        if (errorMessage != null) {
          ((TextView) findViewById(R.id.statusMessage)).setText(errorMessage);
        }

      } else {
        ((TextView) findViewById(R.id.statusMessage)).setText(
            "Once this image is processed a notification will appear in your notification tray.");
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
