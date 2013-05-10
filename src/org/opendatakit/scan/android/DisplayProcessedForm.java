package org.opendatakit.scan.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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
import android.widget.Toast;

/**
 * This activity displays the image of a processed form
 */
public class DisplayProcessedForm extends Activity {

	private static final String LOG_TAG = "ODKScan";

	private static final int LENGTH_SHORT = 0;

	private String photoName;
	private String templatePath;
	WebView myWebView;
	
	private Bundle extras;

	private boolean morePagesToScan = false;

	private Intent collectIntent;
	
	// Set up the UI and load the processed image
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
			/*
			if (extras.getBoolean("startCollect", false)) {
				Intent dataIntent = new Intent();
				dataIntent.putExtra("start", true);
				startCollect(dataIntent);
				return;
			}
			*/
			templatePath = ScanUtils.getTemplatePath(photoName);
			if(!(new File(templatePath, "template.json")).exists()){
				throw new Exception("The form template is missing.");
			}
			
			Log.i(LOG_TAG, "Enabling buttons and attaching handlers...");
			//Multipage forms:
			//If there is a nextPage directory in the template directory 
			//Scan will assume it is processing a multipage form
			//where the template for the next page is in the nextPage directory.
			//In the extras, the prevTemplatePaths and prevPhotoPaths arrays are passed through
			//subsequent invocations of the Scan activities in order to store
			//information about the previous pages used to combine them into an xform.
			final File nextPageTemplatePath = new File(templatePath, "nextPage");
			morePagesToScan = nextPageTemplatePath.exists();
			if(morePagesToScan){
				Button nextPage = (Button) findViewById(R.id.nextPageBtn);
				nextPage.setVisibility(View.VISIBLE);
				nextPage.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(getApplication(), PhotographForm.class);
						intent.putExtra("templatePath", nextPageTemplatePath.toString());
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
								saveToCollect(0);
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
								saveToCollect(1);
							} else {
								startActivity(collectIntent);
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
	public Intent makeCollectIntent() {
		// Initialize the intent that will start collect and use it to see if
		// collect is installed.
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(new ComponentName("org.odk.collect.android",
				"org.odk.collect.android.activities.FormEntryActivity"));
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

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Exporting to Collect...");
		builder.setCancelable(false);
		return builder.create();
	}
	
	public void saveToCollect(int requestCode) {
		showDialog(0);
		Intent createInstanceIntent = new Intent(getApplication(), JSON2XForm.class);
		createInstanceIntent.putExtras(extras);
		createInstanceIntent.putExtra("templatePath", templatePath);
		createInstanceIntent.putExtra("photoName", photoName);
		startActivityForResult(createInstanceIntent, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		dismissDialog(0);
		if (resultCode == Activity.RESULT_OK) {
			Button saveData = (Button) findViewById(R.id.saveBtn);
			saveData.setEnabled(false);
			saveData.setText("saved");
			collectIntent.putExtras(data);
			collectIntent.setData(data.getData());
		}
		if (requestCode == 1) {
			// ////////////
			Log.i(LOG_TAG, "Starting Collect...");
			// ////////////		
			startActivity(collectIntent);
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
		/*
		if (itemId == R.id.exportToODK) {
			Log.i(LOG_TAG, "Using template: " + templatePath);
			Intent dataIntent = new Intent();
			dataIntent.putExtra("start", true);
			startCollect(dataIntent);
			return true;
		}
		*/
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