package org.opendatakit.scan.android;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainMenu extends Activity {

	ProgressDialog pd;
	SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu); // Setup the UI

		settings = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		try {
			// Create the app folder if it doesn't exist:
			new File(ScanUtils.appFolder).mkdirs();
			checkSDCard();
			PackageInfo packInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			{
				// dynamically construct the main screen header string
				TextView mainMenuMessageLabel = (TextView) findViewById(R.id.version_display);
				mainMenuMessageLabel.setText("version:\n"
						+ packInfo.versionName);
			}
			// check version and run setup if needed
			int storedVersionCode = settings.getInt("version", 0);
			int appVersionCode = packInfo.versionCode;
			if (appVersionCode == 0 || storedVersionCode < appVersionCode) {
				pd = ProgressDialog.show(this, "Please wait...",
						"Extracting assets", true);
				Thread thread = new Thread(new RunSetup(handler, settings,
						getAssets(), appVersionCode));
				thread.start();
			}
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

		hookupButtonHandlers();
	}

	private void hookupButtonHandlers() {

		Button scanForm = (Button) findViewById(R.id.ScanButton);
		scanForm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), PhotographForm.class);
				startActivity(intent);
			}
		});

		Button viewForms = (Button) findViewById(R.id.ViewFormsButton);
		viewForms.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(),
						ViewScannedForms.class);
				startActivity(intent);
			}
		});

		Button instructions = (Button) findViewById(R.id.InstructionsButton);
		instructions.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), Instructions.class);
				startActivity(intent);
			}
		});

		Button settingsButton = (Button) findViewById(R.id.SettingsButton);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), AppSettings.class);
				startActivity(intent);
			}
		});
	}

	private void checkSDCard() throws Exception {
		// http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			// Now Check that there is room to store more images
			final int APROX_IMAGE_SIZE = 1000000;
			long usableSpace = ScanUtils.getUsableSpace(ScanUtils.appFolder);
			if (usableSpace >= 0 && usableSpace < 4 * APROX_IMAGE_SIZE) {
				throw new Exception("It looks like there isn't enough space to store more images.");
			}
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			throw new Exception("We cannot write the media.");
		} else {
			throw new Exception("We can neither read nor write the media.");
		}
	}

	@Override
	public void onBackPressed() {
		// This override is needed in order to avoid going back to the
		// AfterPhotoTaken activity
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			pd.dismiss();
		}
	};
}