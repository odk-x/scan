package org.opendatakit.scan.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.app.Activity;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opendatakit.common.android.activities.BaseActivity;
import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.services.ProcessFormService;
import org.opendatakit.scan.android.utils.ScanUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * AcquireFormImageActivity launches the Android camera app or a file picker app to capture a form
 * image. It also creates a directory for data about the form to be stored.
 **/
public class AcquireFormImageActivity extends BaseActivity {
   private static final String LOG_TAG = "ODKScan";
   private static final DateFormat COLLECT_INSTANCE_NAME_DATE_FORMAT = new SimpleDateFormat(
       "yyyy-MM-dd_kk-mm-ss");

   String photoName;
   Intent processPhoto;

   @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Default to taking pictures to acquire form images
      int acquisitionCode = R.integer.take_picture;

      try {
         photoName = "taken_" + COLLECT_INSTANCE_NAME_DATE_FORMAT.format(new Date());

         Bundle extras = getIntent().getExtras();
         if (extras == null) {
            extras = new Bundle();
            Log.i("SCAN", "No bundle");
         }
         if (extras.containsKey("acquisitionMethod")) {
            acquisitionCode = extras.getInt("acquisitionMethod");
            Log.d(LOG_TAG, "Acquisition code: " + acquisitionCode);
         }
         if (extras.containsKey("photoName")) {
            photoName = extras.getString("photoName");
         }
         if (!extras.containsKey("templatePaths")) {
            SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
            Log.d(LOG_TAG, "Captured photo");
            String[] templatePaths = { settings.getString("select_templates", "") };
            extras.putStringArray("templatePaths", templatePaths);

            if (templatePaths.length > 0) {
               String[] parts = templatePaths[0].split("/");
               String templateName = parts[parts.length - 1];
               photoName =
                   templateName + "_" + COLLECT_INSTANCE_NAME_DATE_FORMAT.format(new Date());
            }
         }

         prepareToProcessForm(extras.getStringArray("templatePaths"), extras);

         launchAcquireIntent(acquisitionCode);
      } catch (Exception e) {
         //Display an error dialog if something goes wrong.
         failAndReturn(e.toString());
         return;
      }
   }

   /**
    * Create the file structure for storing the scan data
    */
   private void prepareToProcessForm(String[] templatePaths, Bundle extras) {

      String inputPath = ScanUtils.getPhotoPath(photoName);
      String outputPath = ScanUtils.getOutputPath(photoName);

      //This is the configuration JSON passed into ODKScan-core
      //see: scan/bubblebot_lib/jni/ODKScan-core/processViaJSON.md
      JSONObject config = new JSONObject();
      try {
         config.put("trainingDataDirectory", ScanUtils.getTrainingExampleDirPath());
         config.put("inputImage", inputPath);
         config.put("outputDirectory", outputPath);
         config.put("templatePaths", new JSONArray(Arrays.asList(templatePaths)));
      } catch(Exception e) {
         failAndReturn(e.toString());
         return;
      }

      // Create the intent to process the acquired image.
      processPhoto = new Intent(this, ProcessFormService.class);
      processPhoto.putExtras(extras);
      processPhoto.putExtra("photoName", photoName);
      processPhoto.putExtra("config", config.toString());

      //Try to create an output folder
      boolean createSuccess = new File(outputPath).mkdirs();
      if (!createSuccess) {
         new Exception("Could not create output folder [" +
             outputPath +
             "].\n There may be a problem with the device's storage.");
      }
      //Create an output directory for the segments
      boolean segDirSuccess = new File(outputPath, "segments").mkdirs();
      if (!segDirSuccess) {
         new Exception("Could not create output folder for segments.");
      }
   }

   /**
    * Launch an app to acquire an image of a form to process
    * @param acquisitionCode the method for acquiring the image (e.g. camera, file picker)
    */
   private void launchAcquireIntent(int acquisitionCode) {
      Intent acquireIntent;
      switch (acquisitionCode) {
      default:
         // Default to capturing a new image from the camera, but log this as an error
         Log.e(LOG_TAG, "Error: Invalid Acquisition Code. Defaulting to camera capture.");
      case R.integer.take_picture:
         // Store the new image here
         Uri imageUri = Uri.fromFile(new File(ScanUtils.getPhotoPath(photoName)));

         // Create the intent
         acquireIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
         acquireIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
         acquireIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

         // Check that there exists an app that can handle this intent
         if (acquireIntent.resolveActivity(getPackageManager()) == null) {
            failAndReturn(this.getString(R.string.error_no_camera));
            return;
         }

         startActivityForResult(acquireIntent, R.integer.new_image);
         break;

      case R.integer.pick_file:
         // Initialize at the root of the SD Card
         File root = new File(android.os.Environment.getExternalStorageDirectory().getPath());
         Uri uri = Uri.fromFile(root);

         // Create the intent
         acquireIntent = new Intent("org.openintents.action.PICK_FILE");
         acquireIntent.setData(uri);
         acquireIntent.putExtra("org.openintents.extra.TITLE", R.string.select_image_title);
         acquireIntent
             .putExtra("org.openintents.extra.BUTTON_TEXT", R.string.select_image_button);

         // Check that there exists an app that can handle this intent
         if (acquireIntent.resolveActivity(getPackageManager()) == null) {
            failAndReturn(this.getString(R.string.error_no_file_picker));
            return;
         }

         startActivityForResult(acquireIntent, R.integer.existing_image);
         break;

      case R.integer.pick_directory:
         // TODO: Handle this case
         //intent.setAction("org.openintents.action.PICK_DIRECTORY");
      }
   }

   /**
    * Display an error message to the user and return to the main menu
    * @param message Error message
    */
   private void failAndReturn(String message) {
      // Show failed intent message
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(message);
      builder.setCancelable(false)
          .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                setResult(RESULT_CANCELED);
                finish();
             }
          });
   }

   @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      Log.d(LOG_TAG, "AcquireFormImage onActivityResult " + requestCode);

      if (resultCode == Activity.RESULT_FIRST_USER) {
         Log.d(LOG_TAG, "First User");
         finish();
      } else if (resultCode == Activity.RESULT_CANCELED) {
         Log.d(LOG_TAG, "Canceled");
         finish();
      } else if (resultCode != RESULT_OK) {
         failAndReturn(this.getString(R.string.error_acquire_bad_return));
         return;
      }

      File destFile = new File(ScanUtils.getPhotoPath(photoName));

      try {
         switch (requestCode) {
         case R.integer.new_image:
            Log.d(LOG_TAG, "Returned from Camera");
            finishActivity(resultCode);

            if (!destFile.exists()) {
               failAndReturn(this.getString(R.string.error_file_creation));
               return;
            }

            break;
         case R.integer.existing_image:
            Log.d(LOG_TAG, "Returned from file picker");
            finishActivity(resultCode);

            Uri uri = data.getData();
            Log.d(LOG_TAG, "File Uri Selected: " + uri.toString());

            File sourceFile = new File(uri.getPath());
            if (!sourceFile.exists()) {
               failAndReturn(this.getString(R.string.error_finding_file));
               return;
            }

            FileUtils.copyFile(sourceFile, destFile);

            break;
         case R.integer.image_directory:
            Log.d(LOG_TAG, "Returned from folder picker");
            // TODO: Implement this
         default:
            failAndReturn(this.getString(R.string.error_acquire_bad_return));
            return;
         }
      } catch (Exception e) {
         failAndReturn(e.toString());
      }

      Log.d(LOG_TAG, "Acquired form: " + ScanUtils.getPhotoPath(photoName));
      startService(processPhoto);

      finish();
   }

   @Override protected void onDestroy() {
      //Try to remove the forms directory if the photo couldn't be captured:
      //Note: this won't delete the folder if it has any files in it.
      File capturedImage = new File(ScanUtils.getPhotoPath(photoName));
      if (!capturedImage.exists()) {
         new File(ScanUtils.getOutputPath(photoName) + "/segments").delete();
         new File(ScanUtils.getOutputPath(photoName)).delete();
      }
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