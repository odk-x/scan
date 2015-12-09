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
package org.opendatakit.scan.android.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.opendatakit.common.android.utilities.ODKFileUtils;
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

  public static final String odk_file_system_sub_path = "scan";

  public static final boolean DebugMode = false;

  public static final String outputFolder = "scan_data";

  public static final String capturedPhotoName = "photo.jpg";

  public static final String alignedPhotoName = "aligned.jpg";

  public static final String markedupPhotoName = "markedup.jpg";

  public static final String outputJSONName = "output.json";

  public static final String templateDirName = "form_templates";

  public static final String trainingExampleDirName = "training_examples";

  public static final String trainingModelDirName = "training_models";

  public static final String numberModule = "mlp_all_classes.txt";

  public static final String calibName = "camera.yml";

  public static final String formViewHTMLDir = "transcription";

  public static final String extStorageDir = Environment.getExternalStorageDirectory()
      .getAbsolutePath();

  public static final String getODKAppName() {
    return appName;
  }

  // TODO: remove trailing slash
  public static String getAppFormDirPath(String formId) {
    return ODKFileUtils.getFormFolder(appName, formId, formId) + File.separator;
  }

  public static String getConfigPath() {
    return ODKFileUtils.getConfigFolder(appName) + File.separator + odk_file_system_sub_path;
  }

  public static String getSystemPath() {
    return ODKFileUtils.getSystemFolder(appName) + File.separator + odk_file_system_sub_path;
  }

  // TODO: remove trailing slash
  public static String getAppInstancesDirPath(String formId) {
    return ODKFileUtils.getInstancesFolder(appName, formId) + File.separator;
  }

  public static String getSurveyUriForInstanceAndDisplayContents(String tableId, String formId,
      String instanceId) {
    return "content://org.opendatakit.common.android.provider.forms/" + appName + "/" + tableId
        + "/" + formId + "/#instanceId=" + instanceId + "&screenPath=survey/_contents";
  }

  public static String getSurveyUriForInstance(String tableId, String formId, String instanceId) {
    return "content://org.opendatakit.common.android.provider.forms/" + appName + "/" + tableId
        + "/" + formId + "/#instanceId=" + instanceId;
  }

  // TODO: place this in the correct spot
  public static String getTablesUriForInstance(String formId) {
    return "assets/" + formId + "/html/" + formId + "_list.html";
  }

  // TODO: place this in the correct spot
  public static String getTablesUriForInstanceWithScanOutputDir(String formId,
      String scanOutputDir) {
    // Need to encode the scan_output_directory query parameter
    String encodedScanOutputDir = UrlUtils.encodeSegment(scanOutputDir);
    return "assets/" + formId + "/html/" + formId + ".html?scan_output_directory="
        + encodedScanOutputDir;
  }

  public static String getXlsxConverterUri() {
    return "http:///localhost:8635/" + appName + "/xlsxconverter/conversion.html";
  }

  // TODO: remove trailing slash
  public static String getAppRelativeInstancesDirPath(String formId, String instancesDir) {
    return ODKFileUtils.asRelativePath(appName,
        new File(ODKFileUtils.getInstanceFolder(appName, formId, instancesDir))) + File.separator;
  }

  // TODO: remove trailing slash
  public static String getOutputDirPath() {
    return ODKFileUtils.getDataFolder(appName) + File.separator + outputFolder;
  }

  public static String getOutputPath(String photoName) {
    return getOutputDirPath() + File.separator + photoName;
  }

  public static String getPhotoPath(String photoName) {
    return getOutputPath(photoName) + File.separator + capturedPhotoName;
  }

  public static String getAlignedPhotoPath(String photoName) {
    return getOutputPath(photoName) + File.separator + alignedPhotoName;
  }

  public static String getJsonPath(String photoName) {
    return getOutputPath(photoName) + File.separator + outputJSONName;
  }

  public static String getMarkedupPhotoPath(String photoName) {
    return getOutputPath(photoName) + File.separator + markedupPhotoName;
  }

  // TODO: Remove trailing slash
  public static String getTemplateDirPath() {
    return getConfigPath() + File.separator + templateDirName + File.separator;
  }

  // TODO: Remove trailing slash
  public static String getTrainingExampleDirPath() {
    return getSystemPath() + File.separator + trainingExampleDirName + File.separator;
  }

  public static String getCalibPath() {
    return getSystemPath() + File.separator + calibName;
  }

  // TODO: Remove trailing slash
  public static String getFormViewHTMLDir() {
    return getSystemPath() + File.separator + formViewHTMLDir + File.separator;
  }

  // TODO: Remove trailing slash
  public static String getTrainedModelDir() {
    return getSystemPath() + File.separator + trainingModelDirName + File.separator;
  }

  public static void displayImageInWebView(WebView myWebView, String imagePath) {
    myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    myWebView.getSettings().setBuiltInZoomControls(true);
    myWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
    myWebView.setVisibility(View.VISIBLE);

    // HTML is used to display the image.
    String html = "<body bgcolor=\"White\">" +
        // "<p>" + + "</p>" +
        "<center> <img src=\"file:///" + imagePath +
        // Appending the time stamp to the filename is a hack to prevent
        // caching.
        "?" + new Date().getTime() + "\" width=\"500\" > </center></body>";

    myWebView.loadDataWithBaseURL("file:///unnecessairy/", html, "text/html", "utf-8", "");
  }

  /**
   * @param folder
   * @return the available space in bytes.
   */
  public static long getUsableSpace(String folder) {
    StatFs sfs = new StatFs(folder);
    return (long) sfs.getAvailableBlocks() * sfs.getBlockSize();
  }

  public static String readFileAsString(String filePath) throws java.io.IOException {
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
   * the template file can't be opened or doesn't exist
   * @throws Exception
   * @deprecated
   */
  public static String getTemplatePath(String photoName) throws Exception {
    if (new File(getOutputPath(photoName)).exists()) {
      String templateValueFile = getOutputPath(photoName) + File.separator + "template";
      if (new File(templateValueFile).exists()) {
        try {
          return readFileAsString(templateValueFile);
        } catch (IOException e) {
          throw new Exception("Could not associate templatePath with photo: " + photoName);
        }
      }
    }
    throw new Exception("Could not associate templatePath with photo: " + photoName);
  }

  /**
   * Save the templatePath for a given form photo in the "template" file in the photo's folder.
   *
   * @param photoName
   * @param templatePath
   * @throws IOException
   * @deprecated
   */
  public static void setTemplatePath(String photoName, String templatePath) throws IOException {
    String templateValueFile = getOutputPath(photoName) + File.separator + "template";
    if (new File(templateValueFile).createNewFile()) {
      BufferedWriter out = new BufferedWriter(new FileWriter(templateValueFile));
      out.write(templatePath);
      out.close();
    }
  }
}
