/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.scan.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bubblebot.jni.Processor;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * This service invokes the cpp image processing code to run in the background.
 * It creates a notification that it's processing an image and updates it when it completes.
 */
public class ProcessInBG extends Service {
	//Android has an IntentService class which fits the queing requirement nicely
	//but I'm not using it because it seems like it will be easier to deal with the 
	//notifications for enqueued jobs this way.
	/*
	 * Ideas:
	 * 1. Update notifications when alignment finished and allow previews of it.
	 * 2. For multipage forms, the user could be prompted to take the next picture as soon as alignment completes.
	 */
	
	private static final String LOG_TAG = "ODKScan";
	
	/**
	 * This process is a singleton. Every time an activity "starts" it
	 * a new runnable is created to do the processing code and added to this queue.
	 * This makes it so the runnables can be throttled (rather than all try to run in parallel)
	 * so they don't end up thrashing or causing other issues due to resource limitations.
	 */
	private LinkedList<Runnable> processingQueue = new LinkedList<Runnable>();
	private int threads = 0;
	/**
	 * Start the next job on the queue is there
	 * aren't too many threads running.
	 */
	public void updateProcessingQueue(){
		if(threads < 1){
			Runnable nextJob = processingQueue.poll();
			if(nextJob == null && threads == 0){
				stopSelf();
				return;
			}
			Thread thread = new Thread(nextJob);
			//thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
			threads++;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		try {
			Toast.makeText(this, "Processing photo in background...", Toast.LENGTH_LONG).show();
			final Bundle extras = intent.getExtras();
			if (extras == null) {
				throw new Exception("Missing extras in intent.");
			}

			final String photoName = extras.getString("photoName");
			
			//Create an output directory for the segments
			new File(ScanUtils.getOutputPath(photoName), "segments").mkdirs();
			
	    	final String inputPath = ScanUtils.getPhotoPath(photoName);
	    	final String outputPath = ScanUtils.getOutputPath(photoName);
	    	final String[] templatePaths = extras.getStringArray("templatePaths");
	    	//final String calibrationPath = null;
	    	final int notificationId = (int) (Math.random() * 9999999);
			final NotificationManager notificationManager = (NotificationManager) 
	                getSystemService(Context.NOTIFICATION_SERVICE);
			final Context context = getApplicationContext();
			
	        int icon = android.R.drawable.status_bar_item_background;
	        CharSequence tickerText = "Processing form...";
	        long when = System.currentTimeMillis();
	        Intent waitingIntent = new Intent(context, DisplayStatus.class);
	        waitingIntent.putExtras(extras);
	        Notification notification = new Notification(icon, tickerText, when);
	        notification.setLatestEventInfo(context, "ODK Scan", "Processing form...",
	                PendingIntent.getActivity(context, 0, waitingIntent, 0));
	        notificationManager.notify(notificationId , notification);
	    	
	    	//A subprocess is used to run the OpenCV code.
	    	//I tried doing it synchronously inside this service but it locks up the UI.
	    	
	        /**
	         * This handler gets called with the result json when
	         * the scan cpp code finishes processing an image.
	         */
	    	final Handler handler = new Handler(new Handler.Callback() {
	            public boolean handleMessage(Message message) {
	            	threads--;
	            	updateProcessingQueue();
	            	Bundle messageData = message.getData();
	            	JSONObject result = null;
	            	
					try {
						result = new JSONObject(messageData.getString("result"));
					} catch (JSONException e) {
						Log.i(LOG_TAG, "Unparsable JSON: " + messageData.getString("result"));
					}

	    	        CharSequence contentTitle = "ODK Scan";
	    	        
					//Assume an error for the default notification.
	    	        int icon = android.R.drawable.stat_notify_error;
	    	        CharSequence contentText = "Error processing form.";
	    	        CharSequence tickerText = contentText;
	    	        long when = System.currentTimeMillis();
	    	        Intent notificationIntent = new Intent(context, DisplayStatus.class);
	    	        
	    	        if(result != null) {
		    	        String errorMessage = result.optString("errorMessage");
		    	        if(errorMessage.length() == 0) {
			    	        try {
			    	        	ScanUtils.setTemplatePath(photoName, result.optString("templatePath"));
			    	        } catch (IOException e) {
			    	        	Toast.makeText(context, "Error: Couldn't write template name to file system.", Toast.LENGTH_LONG).show();
							}
		    	        	contentText = "Successfully processed image!";
		    	        	tickerText = contentText;
		    	        	icon = android.R.drawable.stat_notify_more;
		    	        	notificationIntent = new Intent(context, DisplayProcessedForm.class);
		    	        }
	            	} else {
	            		Toast.makeText(context, "Error: Scan result is null.", Toast.LENGTH_LONG).show();
	            	}
	    	        
	    	        //Pass along the data from the intent that started this service.
	    	        notificationIntent.putExtras(extras);
	    	        //Pass along the message data to the notification activity.
	    	        notificationIntent.putExtras(messageData);
	    	        Notification notification = new Notification(icon, tickerText, when);
	    	        //Make it so notifications are canceled on click
	    	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    	        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId,
	    	                notificationIntent, 0);
	    	        notification.setLatestEventInfo(context, contentTitle, contentText,
	    	                contentIntent);
	    	        
					notificationManager.notify(notificationId , notification);
	    	        
					return true;
	            }
	    	});

	    	Runnable pRunner = new Runnable() {
	    		public void run() {
	    			//The JNI object that connects to the ODKScan-core code.
	    			//TODO: I don't like that the appFolder is needed here...
	    			final Processor mProcessor = new Processor(ScanUtils.appFolder);
	    			Bundle outputData = new Bundle();
	    			try {
	    				//TODO: Pass the config JSON in with the bundle
	    				JSONObject config = new JSONObject();
	                    config.put("trainingDataDirectory", ScanUtils.getTrainingExampleDirPath());
	                    config.put("inputImage", inputPath);
	                    config.put("outputDirectory", outputPath);
	                    config.put("templatePaths", new JSONArray(Arrays.asList(templatePaths)));
	                    Log.i(LOG_TAG, config.toString());
	    				String result = mProcessor.processViaJSON(config.toString());
						outputData.putString("result", result);
	    			} catch (JSONException e) {
	    				Log.i(LOG_TAG, "Error adding property to config.");
	    			}
	    			Message msg = new Message();
	    			msg.setData(outputData);
	    			handler.sendMessage(msg);
	    		}
	    	};
	    	processingQueue.offer(pRunner);
	    	updateProcessingQueue();
	    	
		} catch (Exception e) {
			Log.i(LOG_TAG, "BG procesing exception.");
		}
		return startid;
	}

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}