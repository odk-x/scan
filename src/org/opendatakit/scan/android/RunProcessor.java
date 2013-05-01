package org.opendatakit.scan.android;

import java.io.File;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bubblebot.jni.Processor;
/**
 * RunProcessor is a wrapper for ODKScan-core
 * the cpp OpenCV image processing code.
 **/
public class RunProcessor implements Runnable {
	
	private static final String LOG_TAG = "ODKScan";
	
	public enum Mode {
	    LOAD, LOAD_ALIGN, PROCESS
	}
	
	private String[] templatePaths;
	private String calibrationPath;
	
	private Mode mode;
	
	//The JNI object that connects to the ODKScan-core code.
	private Processor mProcessor;
	//A handler to call once the processing is finished.
	private Handler handler;
	private String photoName;

	public RunProcessor(Handler handler, String photoName, String[] templatePaths) {
		this.handler = handler;
		this.photoName = photoName;
		this.templatePaths = templatePaths;
		this.calibrationPath = null;
		mProcessor = new Processor(ScanUtils.appFolder);
	}
	public RunProcessor(Handler handler, String photoName, String[] templatePaths, String calibrationPath) {
		this.handler = handler;
		this.photoName = photoName;
		this.templatePaths = templatePaths;
		this.calibrationPath = calibrationPath;
		mProcessor = new Processor(ScanUtils.appFolder);
	}
	/**
	 * This method sets the mode the processor is to run in.
	 * @param mode
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	//@Override
	public void run() {
		
		Message msg = new Message();
		msg.arg1 = 0;//I'm using arg1 as a success indicator. A value of 1 means success.
		msg.what = mode.ordinal();
		
		try{
			if(mode == Mode.LOAD) {
				if( mProcessor.loadFormImage(ScanUtils.getAlignedPhotoPath(photoName), null) ) {
					if(mProcessor.setTemplate( templatePaths[0] )) {
						msg.arg1 = 1;
					}
				}
			}
			else if(mode == Mode.LOAD_ALIGN) {
				if( mProcessor.loadFormImage(ScanUtils.getPhotoPath(photoName), calibrationPath) ) {
					Log.i(LOG_TAG,"Loading: " + photoName);
					
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
							if( mProcessor.alignForm(ScanUtils.getAlignedPhotoPath(photoName), formIdx) ) {
								msg.arg1 = 1;//indicates success
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
					//Indicate which template was used.
					msg.arg2 = formIdx;
				}
				else {
					throw new Exception("Failed to load image: " + photoName);
				}
			}
			else if(mode == Mode.PROCESS) {
				//Create an output directory for the segments
				new File(ScanUtils.getOutputPath(photoName), "segments").mkdirs();
				String result = mProcessor.scanAndMarkup( ScanUtils.getOutputPath(photoName) );
				if( result.length() > 0 ) {
					throw new Exception(result);
				} else {
					msg.arg1 = 1;
				}
			}
		}
		catch(Exception e){
			Log.i(LOG_TAG,e.getMessage());
			Bundle errorMessage = new Bundle();
			errorMessage.putString("errorMessage", e.getMessage());
			msg.setData(errorMessage);
		}
		handler.sendMessage(msg);
	
	}
}
