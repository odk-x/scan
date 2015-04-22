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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.opendatakit.common.android.utilities.UrlUtils;

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.os.Environment;
import android.os.StatFs;

/**
 * ScanUtils contains methods and data shared across the application. For
 * example it is used to construct all the output filepaths.
 */
public class ScanUtils {
	// Prevent instantiations
	private ScanUtils() {
	}

	public static final String appName = "tables";
	public static final boolean DebugMode = false;

	public static final String appFolder = Environment.getExternalStorageDirectory().getPath() + "/ODKScan/";
	
	public static final String extStorageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	public static final String getODKAppName() {
		return appName;
	}
	
	public static String getAppFormDirPath(String formId) {
		return  extStorageDir + "/opendatakit/" + appName + "/tables/" + formId + "/forms/" + formId + "/";
	}
	
	public static String getAppInstancesDirPath(String formId) {
		return extStorageDir + "/opendatakit/" + appName + "/tables/" + formId +  "/instances/"; 
	}
	
	public static String getSurveyUriForInstanceAndDisplayContents(String formId, String instanceId) {
		return "content://org.opendatakit.common.android.provider.forms/"+ appName +"/" + formId + "/#instanceId=" + instanceId + "&screenPath=survey/_contents";
	}
	
	public static String getSurveyUriForInstance(String formId, String instanceId) {
		return "content://org.opendatakit.common.android.provider.forms/"+ appName +"/" + formId + "/#instanceId=" + instanceId;
	}
	
	public static String getTablesUriForInstance(String formId) {
	  return "assets/" + formId + "/html/" + formId + "_list.html";
	}
	
	public static String getTablesUriForInstanceWithScanOutputDir(String formId, String scanOutputDir) {
	  // Need to encode the scan_output_directory query parameter
	  String encodedScanOutputDir = UrlUtils.encodeSegment(scanOutputDir);
	  return "assets/" + formId + "/html/" + formId + ".html?scan_output_directory=" + encodedScanOutputDir;
	}
	
	public static String getXlsxConverterUri() {
		return "http:///localhost:8635/" + appName + "/xlsxconverter/conversion.html";
	}
	
	public static String getAppRelativeInstancesDirPath(String formId, String instancesDir) 
	{
		return "tables/" + formId + "/instances/" + instancesDir + "/";
	}

	public static String getOutputDirPath() {
		return  appFolder + "output/";
	}

	public static String getOutputPath(String photoName) {
		return getOutputDirPath() + photoName + "/";
	}

	public static String getPhotoPath(String photoName) {
		return getOutputPath(photoName) + "photo.jpg";
	}

	public static String getAlignedPhotoPath(String photoName) {
		return getOutputPath(photoName) + "aligned.jpg";
	}

	public static String getJsonPath(String photoName) {
		return getOutputPath(photoName) + "output.json";
	}

	public static String getMarkedupPhotoPath(String photoName) {
		return getOutputPath(photoName) + "markedup.jpg";
	}

	public static String getTemplateDirPath() {
		return appFolder + "form_templates/";
	}

	public static String getTrainingExampleDirPath() {
		return appFolder + "training_examples/";
	}

	public static String getCalibPath() {
		return appFolder + "camera.yml";
	}

	public static String getFormViewHTMLDir() {
		return appFolder + "transcription/";
	}
	
	public static String getTrainedModelDir(String classifier) {
		return appFolder + "training_models/" + classifier + "/";
	}
	
	public static String getNumberClassifierModel() {
		return getTrainedModelDir("numbers") + "mlp_all_classes.txt";
	}

	public static void displayImageInWebView(WebView myWebView, String imagePath) {
		myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		myWebView.getSettings().setBuiltInZoomControls(true);
		myWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		myWebView.setVisibility(View.VISIBLE);

		// HTML is used to display the image.
		String html = "<body bgcolor=\"White\">"
				+
				// "<p>" + + "</p>" +
				"<center> <img src=\"file:///" + imagePath
				+
				// Appending the time stamp to the filename is a hack to prevent
				// caching.
				"?" + new Date().getTime()
				+ "\" width=\"500\" > </center></body>";

		myWebView.loadDataWithBaseURL("file:///unnecessairy/", html,
				"text/html", "utf-8", "");
	}

	/**
	 * @param folder
	 * @return the available space in bytes.
	 */
	public static long getUsableSpace(String folder) {
		StatFs sfs = new StatFs(folder);
		return sfs.getAvailableBlocks() * sfs.getBlockSize();
	}
	public static String readFileAsString(String filePath)
			throws java.io.IOException {
		StringBuilder text = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
		String line;
		
		while ((line = br.readLine()) != null) {
			text.append(line);
		}
		
		br.close();

		return text.toString();
	}
	/**
	 * @param photoName
	 * @return the template path used to aligned/process the photo or null if
	 *         the template file can't be opened or doesn't exist
	 * @throws Exception
	 * @deprecated
	 */
	public static String getTemplatePath(String photoName) throws Exception {
		if (new File(getOutputPath(photoName)).exists()) {
			String templateValueFile = getOutputPath(photoName) + "template";
			if (new File(templateValueFile).exists()) {
				try {
					return readFileAsString(templateValueFile);
				} catch (IOException e) {
					throw new Exception(
							"Could not associate templatePath with photo: "
									+ photoName);
				}
			}
		}
		throw new Exception("Could not associate templatePath with photo: "
				+ photoName);
	}
	/**
	 * Save the templatePath for a given form photo in the "template" file in the photo's folder.
	 * @param photoName
	 * @param templatePath
	 * @throws IOException
	 * @deprecated
	 */
	public static void setTemplatePath(String photoName, String templatePath)
			throws IOException {
		String templateValueFile = getOutputPath(photoName) + "template";
		if (new File(templateValueFile).createNewFile()) {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					templateValueFile));
			out.write(templatePath);
			out.close();
		}
	}
}