package org.opendatakit.scan.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
public class DisplayProcessedForm extends Activity {

	private static final String LOG_TAG = "ODKScan";

	private String photoName;
	private String templatePath;
	WebView myWebView;
	
	private Bundle extras;

	private boolean morePagesToScan = false;

	private Intent collectIntent;
	
	private Intent surveyIntent;
	
	private Intent tablesIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.processed_form);

			extras = getIntent().getExtras();
			if (extras == null || !extras.containsKey("photoName")) {
				throw new Exception("This activity must be lauched with a photoName specified in the extras.");
			}
			photoName = extras.getString("photoName");
			
			templatePath = extras.getString("templatePath");
			if(templatePath == null){
				//Since the template path is not in the extras we'll try to get it from the json output.
				JSONObject outputJSON = JSONUtils.parseFileToJSONObject(ScanUtils.getOutputPath(photoName) + "output.json");
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
			morePagesToScan = nextPageTemplatePath.exists(); //TODO: This doesn't work on the "View Scanned forms" path.
			if(morePagesToScan){
				Button nextPage = (Button) findViewById(R.id.nextPageBtn);
				nextPage.setVisibility(View.VISIBLE);
				nextPage.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(getApplication(), PhotographForm.class);
						String[] templatePaths = { nextPageTemplatePath.toString() };
						intent.putExtra("templatePaths", templatePaths);
						ArrayList<String> prevTemplatePaths = extras.getStringArrayList("prevTemplatePaths");
						ArrayList<String> prevPhotoNames = extras.getStringArrayList("prevPhotoNames");
						if(prevTemplatePaths == null || prevPhotoNames == null){
							intent.putStringArrayListExtra("prevTemplatePaths", new ArrayList<String>(
								    Arrays.asList(templatePath)));
							intent.putStringArrayListExtra("prevPhotoNames", new ArrayList<String>(
								    Arrays.asList(photoName)));
						} else {
							prevTemplatePaths.add(templatePath);
							prevPhotoNames.add(photoName);
							//Can I modify the array list in extras or do I need to do a put?
						}
						if(prevPhotoNames != null){
							intent.putExtra("photoName", photoName.replace("\\(page[0-9]+\\)", "(page" + (prevPhotoNames.size() + 1) + ")"));
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
				Button saveData = (Button) findViewById(R.id.saveBtn);
				saveData.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Log.i(LOG_TAG, "Using template: " + templatePath);
						if(collectIntent == null) {
							collectIntent = makeCollectIntent();
						} 
						//collectIntent is still null if Collect not installed.
						if(collectIntent != null) {
							if(collectIntent.getData() == null) {
								exportToCollect(0);
							}
						}
						
					}
				});
				Button transcribeData = (Button) findViewById(R.id.transcribeBtn);
				transcribeData.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Log.i(LOG_TAG, "Using template: " + templatePath);
						if(collectIntent == null) {
							collectIntent = makeCollectIntent();
						}
						//collectIntent is still null if Collect not installed.
						if(collectIntent != null) {
							if(collectIntent.getData() == null) {
								exportToCollect(1);
							} else {
								//The scan data has already been exported
								//so just start collect.
								startActivity(collectIntent);
							}
						}
					}
				});
				Button save2Data = (Button) findViewById(R.id.save2Btn);
				save2Data.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Log.i(LOG_TAG, "Using template: " + templatePath);
						/* Uncomment if you want Scan to launch Tables
						if(tablesIntent == null) {
							tablesIntent = makeTablesIntent();
						} 
						//tablesIntent is still null if Tables not installed.
						if(tablesIntent != null) {
							if(tablesIntent.getData() == null) {
								exportToTables(2);
							}
						}
						*/
						// Uncomment if you want Scan to launch Survey
						if(surveyIntent == null) {
							surveyIntent = makeSurveyIntent();
						}	 
						//surveyIntent is still null if Survey not installed.
						if(surveyIntent != null) {
							if(surveyIntent.getData() == null) {
								exportToSurvey(2);
							}
						}
						
					}
				});
				Button transcribe2Data = (Button) findViewById(R.id.transcribe2Btn);
				transcribe2Data.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Log.i(LOG_TAG, "Using template: " + templatePath);

						/* Uncomment to Launch Tables
						if(tablesIntent == null) {
							tablesIntent = makeTablesIntent();
						}
						//tablesIntent is still null if Tables not installed.
						if(tablesIntent != null) {
							if(tablesIntent.getData() == null) {
								exportToTables(3);
							} else {
								//The scan data has already been exported
								//so just start Tables.
								boolean tablesInstalled = checkForTablesInstallation(tablesIntent);
								if (tablesInstalled) {
									startActivity(tablesIntent);
								}
							}
						}
						*/
						// Launch Survey
						if(surveyIntent == null) {
							surveyIntent = makeSurveyIntent();
						}
						//surveyIntent is still null if Survey not installed.
						if(surveyIntent != null) {
							if(surveyIntent.getData() == null) {
								exportToSurvey(3);
							} else {
								//The scan data has already been exported
								//so just start Tables.
								boolean surveyInstalled = checkForTablesInstallation(surveyIntent);
								if (surveyInstalled) {
									startActivity(surveyIntent);
								}
							}
						}
					}
				});
			}

			ScanUtils.displayImageInWebView(
					(WebView) findViewById(R.id.webview2),
					ScanUtils.getMarkedupPhotoPath(photoName));
			
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
	
	/**
	 * Creates an intent for launching collect.
	 * May return null if collect is not installed.
	 * @return
	 */
	public Intent makeCollectIntent() {
		// Initialize the intent that will start collect.
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(new ComponentName("org.odk.collect.android",
				"org.odk.collect.android.activities.FormEntryActivity"));
		
		//Use the intent to see if collect is installed.
		PackageManager packMan = getPackageManager();
		if (packMan.queryIntentActivities(intent, 0).size() == 0) {
			// ////////////
			Log.i(LOG_TAG, "Collect is not installed.");
			// ////////////		
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("ODK Collect was not found on this device.")
					.setCancelable(false)
					.setPositiveButton("Install it.", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							Intent goToMarket = new Intent(Intent.ACTION_VIEW)
						    	.setData(Uri.parse("market://details?id=org.odk.collect.android"));
							startActivity(goToMarket);
							dialog.cancel();
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
			return null;
		}
		
		intent.setAction(Intent.ACTION_EDIT);
		
		//Start indicates that the form should be launched from the first question
		//rather than the prompt list.
		intent.putExtra("start", true);
		return intent;
	}
	
	/**
	 * Creates an intent for launching survey.
	 * May return null if survey is not installed.
	 * @return
	 */
	public Intent makeSurveyIntent() {
		// Initialize the intent that will start Survey.
		Intent intent =  getPackageManager().getLaunchIntentForPackage("org.opendatakit.survey.android");
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		intent.setAction(Intent.ACTION_EDIT);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		
		//Start indicates that the form should be launched from the first question
		//rather than the prompt list.
		// Not sure if this start parameter is still necessary in Survey
		intent.putExtra("start", true);
		return intent;
	}

	/**
	 * Creates an intent for launching tables.
	 * May return null if tables is not installed.
	 * @return
	 */
	public Intent makeTablesIntent() {
		// Initialize the intent that will start Tables.
	   // final String TABLE_DISPLAY_ACTIVITY =
	   //     "org.opendatakit.tables.activities.TableDisplayActivity";
	   // Intent intent = new Intent(TABLE_DISPLAY_ACTIVITY);
	    
	   //Old Way
		Intent intent = getPackageManager().getLaunchIntentForPackage("org.opendatakit.tables");
	    
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setAction(Intent.ACTION_VIEW);
		
		return intent;
	}
	
	/**
	 * Creates an intent for launching survey.
	 * May return null if survey is not installed.
	 * @return
	 */
	public boolean checkForSurveyInstallation(Intent intent) {
		boolean installed = true;
		
		//Use the intent to see if survey is installed.
		//this assumes that you have action and data specified
		PackageManager packMan = getPackageManager();
		if (packMan.queryIntentActivities(intent, 0).size() == 0) {
			// ////////////
			Log.i(LOG_TAG, "Survey is not installed.");
			// ////////////		
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("ODK Survey was not found on this device.")
					.setCancelable(false)
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
						public void onClick(DialogInterface dialog,
								int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
			installed = false;
		}
		
		return installed;
	}
	
	/**
	 * Creates an intent for launching tables.
	 * May return null if tables is not installed.
	 * @return
	 */
	public boolean checkForTablesInstallation(Intent intent) {
		boolean installed = true;
		
		//Use the intent to see if tables is installed.
		//this assumes that you have action and data specified
		PackageManager packMan = getPackageManager();
		if (packMan.queryIntentActivities(intent, 0).size() == 0) {
			// ////////////
			Log.i(LOG_TAG, "Tables is not installed.");
			// ////////////		
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("ODK Tables was not found on this device.")
					.setCancelable(false)
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
						public void onClick(DialogInterface dialog,
								int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
			installed = false;
		}
		
		return installed;
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exporting...");
		builder.setCancelable(false);
		return builder.create();
	}
	/**
	 * Exports the Scan JSON data to collect.
	 * If the requestCode is 1 collect will be launched
	 * after the export activity returns a result.
	 * @param requestCode
	 */
	public void exportToCollect(int requestCode) {
		showDialog(0);
		Intent createInstanceIntent = new Intent(getApplication(), JSON2XForm.class);
		createInstanceIntent.putExtras(extras);
		createInstanceIntent.putExtra("templatePath", templatePath);
		createInstanceIntent.putExtra("photoName", photoName);
		startActivityForResult(createInstanceIntent, requestCode);
	}
	
	/**
	 * Exports the Scan JSON data to Survey.
	 * If the requestCode is 3 Survey will be launched
	 * after the export activity returns a result.
	 * @param requestCode
	 */
	public void exportToSurvey(int requestCode) {
		showDialog(0);
		Intent createInstanceIntent = new Intent(getApplication(), JSON2SurveyJSON.class);
		createInstanceIntent.putExtras(extras);
		createInstanceIntent.putExtra("templatePath", templatePath);
		createInstanceIntent.putExtra("photoName", photoName);
		startActivityForResult(createInstanceIntent, requestCode);
	}
	
	/**
	 * Exports the Scan JSON data to Tables.
	 * If the requestCode is 3 Tables will be launched
	 * after the export activity returns a result.
	 * @param requestCode
	 */
	public void exportToTables(int requestCode) {
		showDialog(0);
		Intent createInstanceIntent = new Intent(getApplication(), JSON2SurveyJSON.class);
		createInstanceIntent.putExtras(extras);
		createInstanceIntent.putExtra("templatePath", templatePath);
		createInstanceIntent.putExtra("photoName", photoName);
		startActivityForResult(createInstanceIntent, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		dismissDialog(0);
		// Changed the code to only launch intents if the result was ok
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == 0 || requestCode == 1) {
				Button saveData = (Button) findViewById(R.id.saveBtn);
				saveData.setEnabled(false);
				saveData.setText("saved");
				collectIntent.putExtras(data);
				collectIntent.setData(data.getData());
			}
			
			if (requestCode == 2 || requestCode == 3) {
				Button save2Data = (Button) findViewById(R.id.save2Btn);
				save2Data.setEnabled(false);
				save2Data.setText("saved");
				/* Uncomment to launch tables
				tablesIntent.putExtras(data);
				tablesIntent.setData(data.getData()); */
				// Launch survey
				surveyIntent.putExtras(data);
				surveyIntent.setData(data.getData());
			}
			
			if (requestCode == 1) {
				//dismissDialog(0);
				Log.i(LOG_TAG, "Starting Collect...");	
				startActivity(collectIntent);
			}
			
			if (requestCode == 3) {
				//dismissDialog(1);

				/* Uncomment to Launch tables
				Log.i(LOG_TAG, "Starting Tables...");
				boolean tablesInstalled = checkForTablesInstallation(tablesIntent);
				if (tablesInstalled) {
					startActivity(tablesIntent);
				}
				*/
			   // Launch survey
				Log.i(LOG_TAG, "Starting Survey...");
				boolean surveyInstalled = checkForSurveyInstallation(surveyIntent);
				if (surveyInstalled) {
					startActivity(surveyIntent);
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//if(morePagesToScan) return false;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mscan_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		int itemId = item.getItemId();
		if (itemId == R.id.scanNewForm) {
			intent = new Intent(getApplication(), PhotographForm.class);
			startActivity(intent);
			finish();
			return true;
		} else if (itemId == R.id.startOver) {
			intent = new Intent(getApplication(), MainMenu.class);
			startActivity(intent);
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
