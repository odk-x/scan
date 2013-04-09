package org.opendatakit.scan.android;


import com.bubblebot.jni.Processor;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

//TODO: Get rid of process button.
/**
 * This is a activity for calling scan by intent just to do alignment.
 */
public class AlignImage extends Activity {

	private static final String LOG_TAG = "ODKScan";
	
	/*
	static class MyInnerHandler extends Handler{
        WeakReference<AlignImage> outer;

        MyInnerHandler(AlignImage caller) {
        	outer = new WeakReference<AlignImage>(caller);
        }

        @Override
        public void handleMessage(Message message) {
        	outer.get().updateUI(message.getData());
        }
    }
	*/

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		try {
			Log.i(LOG_TAG, "Align Image");

			setContentView(R.layout.align_image);

			final LinearLayout content = (LinearLayout) findViewById(R.id.myLinearLayout);
			
			Intent intent = getIntent();

			Bundle extras = intent.getExtras();
			if (extras == null) {
				throw new Exception("Missing extras in intent.");
			}

	    	final String inputPath = extras.getString("inputPath");
	    	final String outputPath = extras.getString("outputPath");
	    	final String[] templatePaths = { extras.getString("templatePath") };
	    	final String calibrationPath = null;
	    	final Bundle outputData = new Bundle(extras);
	    	
	    	//final MyInnerHandler handler = new MyInnerHandler(this);
	    	
	    	final Handler handler = new Handler(new Handler.Callback() {
	            public boolean handleMessage(Message message) {
	            	Log.i(LOG_TAG, "Alignment processing complete");
	            	dismissDialog(0);
	            	Bundle result = message.getData();
	        		if (!result.containsKey("errorMessage")) {
	        			ScanUtils.displayImageInWebView(
	        					(WebView) findViewById(R.id.webview),
	        					result.getString("outputPath"));
	        		} else {
	        			RelativeLayout failureMessage = (RelativeLayout) findViewById(R.id.failureContainer);
	        			failureMessage.setVisibility(View.VISIBLE);
	        			((TextView) findViewById(R.id.failureMessage)).setText(result.getString("errorMessage"));
	        		}
	        		content.setVisibility(View.VISIBLE);
					return true;
	            }
	    	});

	    	Runnable alignmentRunner = new Runnable() {
	    		public void run() {
	    			//The JNI object that connects to the ODKScan-core code.
	    			//TODO: I don't like that the appFolder is needed here...
	    			final Processor mProcessor = new Processor(ScanUtils.appFolder);
	    			
	    			try {
						if( mProcessor.loadFormImage(inputPath, calibrationPath) ) {
							int formIdx = 0;
							
							for(int i = 0; i < templatePaths.length; i++){ 
								Log.i(LOG_TAG, "loadingFD: " + templatePaths[i]);
								if(!mProcessor.loadFeatureData(templatePaths[i])){
									throw new Exception("Could not load feature data from: " + templatePaths[i]);
								}
							}
							
							if(templatePaths.length > 1) {
								formIdx = mProcessor.detectForm();
							}
							
							if(formIdx >= 0) {
								if(mProcessor.setTemplate(templatePaths[formIdx])) {
									Log.i(LOG_TAG,"template loaded");
									if( mProcessor.alignForm(outputPath, formIdx) ) {
										Log.i(LOG_TAG,"aligned");
									}
									else {
										throw new Exception("Failed to align form.");
									}
								}
								else {
									throw new Exception("Failed to set template.");
								}
							}
							else {
								throw new Exception("Failed to detect form.");
							}
						}
						else {
							throw new Exception("Failed to load image: " + inputPath);
						}
	    			} catch (Exception e) {
	    				outputData.putString("errorMessage", e.toString());
	    			}
	    			Message msg = new Message();
	    			msg.setData(outputData);
	    			handler.sendMessage(msg);
	    		}
	    	};
			showDialog(0);
			Thread thread = new Thread(alignmentRunner);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();

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
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.aligning_form));
		builder.setCancelable(false);

		return builder.create();
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
}