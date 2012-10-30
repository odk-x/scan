package org.opendatakit.scan.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

/**
 * This activity displays the image of a processed form
 */
public class DisplayProcessedForm extends Activity {

	private static final String LOG_TAG = "ODKScan";

	private String photoName;
	private String templatePath;
	WebView myWebView;
	
	private Bundle extras;

	private boolean morePagesToScan = false;

	// Set up the UI and load the processed image
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.processed_form);

			extras = getIntent().getExtras();
			if (extras != null) {
				photoName = extras.getString("photoName");
				// templatePath = settings.getString(photoName, "");
				templatePath = ScanUtils.getTemplatePath(photoName);
				if (extras.getBoolean("startCollect", false)) {
					Intent dataIntent = new Intent();
					dataIntent.putExtra("start", true);
					startCollect(dataIntent);
					return;
				}
			}
			//TODO: Check for template.json
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
			}
			
			/*
			 * String url = "file://" + ScanUtils.getFormViewHTMLDir() +
			 * "formView.html" + "?" + "formLocation=" +
			 * ScanUtils.getOutputPath(photoName); myWebView = (WebView)
			 * findViewById(R.id.webview2); WebSettings webSettings =
			 * myWebView.getSettings();
			 * webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			 * webSettings.setBuiltInZoomControls(true);
			 * webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
			 * webSettings.setJavaScriptEnabled(true);
			 * 
			 * myWebView.loadUrl(url); //myWebView.addJavascriptInterface(new
			 * JavaScriptInterface(getApplicationContext(), new
			 * File(ScanUtils.getOutputPath(photoName), "transcription.txt")),
			 * "Android");
			 * 
			 * Intent browserIntent = new Intent(Intent.ACTION_VIEW,
			 * Uri.parse(url)); startActivity(browserIntent);
			 * 
			 * 
			 * setTitle(getResources().getString(R.string.Health_Center) + ": "
			 * +
			 * //getSharedPreferences(getResources().getString(R.string.prefs_name
			 * ), 0)
			 * PreferenceManager.getDefaultSharedPreferences(getApplicationContext
			 * ()) .getString("healthCenter", "unspecifiedHC"));
			 */

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

	/*
	 * private void makeAlert(final String field){ AlertDialog.Builder alert =
	 * new AlertDialog.Builder(this); alert.setTitle("field");
	 * alert.setMessage("field :" + field);
	 * 
	 * // Set an EditText view to get user input final EditText input = new
	 * EditText(this); alert.setView(input);
	 * 
	 * alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	 * public void onClick(DialogInterface dialog, int whichButton) { String
	 * value = input.getText().toString();
	 * myWebView.loadUrl("javascript:testEcho('" + field + "', '"+ value +"')");
	 * return; } });
	 * 
	 * alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	 * 
	 * public void onClick(DialogInterface dialog, int which) { // TODO
	 * Auto-generated method stub return; } }); alert.show(); } public class
	 * JavaScriptInterface { Context mContext; BufferedWriter bufferWritter;
	 * 
	 * // Instantiate the interface and set the context
	 * JavaScriptInterface(Context c, File file) { mContext = c; FileWriter
	 * fileWritter; try { fileWritter = new FileWriter(file.toString());
	 * bufferWritter = new BufferedWriter(fileWritter); } catch (IOException e)
	 * { // TODO Auto-generated catch block e.printStackTrace(); } } public void
	 * saveData(String data){ try { bufferWritter.write(data);
	 * bufferWritter.flush(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } }
	 * 
	 * public void makePopup(String field, int field_idx) { Log.i(LOG_TAG,
	 * "makePopup args: " + field + ' ' + field_idx); makeAlert(field);
	 * 
	 * }
	 * 
	 * public void launchCollect(String field, int segment_idx, int field_idx) {
	 * //It would probably be better to launch collect directly if the form has
	 * already been exported. Log.i(LOG_TAG, "args: " + field + ' ' +
	 * segment_idx + ' ' + field_idx); Intent dataIntent = new Intent();
	 * dataIntent.putExtra("goto", field_idx); startCollect(dataIntent); } }
	 */
	public void startCollect(Intent data) {
		if (data.getData() == null) {
			// No instance specified, create or find one with new activity.
			Intent intent = new Intent(getApplication(), JSON2XForm.class);
			intent.putExtras(extras);
			intent.putExtras(data);
			intent.putExtra("templatePath", templatePath);
			intent.putExtra("photoName", photoName);
			startActivityForResult(intent, 1);
			//Once we get the result we rerun this function and collect should start.
			return;
		}
		// ////////////
		Log.i(LOG_TAG, "Starting Collect...");
		// ////////////
		// Initialize the intent that will start collect and use it to see if
		// collect is installed.
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(new ComponentName("org.odk.collect.android",
				"org.odk.collect.android.activities.FormEntryActivity"));
		PackageManager packMan = getPackageManager();
		if (packMan.queryIntentActivities(intent, 0).size() == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("ODK Collect was not found on this device.")
					.setCancelable(false)
					.setNeutralButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			return;
		}

		intent.setAction(Intent.ACTION_EDIT);
		intent.putExtras(data);
		intent.setData(data.getData());
		startActivity(intent);
		//startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			//finish();
		} else if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK) {
				startCollect(data);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if(morePagesToScan) return;
		// Show the options menu when the activity is started (so people know it
		// exists)
		openOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(morePagesToScan) return false;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mscan_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent intent;
		int itemId = item.getItemId();
		if (itemId == R.id.exportToODK) {
			Log.i(LOG_TAG, "Using template: " + templatePath);
			Intent dataIntent = new Intent();
			dataIntent.putExtra("start", true);
			startCollect(dataIntent);
			return true;
		} else if (itemId == R.id.scanNewForm) {
			intent = new Intent(getApplication(), PhotographForm.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.startOver) {
			intent = new Intent(getApplication(), MainMenu.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}