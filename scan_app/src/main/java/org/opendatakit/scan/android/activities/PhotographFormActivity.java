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
package org.opendatakit.scan.android.activities;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.droidparts.preference.MultiSelectListPreference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opendatakit.common.android.activities.BaseActivity;
import org.opendatakit.scan.android.services.ProcessFormService;
import org.opendatakit.scan.android.utils.ScanUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
/**
 * PhotographFormActivity launches the Android camera app to capture a form image.
 * It also creates a directory for data about the form to be stored.
 **/
public class PhotographFormActivity extends BaseActivity {
	private static final String LOG_TAG = "ODKScan";
	private static final int TAKE_PICTURE = 12346789;
	private String photoName;
    private static final DateFormat COLLECT_INSTANCE_NAME_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss");
    private Intent processPhoto;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try{
			photoName = "taken_" + COLLECT_INSTANCE_NAME_DATE_FORMAT.format(new Date());

			Bundle extras = getIntent().getExtras();
			if(extras == null) {
				extras = new Bundle();
				Log.i("SCAN", "No bundle");
			}
			if(extras.containsKey("photoName")){
				photoName = extras.getString("photoName");
			}
			if(!extras.containsKey("templatePaths")){
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext());
				Log.d(LOG_TAG, "Captured photo: ");
				String[] templatePaths = MultiSelectListPreference
						.fromPersistedPreferenceValue(settings.getString(
								"select_templates", ""));
				extras.putStringArray("templatePaths", templatePaths);

				if (templatePaths.length > 0)
				{
					String[] parts = templatePaths[0].split("/");
					String templateName = parts[parts.length-1];
					photoName = templateName + "_" + COLLECT_INSTANCE_NAME_DATE_FORMAT.format(new Date());
				}
			}

		String inputPath = ScanUtils.getPhotoPath(photoName);
		String outputPath = ScanUtils.getOutputPath(photoName);
		String[] templatePaths = extras.getStringArray("templatePaths");

		//This is the configuration JSON passed into ODKScan-core
		//see: https://github.com/UW-ICTD/ODKScan-core/blob/master/processViaJSON.md
			JSONObject config = new JSONObject();
	        config.put("trainingDataDirectory", ScanUtils.getTrainingExampleDirPath());
	        config.put("inputImage", inputPath);
	        config.put("outputDirectory", outputPath);
	        config.put("templatePaths", new JSONArray(Arrays.asList(templatePaths)));

			processPhoto = new Intent(this, ProcessFormService.class);
			processPhoto.putExtras(extras);
			processPhoto.putExtra("photoName", photoName);
	        processPhoto.putExtra("config", config.toString());

			//Try to create an output folder
			boolean createSuccess = new File(outputPath).mkdirs();
			if(!createSuccess){
				new Exception("Could not create output folder [" +
						outputPath +
						"].\n There may be a problem with the device's storage.");
			}
			//Create an output directory for the segments
			boolean segDirSuccess = new File(outputPath, "segments").mkdirs();
			if(!segDirSuccess){
				new Exception("Could not create output folder for segments.");
			}

			Uri imageUri = Uri.fromFile( new File(ScanUtils.getPhotoPath(photoName)) );
			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivityForResult(intent, TAKE_PICTURE);

		} catch (Exception e) {
			//Display an error dialog if something goes wrong.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(e.toString())
			.setCancelable(false)
			.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					setResult(RESULT_CANCELED);
					finish();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(LOG_TAG, "Camera activity result: " + requestCode);
		if (requestCode == TAKE_PICTURE) {
			finishActivity(TAKE_PICTURE);
			if (resultCode == Activity.RESULT_OK) {

				File destFile = new File(ScanUtils.getPhotoPath(photoName));
				if( !destFile.exists() ) {
					Toast.makeText(getApplicationContext(), "Could not save photo.", Toast.LENGTH_LONG).show();
					finish();
					return;
				}

				Log.d(LOG_TAG, "Captured photo: " + ScanUtils.getPhotoPath(photoName));

				startService(processPhoto);
			}
			else{
				if(resultCode == Activity.RESULT_FIRST_USER){
					Log.d(LOG_TAG, "First User");
				}
				else if(resultCode == Activity.RESULT_CANCELED){
					Log.d(LOG_TAG, "Canceled");
				}
			}
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		//Try to remove the forms directory if the photo couldn't be captured:
		//Note: this won't delete the folder if it has any files in it.
		new File(ScanUtils.getOutputPath(photoName)).delete();
		super.onDestroy();
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