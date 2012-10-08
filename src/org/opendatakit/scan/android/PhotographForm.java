package org.opendatakit.scan.android;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
/**
 * PhotographForm launches the Android camera app
 * to capture a form image.
 * It also creates a directory for data about the form to be stored.
 **/
public class PhotographForm extends Activity {
	private static final String LOG_TAG = "ODKScan";
	private static final int TAKE_PICTURE = 12346789;
	private String photoName;
    private static final DateFormat COLLECT_INSTANCE_NAME_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss");
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		photoName = "taken_" + COLLECT_INSTANCE_NAME_DATE_FORMAT.format(new Date());
		File outputPath = new File(ScanUtils.getOutputPath(photoName));
		//Try to create an output folder
		if(!outputPath.mkdirs()){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Could not create output folder. There may be a problem with the device's storage.")
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
				Intent intent = new Intent(getApplication(), AfterPhotoTaken.class);
				intent.putExtra("photoName", photoName);
				if( new File(ScanUtils.getPhotoPath(photoName)).exists() ) {
					Log.d(LOG_TAG, "Captured photo: " + ScanUtils.getPhotoPath(photoName));
					startActivity(intent);
				}
				else{
					Log.e(LOG_TAG, "Photo [" + photoName + "] could not be saved.");
				}
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