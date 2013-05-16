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

public class ProcessInBG extends Service {
	
	/*
	 * Ideas:
	 * 1. Make it so running jobs are shown in noticiations.
	 * 2. Update notifications when alignment finished and allow previews of it.
	 * 3. When processing fails make an activity that shows the photo.
	 * 4. For multipage forms, the user could be prompted to take the next picture as soon as alignment completes.
	 */
	
	private static final String LOG_TAG = "ODKScan";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		try {
			Toast.makeText(this, "Processing photo in background...", Toast.LENGTH_LONG).show();
			final Bundle extras = intent.getExtras();
			if (extras == null) {
				throw new Exception("Missing extras in intent.");
			}

			final String photoName = extras.getString("photoName");
	    	final String inputPath = ScanUtils.getPhotoPath(photoName);
	    	final String outputPath = ScanUtils.getOutputPath(photoName);
	    	final String[] templatePaths = extras.getStringArray("templatePaths");
	    	final String calibrationPath = null;
	    	final int notificationId = (int) (Math.random() * 9999999);
			final NotificationManager notificationManager = (NotificationManager) 
	                getSystemService(Context.NOTIFICATION_SERVICE);
			final Context context = getApplicationContext();
			
	        //int icon = android.R.drawable.stat_notify_more;
	        //CharSequence tickerText = "Form Processed";
	        //long when = System.currentTimeMillis();
	        Notification notification = new Notification(); //new Notification(icon, tickerText, when);
	        notification.setLatestEventInfo(context, "ODK Scan", "processing form...",
	                PendingIntent.getActivity(context, 0, new Intent(), 0));
	        notificationManager.notify(notificationId , notification);
	    	
	    	//A subprocess is used to the OpenCV code.
	    	//I tried doing it synchronously inside this service but it locks up the UI.
	    	
	    	final Handler handler = new Handler(new Handler.Callback() {
	            public boolean handleMessage(Message message) {
	    	        CharSequence contentTitle = "ODK Scan";
	    	        CharSequence contentText = "";
	    	        Intent notificationIntent = new Intent();
	            	
	            	JSONObject result = null;
	            	//TODO: Handle error messages.
					try {
						result = new JSONObject(message.getData().getString("result"));
					} catch (JSONException e) {
						Log.i(LOG_TAG, message.getData().getString("result"));
						contentText =  "Error parsing result JSON.";
					}

	    	        int icon = android.R.drawable.stat_notify_more;
	    	        CharSequence tickerText = "Form Processed";
	    	        long when = System.currentTimeMillis();
	    	        Notification notification = new Notification(icon, tickerText, when);
	    	        
	    	        if(result != null) {
		    	        String errorMessage = result.optString("errorMessage");
		    	        if(errorMessage == null || errorMessage.length() == 0) {
			    			try {
								ScanUtils.setTemplatePath(photoName, result.optString("templatePath"));
			    	        	contentText = "Successfully processed image!";
			    	        	notificationIntent = new Intent(context, DisplayProcessedForm.class);
			    	        	notificationIntent.putExtras(extras);
							} catch (IOException e) {
								contentText = "Error writing template name to file system.";
							}
		    	        } else {
		    	        	contentText = errorMessage;
		    	        	notificationIntent = new Intent();
		    	        }
	            	}
	    	        
	    	        //Make it so notifications are canceled on click
	    	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	    	        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
	    	                notificationIntent, 0);
	    	        notification.setLatestEventInfo(context, contentTitle, contentText,
	    	                contentIntent);
	    	        
					notificationManager.notify(notificationId , notification);
	    	        Toast.makeText(context, "Form processed", Toast.LENGTH_LONG).show();
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
	    				JSONObject config = new JSONObject();
	    				//config.put("inputPath", inputPath);
	                    config.put("trainingDataDirectory", ScanUtils.getTrainingExampleDirPath());
	                    config.put("inputImage", inputPath);
	                    config.put("outputDirectory", outputPath);
	                    config.put("templatePaths", new JSONArray(Arrays.asList(templatePaths)));
	                    Log.i(LOG_TAG, config.toString());
	    				String result = mProcessor.processViaJSON(config.toString());
						outputData.putString("result", result);
	    			} catch (Exception e) {
	    				outputData.putString("error", e.toString());
	    			}
	    			Message msg = new Message();
	    			msg.setData(outputData);
	    			handler.sendMessage(msg);
	    		}
	    	};
			Thread thread = new Thread(pRunner);
			//thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();

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