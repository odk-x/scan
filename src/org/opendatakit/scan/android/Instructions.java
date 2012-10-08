package org.opendatakit.scan.android;

import android.app.Activity;
import android.os.Bundle;

/* Instructions activity
 * 
 * This activity displays the instructions on screen
 */
public class Instructions extends Activity {

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
}
