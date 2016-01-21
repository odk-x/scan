package org.opendatakit.scan.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.opendatakit.scan.R;
import org.opendatakit.scan.activities.AcquireFormImageActivity;
import org.opendatakit.scan.activities.ViewScannedForms;

import java.util.Random;
import java.util.Set;

public class MainMenuFragment extends Fragment {
  private static final String LOG_TAG = "ODKScan MainMenuFragment";

  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.main_menu, container, false);
    final Activity containerActivity = getActivity();

    SharedPreferences settings = PreferenceManager
        .getDefaultSharedPreferences(containerActivity.getApplicationContext());

    try {
      PackageInfo packInfo = containerActivity.getPackageManager()
          .getPackageInfo(containerActivity.getPackageName(), 0);

      // check version and run setup if needed
      int storedVersionCode = settings.getInt("version", 0);
      int appVersionCode = packInfo.versionCode;
      if (appVersionCode == 0 || storedVersionCode < appVersionCode) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("version", appVersionCode);
        editor.commit();
      }
    } catch (Exception e) {
      // Display an error dialog if something goes wrong.
      AlertDialog.Builder builder = new AlertDialog.Builder(containerActivity);
      builder.setMessage(e.toString()).setCancelable(false)
          .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              dialog.cancel();
            }
          });
      AlertDialog alert = builder.create();
      alert.show();
    }

    hookupButtonHandlers();
    updateTemplateText();

    return view;
  }

  private void hookupButtonHandlers() {
    final Application scanApp = getActivity().getApplication();

    Button scanForm = (Button) view.findViewById(R.id.ScanButton);
    scanForm.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(scanApp, AcquireFormImageActivity.class);
        intent.putExtra("acquisitionMethod", R.integer.take_picture);
        intent.putExtra("intentRequestCode", R.integer.scan_main_menu);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
      }
    });

    Button viewForms = (Button) view.findViewById(R.id.ViewFormsButton);
    viewForms.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(scanApp, ViewScannedForms.class);
        startActivity(intent);
      }
    });

  }

  private void updateTemplateText() {
    final Activity containerActivity = getActivity();

    SharedPreferences settings = PreferenceManager
        .getDefaultSharedPreferences(containerActivity.getApplicationContext());

    TextView templateText = (TextView) view.findViewById(R.id.TemplateText);

    // If no template is selected, present a warning
    if (!settings.contains("select_templates")) {
      templateText.setText(R.string.no_template);
      templateText.setTextColor(Color.RED);
      return;
    }

    Set<String> templatePaths = settings.getStringSet("select_templates", null);
    if (templatePaths == null || templatePaths.isEmpty()) {
      templateText.setText(R.string.no_template);
      templateText.setTextColor(Color.RED);
      return;
    }

    String templateName = "";
    for (String path : templatePaths) {
      String[] parts = path.split("/");
      templateName += parts[parts.length - 1] + ", ";
    }
    // Remove the trailing comma and space
    templateName = templateName.substring(0, templateName.length() - 2);

    String newScanText = String.format(getString(R.string.template_selected), templateName);

    templateText.setText(Html.fromHtml(newScanText));
    templateText.setTextColor(Color.BLACK);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateTemplateText();
  }

}
