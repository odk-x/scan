package org.opendatakit.scan.android;

import java.io.File;
import java.io.FilenameFilter;

import org.droidparts.preference.MultiSelectListPreference;
import org.opendatakit.common.android.activities.BasePreferenceActivity;

import android.os.Bundle;

public class AppSettings extends BasePreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.prefs);

		// TODO: For this it would be better to have a template manager like
		// Collect's form manager.
		// This will become slow and it would be nice to be able to
		// download/delete templates.
		// Plus if we put it on the main page it will be one less click.
		MultiSelectListPreference multiSelectPreference = (MultiSelectListPreference) findPreference("select_templates");

		// // Get the available templates:
		File dir = new File(ScanUtils.getTemplateDirPath());
		String[] templateNames = dir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				File templateFile = new File(dir, name);
				if (templateFile.isDirectory()) {
					// Make sure necessary files are present
					if (new File(templateFile, "template.json").exists()
							&& new File(templateFile, "form.jpg").exists()) {
						return true;
					}

				}
				return false;
			}
		});

		// Remove suffixes and set paths
		String[] templatePaths = new String[templateNames.length];
		for (int i = 0; i < templateNames.length; i++) {
			templateNames[i] = templateNames[i];//.replace(".json", "");
			templatePaths[i] = ScanUtils.getTemplateDirPath()
					+ templateNames[i];
		}

		multiSelectPreference.setEntries(templateNames);
		multiSelectPreference.setEntryValues(templatePaths);
	}

  public String getAppName() {
    return ScanUtils.getODKAppName();
  }
}