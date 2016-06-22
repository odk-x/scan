/*
 * Copyright (C) 2014 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.scan.preferences;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;
import org.opendatakit.scan.R;
import org.opendatakit.scan.utils.ScanUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

public class TemplatePreference extends MultiSelectListPreference {

  // TODO: For this it would be better to have a template manager like
  // Collect's form manager.
  // This will become slow and it would be nice to be able to
  // download/delete templates.
  // Plus if we put it on the main page it will be one less click.

  public TemplatePreference(final Context context, final AttributeSet attrs) {
    super(context, attrs);

    // Get the available templates:
    File dir = new File(ScanUtils.getTemplateDirPath());
    String[] templateNames = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        File templateFile = new File(dir, name);
        if (templateFile.isDirectory()) {
          // Make sure necessary files are present
          if (new File(templateFile, "template.json").exists() && new File(templateFile, "form.jpg")
              .exists()) {
            return true;
          }

        }
        return false;
      }
    });

    // Remove suffixes and set paths
    String[] templatePaths = new String[templateNames.length];
    for (int i = 0; i < templateNames.length; i++) {
      templateNames[i] = templateNames[i];
      templatePaths[i] = ScanUtils.getTemplateDirPath() + templateNames[i];
    }

    setEntries(templateNames);
    setEntryValues(templatePaths);
  }

  /**
   * Displays the selected templates
   */
  @Override
  public CharSequence getSummary() {
    Set<String> selectedTemplates = getValues();

    // Concat the list of template names that are selected
    String templateNameString = "";
    for (String path : selectedTemplates) {
      String[] parts = path.toString().split("/");
      templateNameString += parts[parts.length - 1] + ", ";
    }

    // Fill in the selected template names, or prompt the user for one if none are found
    String summary = getContext().getString(R.string.no_form_selected);
    if (!selectedTemplates.isEmpty()) {
      // Remove the trailing comma and space
      templateNameString = templateNameString.substring(0, templateNameString.length() - 2);
      summary = String
          .format(getContext().getString(R.string.specify_form_type), templateNameString);
    }

    return summary;
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);
    setSummary(getSummary());
  }

}
