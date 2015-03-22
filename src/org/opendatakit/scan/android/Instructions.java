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
