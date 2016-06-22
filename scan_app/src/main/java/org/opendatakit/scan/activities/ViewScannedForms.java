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

package org.opendatakit.scan.activities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

import org.opendatakit.common.android.activities.BaseListActivity;
import org.opendatakit.scan.R;
import org.opendatakit.scan.utils.ScanUtils;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This activity displays a list of previously scanned forms.
 **/
public class ViewScannedForms extends BaseListActivity {

  private String[] photoNames;
  private ArrayAdapter<String> myAdapter;

  // Initialize the application
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    File dir = new File(ScanUtils.getOutputDirPath());

    photoNames = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return (new File(dir, name)).isDirectory();
      }
    });

    myAdapter = new ArrayAdapter<String>(this, R.layout.filename_list_item, photoNames) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout view = (convertView != null) ? (LinearLayout) convertView : createView(parent);

        String photoName = photoNames[position];

        TextView photoStatus = (TextView) view.findViewById(R.id.photoStatus);
        if (new File(ScanUtils.getJsonPath(photoName)).exists()) {
          photoStatus.setTextColor(Color.parseColor("#00FF00"));
        } else if (new File(ScanUtils.getAlignedPhotoPath(photoName)).exists()) {
          photoStatus.setTextColor(Color.parseColor("#FFFF00"));
        } else {
          photoStatus.setTextColor(Color.parseColor("#FF0000"));
        }

        TextView nameView = (TextView) view.findViewById(R.id.templateName);

        String[] parts = photoName.split("_");
        String templateName = parts[0];

        if (templateName != null && templateName.length() != 0) {
          nameView.setText(templateName);
        }

        //ND restructuring to try and put view scanned forms back in
        /*try {
          String templatePath = ScanUtils.getTemplatePath(photoName);
					Log.i("SCAN", "templatePath " + templatePath);

					String templateName = new File(templatePath).getName();
					Log.i("SCAN", "templateName " + templateName);

					if (templateName != null && templateName.length() != 0) {
						nameView.setText(templateName);
					}
				} catch (Exception e) {
					// no-op
					Log.i("SCAN", "BOO ");
				}*/

        TextView type = (TextView) view.findViewById(R.id.createdTime);
        type.setText(
            new Date(new File(ScanUtils.getPhotoPath(photoName)).lastModified()).toString());

        return view;
      }

      private LinearLayout createView(ViewGroup parent) {
        LinearLayout item = (LinearLayout) getLayoutInflater()
            .inflate(R.layout.filename_list_item, parent, false);
        return item;
      }
    };

    setListAdapter(myAdapter);

    ListView lv = getListView();

    lv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String photoName = photoNames[position];
        String[] parts = photoName.split("_");
        String templateName = parts[0];

        if (new File(ScanUtils.getJsonPath(photoName)).exists()) {
          Intent intent = new Intent(getApplication(), DisplayProcessedFormActivity.class);
          intent.putExtra("photoName", photoName);
          intent.putExtra("templatePath", ScanUtils.getTemplateDirPath() + templateName);
          startActivity(intent);
        } else {
          // TODO: Throw an error
        }
      }
    });

  }

  @Override
  public void onResume() {
    super.onResume();
    myAdapter.notifyDataSetChanged();
  }

  public String getAppName() {
    return ScanUtils.getODKAppName();
  }
}
