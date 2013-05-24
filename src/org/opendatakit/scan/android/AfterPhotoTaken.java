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

import java.io.IOException;
import java.util.Date;

import org.droidparts.preference.MultiSelectListPreference;
import org.opendatakit.scan.android.RunProcessor.Mode;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @deprecated using ProcessInBG instead.
 * This activity starts the form processor and provides user feedback by
 * displaying progress dialogs and the alignment results.
 */
public class AfterPhotoTaken extends Activity {

	private static final String LOG_TAG = "ODKScan";

	private String photoName;
	private RunProcessor runProcessor;

	private Button processButton;
	private LinearLayout content;

	private long startTime;// only needed in debugMode

	private String[] templatePaths;
	
	private Bundle extras;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Log.i(LOG_TAG, "After Photo taken");

			setContentView(R.layout.after_photo_taken);

			content = (LinearLayout) findViewById(R.id.myLinearLayout);
			processButton = (Button) findViewById(R.id.process_button);
			
			extras = getIntent().getExtras();
			if (extras == null) {
				Log.i(LOG_TAG, "extras == null");
				// This might happen if we use back to reach this activity from
				// the camera activity.
				content.setVisibility(View.VISIBLE);
				return;
			}

			photoName = extras.getString("photoName");
			
			//Handler that gets called when processing finishes:
	    	final Handler handler = new Handler(new Handler.Callback() {
	            public boolean handleMessage(Message message) {
					RunProcessor.Mode mode = RunProcessor.Mode.values()[message.what];
					boolean success = (message.arg1 == 1);
	            	Bundle result = message.getData();
					String errorMessage = result.getString("errorMessage");
					try {
						dismissDialog(message.what);
					} catch (IllegalArgumentException e) {
						Log.i(LOG_TAG, "Exception: Dialog with id " + message.what
								+ " was not previously shown.");
					}

					if (ScanUtils.DebugMode) {
						Log.i(LOG_TAG, "Mode: " + mode);
						if (success) {
							Log.i(LOG_TAG, "Success!");
						}
						double timeTaken = (double) (new Date().getTime() - startTime) / 1000;
						Log.i(LOG_TAG, "Time taken:" + String.format("%.2f", timeTaken));
					}
					switch (mode) {
					case LOAD:
						updateUI(success, errorMessage);
						break;
					case LOAD_ALIGN:
						if (success) {
							int templatePathIdx = message.arg2;
							try {
								ScanUtils.setTemplatePath(photoName,
										templatePaths[templatePathIdx]);
							} catch (IOException e) {
								// TODO Auto-generated catch block
							}
						}
						updateUI(success, errorMessage);
						break;
					case PROCESS:
						if (success) {
							Intent intent = new Intent(getApplication(),
									DisplayProcessedForm.class);
							intent.putExtras(extras);
							startActivity(intent);
							finish();
						} else {
							updateUI(success, errorMessage);
						}
						break;
					default:
						return true;
					}
					return true;
	            }
	    	});
			
			Button retake = (Button) findViewById(R.id.retake_button);
			retake.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getApplication(),
							PhotographForm.class);
					extras.putString("photoName", photoName + "_retake");
					intent.putExtras(extras);
					startActivity(intent);
					finish();
				}
			});

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());

			if (extras.getBoolean("preAligned")) {
				// This is for opening forms from the scanned form list that haven't yet been processed.
				String[] templatePath = { ScanUtils.getTemplatePath(photoName) };
				runProcessor = new RunProcessor(handler, photoName,
						templatePath, settings.getString("calibrationFile",
								null));

				startThread(RunProcessor.Mode.LOAD);
			} else if (extras.containsKey("templatePaths")) {
				// Supplying templatePaths in the extras overrides those in the prefs.
				// This is used for specifying the next page in a multi-page form.
				templatePaths = extras.getStringArray("templatePaths");
				runProcessor = new RunProcessor(handler, photoName,
						templatePaths, settings.getString("calibrationFile",
								null));
				startThread(RunProcessor.Mode.LOAD_ALIGN);
			} else {
				updateUI(false, "Error: templatePaths key was not provided.");
			}
			/*
			//Moving this to Photograph Form:
			else {
				templatePaths = MultiSelectListPreference
						.fromPersistedPreferenceValue(settings.getString(
								"select_templates", ""));

				if (templatePaths == null || templatePaths.length == 0) {
					updateUI(false, "No templates are selected.\nYou can select templates in the Settings menu.");
					return;
				}

				// Start another thread to handle all the image processing:
				runProcessor = new RunProcessor(handler, photoName,
						templatePaths, settings.getString("calibrationFile",
								null));
				startThread(RunProcessor.Mode.LOAD_ALIGN);
			}
			*/

			processButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startThread(RunProcessor.Mode.PROCESS);
				}
			});
		} catch (Exception e) {
			// Display an error dialog if something goes wrong.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(e.toString())
			.setCancelable(false)
			.setNeutralButton("Ok",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int id) {
					dialog.cancel();
					finish();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	// Launch a thread that runs runProcessor.
	// Shows a status dialog as well.
	protected void startThread(Mode mode) {
		if (ScanUtils.DebugMode) {
			Log.i(LOG_TAG, "photoName: " + photoName);
			startTime = new Date().getTime();
		}
		showDialog(mode.ordinal());
		runProcessor.setMode(mode);

		Thread thread = new Thread(runProcessor);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (RunProcessor.Mode.values()[id]) {
		case LOAD:
			builder.setTitle("Loading Image");
			break;
		case LOAD_ALIGN:
			builder.setTitle(getResources().getString(R.string.aligning_form));
			break;
		case PROCESS:
			builder.setTitle(getResources().getString(R.string.processing_form));
			//builder.setMessage("The first time you use a template this will be slow because the classifier is being trained.");
			break;
		default:
			return null;
		}
		builder.setCancelable(false);

		return builder.create();
	}

	/**
	 * Updates the UI based on the result of the loading/alignment stage.
	 * 
	 * @param success
	 */
	public void updateUI(boolean success, String errorMessage) {
		Log.i(LOG_TAG, "errorMessage:" + errorMessage);
		if (success) {
			ScanUtils.displayImageInWebView(
					(WebView) findViewById(R.id.webview),
					ScanUtils.getAlignedPhotoPath(photoName));
		} else {
			RelativeLayout failureMessage = (RelativeLayout) findViewById(R.id.failureContainer);
			failureMessage.setVisibility(View.VISIBLE);
			if(errorMessage != null && errorMessage.length() > 0) {
				((TextView) findViewById(R.id.failureMessage)).setText(errorMessage);
			}
		}
		content.setVisibility(View.VISIBLE);
		processButton.setEnabled(success);
	}

}