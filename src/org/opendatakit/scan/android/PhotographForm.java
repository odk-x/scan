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
package org.opendatakit.scan.android;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.droidparts.preference.MultiSelectListPreference;

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
 * PhotographForm launches the Android camera app to capture a form image.
 * It also creates a directory for data about the form to be stored.
 **/
public class PhotographForm extends Activity {
	private static final String LOG_TAG = "ODKScan";
	private static final int TAKE_PICTURE = 12346789;
	private String photoName;
    private static final DateFormat COLLECT_INSTANCE_NAME_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss");
    private Intent afterPhotoTaken;
	private Date activityCreateTime;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activityCreateTime = new Date();
		photoName = "taken_" + COLLECT_INSTANCE_NAME_DATE_FORMAT.format(new Date());
		Bundle extras = getIntent().getExtras();
		if(extras == null) {
			extras = new Bundle();
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
		}
		
		//afterPhotoTaken = new Intent(getApplication(), AfterPhotoTaken.class);
		afterPhotoTaken = new Intent(this, ProcessInBG.class);
		afterPhotoTaken.putExtras(extras);
		afterPhotoTaken.putExtra("photoName", photoName);
		
		File outputPath = new File(ScanUtils.getOutputPath(photoName));
		//Try to create an output folder
		if(!outputPath.mkdirs()){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Could not create output folder [" + 
								outputPath + 
								"].\n There may be a problem with the device's storage.")
			       .setCancelable(false)
			       .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			                finish();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			Uri imageUri = Uri.fromFile( new File(ScanUtils.getPhotoPath(photoName)) );
			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivityForResult(intent, TAKE_PICTURE);
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
				
				//The android camera app saves an additional copy of the image.
				//The following query is used to find it.
				String[] projection = new String[]{
				     MediaStore.Images.ImageColumns._ID,
				     MediaStore.Images.ImageColumns.DATA,
				     MediaStore.Images.ImageColumns.DATE_TAKEN};     

				final Cursor cursor = managedQuery(
				     MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
				     MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"); 

				if(cursor != null && cursor.getCount() > 0){
					cursor.moveToFirst();
					Log.e(LOG_TAG, "[" + cursor.getString(0) + "]");
					
					File outfile = new File(cursor.getString(1));
					if( !outfile.exists() || outfile.lastModified() < activityCreateTime.getTime() ) {
						Toast.makeText(getApplicationContext(), "Could not find original photo duplicate.", Toast.LENGTH_LONG).show();
					} else {
						//Ideally this would just rename the photo to move it to the ODKScan folder,
						//but that can fail in a number of ways.
						//see: outfile.renameTo(new File(ScanUtils.getOutputPath(photoName)));
						//So instead an extra photo is create in the ODKScan folder via the extra_output intent parameter
						//and the original is deleted.

						boolean success = outfile.delete();
						if(!success){
							Toast.makeText(getApplicationContext(), "Could not remove original photo duplicate from DCIM folder.", Toast.LENGTH_LONG).show();
						}
					}
				}
				startService(afterPhotoTaken);
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
}