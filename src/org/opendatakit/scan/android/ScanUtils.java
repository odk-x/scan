package org.opendatakit.scan.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.os.StatFs;

/**
 * ScanUtils contains methods and data shared across the application. For
 * example it is used to construct all the output filepaths.
 */
public class ScanUtils {
	// Prevent instantiations
	private ScanUtils() {
	}

	public static final boolean DebugMode = false;

	public static final String appFolder = "/sdcard/ODKScan/";

	public static String getOutputDirPath() {
		return appFolder + "output/";
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

		return text.toString();
	}
	/**
	 * @param photoName
	 * @return the template path used to aligned/process the photo or null if
	 *         the template file can't be opened or doesn't exist
	 * @throws Exception
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