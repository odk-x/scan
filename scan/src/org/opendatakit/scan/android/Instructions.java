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

package org.opendatakit.scan.android;

import org.opendatakit.common.android.activities.BaseActivity;

import android.os.Bundle;

/* Instructions activity
 * 
 * This activity displays the instructions on screen
 */
public class Instructions extends BaseActivity {

	// Initialize the application
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bubble_instructions); // Setup the UI
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
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
