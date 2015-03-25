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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;
/**
 * The RunSetup extracts everything in the assets folder to the ODKScan folder.
 * It also does some version control stuff.
 * It runs on a separate thread so as to avoid locking up the UI.
 */
public class RunSetup implements Runnable {
	
	private static final String LOG_TAG = "ODKScan";
	
	private SharedPreferences settings;
	private AssetManager assets;
	private Handler handler;
	private int appVersionCode;
	
	public RunSetup(Handler handler, SharedPreferences settings, AssetManager assets, int appVersionCode){
		this.handler = handler;
		this.settings = settings;
		this.assets = assets;
		this.appVersionCode = appVersionCode;
	}
	public void run() {

		SharedPreferences.Editor editor = settings.edit();
		
		// Create output dir if it doesn't exist
		new File(ScanUtils.getOutputDirPath()).mkdirs();
		
		try {
			//Creates a .nomedia file to prevent the images from showing up in the gallery.
			new File(ScanUtils.appFolder + ".nomedia").createNewFile();
			
			File trainingExamplesDir =  new File(ScanUtils.getTrainingExampleDirPath());
			File formTemplatesDir = new File(ScanUtils.getTemplateDirPath());
			
			//TODO: When new examples/templates are added to the assets dir they should be added here as well.
			//It would be nice to automatically delete examples/templates in the assets dir.
			rmdir(new File(trainingExamplesDir, "bubbles"));
			rmdir(new File(trainingExamplesDir, "squre_checkboxes"));
			rmdir(new File(formTemplatesDir, "example"));
			rmdir(new File(ScanUtils.getFormViewHTMLDir()));
			
			if(!settings.contains("select_templates")){
				//If there is no data for which templates to use, use the example template as default
				editor.putString("select_templates", ScanUtils.getTemplateDirPath() + "example");
			}
			
			extractAssets(new File(""), new File(ScanUtils.appFolder));
			
			editor.putInt("version", appVersionCode);
			
		} catch (IOException e) {
			// TODO: Terminate the app if this fails.
			e.printStackTrace();
			Log.i(LOG_TAG, "Extration Error");
		}
		editor.commit();
		handler.sendEmptyMessage(0);
	}
	/**
	 * Recursively copy all the assets in the specified assets directory to the specified output directory
	 * @param assetsDir
	 * @param outputDir
	 * @throws IOException
	 */
	protected void extractAssets(File assetsDir, File outputDir) throws IOException{

		outputDir.mkdirs();
		String[] assetNames = assets.list(assetsDir.toString());
		for(int i = 0; i < assetNames.length; i++){
			
			File nextAssetsDir = new File(assetsDir, assetNames[i]);
			File nextOutputDir = new File(outputDir, assetNames[i]);
			
			
			if(assets.list(nextAssetsDir.toString()).length == 0){
				copyAsset(nextAssetsDir, nextOutputDir);
			}
			else{
				extractAssets(nextAssetsDir, nextOutputDir);
			}
		}
	}
	/**
	 * Copy a single asset file to the specified directory.
	 * @param assetDir
	 * @param outputDir
	 * @throws IOException
	 */
	protected void copyAsset(File assetDir, File outputDir) throws IOException {
		
		Log.i(LOG_TAG, "Copying " + assetDir + " to " + outputDir.toString());
		
		InputStream fis = assets.open(assetDir.toString());

		outputDir.createNewFile();
		FileOutputStream fos = new FileOutputStream(outputDir);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = fis.read(buf)) > 0) {
			fos.write(buf, 0, len);
		}

		fos.close();
		fis.close();
	}
	/**
	 * Recursively remove all the files in a directory, then the directory.
	 * @param dir
	 */
	public void rmdir(File dir){
		
		if(dir.exists()){
			String[] files = dir.list();
			for(int i = 0; i < files.length; i++){
				File file = new File(dir, files[i]);
				Log.i(LOG_TAG, "Removing: " + file.toString());
				if(file.isDirectory()){
					rmdir(file);
				}
				else{
					file.delete();
				}
			}
			dir.delete();
		}
		else{
			Log.i(LOG_TAG, "Cound not remove directory, it does not exist:" + dir.toString());
		}
	}
}
