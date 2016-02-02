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

package org.opendatakit.scan.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.common.android.activities.BaseActivity;
import org.opendatakit.common.android.data.ColumnList;
import org.opendatakit.common.android.data.OrderedColumns;
import org.opendatakit.common.android.data.UserTable;
import org.opendatakit.common.android.provider.DataTableColumns;
import org.opendatakit.common.android.utilities.DataTypeNamesToRemove;
import org.opendatakit.common.android.utilities.ODKFileUtils;
import org.opendatakit.database.service.OdkDbHandle;
import org.opendatakit.scan.utils.JSONUtils;
import org.opendatakit.scan.utils.ScanUtils;
import org.opendatakit.scan.application.Scan;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;

public class JSON2SurveyJSONActivity extends BaseActivity {

  private static final String TAG = "JSON2SurveyJSONActivity";
  private static final String LOG_TAG = "ODKScan JSON";

  private static final String scanOutputDir = "scan_output_directory";

  private static final String rawOutputFileName = "raw";

  private static final String APP_NAME = "appName";

  private static final String TABLE_ID = "tableId";

  private static final String FILE_NAME = "filename";

  private static final String TABLE_DISPLAY_TYPE = "tableDisplayViewType";

  private static final String TABLES_DISPLAY_LIST = "LIST";

  private static String xlsxFormId;

  private static final String customCssFileNameStr = "customStyles.css";

  private static int screenWidth;

  private static int screenHeight;

  private ArrayList<String> photoNames;

  // used to communicate info through databaseAvailable() call.
  private String rootTemplatePath;

  private String formId;

  WebView myWebView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();

    if (extras == null) {
      try {
        throw new Exception("No parameters specified");
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    String templatePath = extras.getString("templatePath");
    if (templatePath == null) {
      try {
        throw new Exception("Could not identify template.");
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    ArrayList<String> templatePaths = extras.getStringArrayList("prevTemplatePaths");
    if (templatePaths == null) {
      templatePaths = new ArrayList<String>(Arrays.asList(templatePath));
    } else {
      templatePaths.add(templatePath);
    }

    // If there are multiple pages, we want to get the formId from the root path
    rootTemplatePath = templatePath;
    if (templatePaths.size() > 1) {
      rootTemplatePath = templatePaths.get(0);
    }

    formId = getFormIdFromFormDef(rootTemplatePath);
    xlsxFormId = formId;

    String photoName = extras.getString("photoName");
    if (photoName == null) {
      try {
        throw new Exception("jsonOutPath is null");
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    photoNames = extras.getStringArrayList("prevPhotoNames");
    if (photoNames == null) {
      photoNames = new ArrayList<String>(Arrays.asList(photoName));
    } else {
      photoNames.add(photoName);
    }

    // String rootPhotoName = photoNames.get(0);
    Log.i(LOG_TAG, "photoNames : " + photoNames);

    // Get the screen size in case we need to
    // write out a css file
    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    screenWidth = (int) (displaymetrics.widthPixels / displaymetrics.scaledDensity);
    screenHeight = (int) (displaymetrics.heightPixels / displaymetrics.scaledDensity);

    // register to get databaseAvailable
    Scan.getInstance().establishDatabaseConnectionListener(this);

    // wait for databaseAvailable to do any further processing
  }

  @Override
  public void onPostResume() {

    super.onPostResume();
    Scan.getInstance().establishDatabaseConnectionListener(this);
  }

  /**
   * Check for formId in formDef.json for forms without subforms
   *
   * @param templatePath
   */
  public String getFormIdFromFormDef(String templatePath) {
    // Find out what the formId should be from the
    // formDef.json
    File formDef = null;
    String formIdFromFormDef = null;
    try {
      formDef = findFileThatEndsIn(templatePath, "formDef.json");
      JSONObject formDefObj = getJSONFromFile(formDef);
      formIdFromFormDef = formDefObj.getJSONObject("specification").getJSONObject("settings")
          .getJSONObject("form_id").getString("value");
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(LOG_TAG, "getFormIdFromFormDef: could not get the form id");
    }

    return formIdFromFormDef;
  }

  /**
   * Check for formId in formDef.json for forms without subforms
   *
   * @param templatePath
   * @param suffix
   */
  public File findFileThatEndsIn(String templatePath, String suffix) {
    // Find out what the formId should be from the
    // formDef.json
    File fileToReturn = null;
    File dir = new File(templatePath);
    if (!dir.isDirectory())
      throw new IllegalStateException("Template path is bad");
    for (File file : dir.listFiles()) {
      if (file.getName().endsWith(suffix)) {
        fileToReturn = file;
        return fileToReturn;
      }
    }
    return fileToReturn;
  }

  /**
   * Get formDef.json output for non-subform forms
   *
   * @param formDef
   * @return jsonOutput
   * @throws Exception
   */
  public JSONObject getJSONFromFile(File formDef) throws Exception {
    JSONObject jsonOutput = null;

    if (!formDef.isFile()) {
      throw new IllegalStateException("getJSONFromFile: use a valid file");
    }

    try {
      String jsonPath = formDef.getAbsolutePath();
      jsonOutput = JSONUtils.parseFileToJSONObject(jsonPath);
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(LOG_TAG, "Could not get JSON output for file " + formDef.getName());
    }

    return jsonOutput;
  }

  /**
   * Checking if there are sub_forms
   *
   * @param templatePath
   */
  public boolean checkForSubforms(
      String templatePath) { // TODO: How does this work with multipage forms?
    // Right now the assumption is only one subform
    // definition is possible
    boolean hasSubform = false;

    try {
      for (String photoName : photoNames) {
        if (JSONUtils.parseFileToJSONObject(ScanUtils.getJsonPath(photoName)).has("sub_forms")) {
          hasSubform = true;
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return hasSubform;
  }

  /**
   * Map a scan instance to a survey instance
   */
  public void mapScanInstanceToSurveyInstance(JSONObject field, String fieldNameToValidate,
      ContentValues tablesValues, boolean writeOutCustomCss, StringBuilder cssStr,
      StringBuilder dbValuesToWrite, File dirToMake, String dirId, String formId) {
    try {
      // The reason why the fieldName has to be passed in is for
      // subforms - the fieldName may be different from the
      // scan field object
      String fieldName = validate(fieldNameToValidate);
      JSONArray segments = field.optJSONArray("segments");
      if (segments == null) {
        segments = new JSONArray();
      }
      // Add segment images - Copy these files to the right location
      // and update their database value
      for (int j = 0; j < segments.length(); j++) {
        JSONObject segment = segments.getJSONObject(j);
        // Changed to get rid of underscore for Munjela's code
        String imageName = fieldName + "_image" + j;
        String imagePath = segment.getString("image_path");

        if (!segment.has("image_path") || segment.isNull("image_path")) {
          // I won't add any db value to write
          Log.i(LOG_TAG, "No image_path found " + imageName);
          continue;
        }

        String imageFileName = new File(imagePath).getName();
        int dotPos = imageFileName.lastIndexOf(".");
        String imageFileSubstr = imageFileName.substring(0, dotPos);
        String imageFileExt = imageFileName.substring(dotPos);

        // ---Copy segment image to the correct survey directory------
        InputStream fis = new FileInputStream(imagePath);
        File outputPicFile = new File(dirToMake.getAbsolutePath(),
            imageFileSubstr + "_" + dirId + imageFileExt);
        FileOutputStream fos = new FileOutputStream(outputPicFile.getAbsolutePath());
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = fis.read(buf)) > 0) {
          fos.write(buf, 0, len);
        }
        fos.close();
        fis.close();
        // ---End of copying the image

        // database changes require that images have a field named
        // image_uriFragment and image_contentType
        String imageName_uriFragment = imageName + "_uriFragment";
        String imageName_contentType = imageName + "_contentType";

        addStringValueToTableContentValue(tablesValues, imageName_uriFragment,
            outputPicFile.getName());
        addStringValueToTableContentValue(tablesValues, imageName_contentType, "image/jpg");

        // Add styling for this image in the css file if no css file is found
        if (writeOutCustomCss) {
          int segHeight = segment.getInt("segment_height");
          int segWidth = segment.getInt("segment_width");
          cssStr.append("#").append(imageName).append("{\n");
          boolean segWidthGreater = segWidth > screenWidth ? true : false;
          boolean segHeightGreater = segHeight > screenHeight ? true : false;

          if (segWidthGreater || segHeightGreater) {
            if (segWidthGreater) {
              cssStr.append("max-width:");
            } else {
              cssStr.append("max-height:");
            }
            cssStr.append("100%").append(";\n");

          } else {
            cssStr.append("width:").append(segWidth).append("px;\n");
            cssStr.append("height:").append(segHeight).append("px;\n");
          }
          cssStr.append("}\n");
        }
      }
      // Add the data for field
      if (field.has("value")) {
        if (field.getString("type").equals("int") || field.getString("type").equals("tally")) {
          tablesValues.put(fieldName, field.getInt("value"));
          dbValuesToWrite.append(fieldName).append("=").append(field.getInt("value"));
          // This will need to be addressed correctly
        } else if (field.getString("type").equals("select_many")) {
          // Need to parse this to get multiple values if there are any
          // and write them into the array as appropriate
          String scanValue = field.getString("value");
          if (scanValue.length() > 0) {
            String space = " ";
            String comma = ",";
            String surveyValue = "[";

            int index = scanValue.indexOf(space);
            int startInd = 0;
            String interimSurveyValue;
            while (index >= 0 && index < scanValue.length()) {
              interimSurveyValue = scanValue.substring(startInd, index);
              surveyValue = surveyValue + "\"" + interimSurveyValue + "\",";
              startInd = index + 1;
              index = scanValue.indexOf(space, startInd);
            }

            if (startInd < scanValue.length()) {
              interimSurveyValue = scanValue.substring(startInd);
              surveyValue = surveyValue + "\"" + interimSurveyValue + "\"";
            }

            // Strip off extra comma
            int lastInd = surveyValue.length() - 1;
            if (surveyValue.lastIndexOf(comma) == lastInd) {
              surveyValue = surveyValue.substring(0, lastInd);
            }

            surveyValue = surveyValue + "]";
            addStringValueToTableContentValue(tablesValues, fieldName, surveyValue);
            dbValuesToWrite.append(fieldName).append("=").append(field.getString("value"));
          }
        } else {
          // Check if the string is empty - if it is don't write anything out
          String value = field.getString("value");
          if (value.length() > 0) {
            addStringValueToTableContentValue(tablesValues, fieldName, field.getString("value"));
            dbValuesToWrite.append(fieldName).append("=").append(field.getString("value"));
          }
        }
      } else if (field.has("default")) {
        if (field.getString("type").equals("int")) {
          tablesValues.put(fieldName, field.getInt("default"));
          dbValuesToWrite.append(fieldName).append("=").append(field.getInt("default"));
        } else {
          addStringValueToTableContentValue(tablesValues, fieldName, field.getString("default"));
          dbValuesToWrite.append(fieldName).append("=").append(field.getString("default"));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(LOG_TAG, "Could not map Scan instance to Survey instace");
    }

  }

  /**
   * Add a survey instance into the database
   *
   * @param formId
   */
  public synchronized void createSurveyInstance(String formId) {
    ContentValues tablesValues = new ContentValues();

    String tableId = formId;

    String rowId = null;
    OdkDbHandle db = null;
    OrderedColumns orderedColumns = null;

    StringBuilder dbValuesToWrite = new StringBuilder();
    String uuidStr = UUID.randomUUID().toString();

    // scanOutputDir is not a uriFragment it is an app-relative path.
    String uniqueScanImageFolder = ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1));
    // but we want to make this app-relative
    String appRelativeUniqueScanImageFolder =
        ODKFileUtils.asUriFragment(getAppName(), new File(uniqueScanImageFolder));

    try {
      if (tableId == null) {
        throw new Exception("tableId cannot be blank!!");
      }

      // Get the fields from the output.json file that need to be created in the
      // database table
      JSONArray fields = new JSONArray();
      for (String photoName : photoNames) {
        JSONArray photoFields = JSONUtils.parseFileToJSONObject(ScanUtils.getJsonPath(photoName))
            .getJSONArray("fields");
        int photoFieldsLength = photoFields.length();
        for (int i = 0; i < photoFieldsLength; i++) {
          fields.put(photoFields.get(i));
        }
        Log.i(LOG_TAG, "Concated " + photoName);
      }

      int fieldsLength = fields.length();
      if (fieldsLength == 0) {
        throw new JSONException("There are no fields in the json output file.");
      }

      db = Scan.getInstance().getDatabase().openDatabase(ScanUtils.getODKAppName());

      orderedColumns = Scan.getInstance().getDatabase()
          .getUserDefinedColumns(ScanUtils.getODKAppName(), db, tableId);

      String selection = scanOutputDir + "=?";
      String[] selectionArgs = { appRelativeUniqueScanImageFolder };
      String[] empty = {};
      UserTable data = Scan.getInstance().getDatabase()
          .rawSqlQuery(ScanUtils.getODKAppName(), db, tableId, orderedColumns, selection,
              selectionArgs, empty, null, null, null);

      // Check if the instance already exists in survey
      if (data.getNumberOfRows() >= 1) {
        String foundUuidStr = data.getRowAtIndex(0)
            .getRawDataOrMetadataByElementKey(DataTableColumns.ID);
        setIntentToReturn(tableId, formId, foundUuidStr, RESULT_OK);
        finish();
        return;
      }

      Log.i(LOG_TAG, "Transfering the values from the JSON output into the survey instance");

      // Have to address multiple page scans
      // Not sure what this means for survey
      File dirToMake = null;
      String dirId = null;
      StringBuilder cssStr = new StringBuilder();
      String cssDir = ScanUtils.getAppFormDirPath(formId);

      boolean writeOutCustomCss = false;
      if (fieldsLength > 0) {
        // manufacture a rowId for this record...
        // for directory name to store the image files
        rowId = "uuid:" + uuidStr;

        dirToMake = new File(
            ODKFileUtils.getInstanceFolder(ScanUtils.getODKAppName(), formId, rowId));
        dirId = dirToMake.getAbsolutePath()
            .substring(dirToMake.getAbsolutePath().lastIndexOf("/") + 1);

        File customCssFile = new File(cssDir + customCssFileNameStr);
        if (!customCssFile.exists()) {
          writeOutCustomCss = true;
        }
      }
      for (int i = 0; i < fieldsLength; i++) {
        JSONObject field = fields.optJSONObject(i);

        mapScanInstanceToSurveyInstance(field, field.getString("name"), tablesValues,
            writeOutCustomCss, cssStr, dbValuesToWrite, dirToMake, dirId, formId);
      }

      // Copy raw output values
      if (fieldsLength > 0) {
        String fullFileName = rawOutputFileName + "_" + dirId + ".json";
        InputStream fis = new FileInputStream(
            ScanUtils.getJsonPath(photoNames.get(photoNames.size() - 1)));
        File outputFile = new File(dirToMake.getAbsolutePath(), fullFileName);
        FileOutputStream fos = new FileOutputStream(outputFile.getAbsolutePath());
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = fis.read(buf)) > 0) {
          fos.write(buf, 0, len);
        }
        fos.close();
        fis.close();
        // ---End of copying

        // database changes require that images have a field named
        // image_uriFragment and image_contentType
        String rawOutputFileName_uriFragment = rawOutputFileName + "_uriFragment";
        String rawOutputFileName_contentType = rawOutputFileName + "_contentType";

        String tempPath = ODKFileUtils.asRowpathUri(getAppName(), tableId, rowId, outputFile);

        tablesValues.put(rawOutputFileName_uriFragment, tempPath);
        tablesValues.put(rawOutputFileName_contentType, "application/json");
      }

      if (tablesValues.size() > 0) {
        // Add scan metadata here for the photo taken
        tablesValues.put(scanOutputDir, appRelativeUniqueScanImageFolder);

        if (writeOutCustomCss) {
          writeOutToFile(cssDir, customCssFileNameStr, cssStr.toString());
        }

        Log.i(LOG_TAG,
            "Writing db values for row:" + rowId + " values:" + dbValuesToWrite.toString());
        Scan.getInstance().getDatabase()
            .insertRowWithId(ScanUtils.getODKAppName(), db, tableId, orderedColumns, tablesValues,
                rowId);
      }
      setIntentToReturn(tableId, formId, rowId, RESULT_OK);
    } catch (Exception e) {
      // there was a problem -- report incomplete action
      setIntentToReturn(tableId, formId, rowId, RESULT_CANCELED);
      e.printStackTrace();
      Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableId);
    } finally {
      if (db != null) {
        try {
          Scan.getInstance().getDatabase().closeDatabase(ScanUtils.getODKAppName(), db);
        } catch (RemoteException e) {
          e.printStackTrace();
          Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableId);
        }
      }
    }
    finish();
  }

  /**
   * This is essentially the same things as the as the createSurveyInstance - I
   * am just doing this for a quick demo These things will have to be cleaned
   * up!!!
   *
   * @param templatePath
   */
  public void createSurveyInstanceBasedOnFormDesignerForms(String templatePath) {
    ContentValues tablesValues;
    String subformId = null;
    String tableId = null;
    OdkDbHandle db = null;
    String rowId = null;
    OrderedColumns orderedColumns = null;
    try {
      db = Scan.getInstance().getDatabase().openDatabase(ScanUtils.getODKAppName());
      // This code will only handle one subform currently
      // It will break otherwise
      JSONArray subforms = new JSONArray();

      // Get the fields from the output.json file that need to be created in the
      // database table
      JSONArray fields = new JSONArray();

      /*
       * for(String photoName : photoNames){ JSONArray photoFields =
       * JSONUtils.parseFileToJSONObject
       * (ScanUtils.getJsonPath(photoName)).getJSONArray("fields"); int
       * photoFieldsLength = photoFields.length(); for(int i = 0; i <
       * photoFieldsLength; i++){ fields.put(photoFields.get(i)); }
       * Log.i(LOG_TAG, "Concated " + photoName); }
       */

      for (String photoName : photoNames) {
        // Getting subforms
        if (JSONUtils.parseFileToJSONObject(ScanUtils.getJsonPath(photoName)).has("sub_forms")) {
          JSONArray photoSubforms = JSONUtils
              .parseFileToJSONObject(ScanUtils.getJsonPath(photoName)).getJSONArray("sub_forms");
          int photoSubformsLength = photoSubforms.length();
          for (int i = 0; i < photoSubformsLength; i++) {
            subforms.put(photoSubforms.get(i));
          }
          Log.i(LOG_TAG, "Concated subforms for " + photoName);
        }

        // Getting fields
        JSONArray photoFields = JSONUtils.parseFileToJSONObject(ScanUtils.getJsonPath(photoName))
            .getJSONArray("fields");
        int photoFieldsLength = photoFields.length();
        for (int i = 0; i < photoFieldsLength; i++) {
          fields.put(photoFields.get(i));
        }
        Log.i(LOG_TAG, "Concated " + photoName);
      }

      int subformsLength = subforms.length();

      // We are only able to handle one subform for now
      if (subformsLength > 1) {
        Log.i(LOG_TAG, "Using more than one subform has not been implemented yet");
        return;
      }

      subformId = subforms.getJSONObject(0).getString("name");
      subformId = "scan_" + subformId;

      tableId = subformId;

      int fieldsLength = fields.length();
      if (fieldsLength == 0) {
        throw new JSONException("There are no fields in the json output file.");
      }

      // create or verify that the table matches our table definition.
      orderedColumns = Scan.getInstance().getDatabase().getUserDefinedColumns(ScanUtils
          .getODKAppName(), db, tableId);

      // Check if the instance already exists in survey
      String selection = scanOutputDir + "=?";
      String[] selectionArgs = { ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1)) };
      String[] empty = {};
      UserTable data = Scan.getInstance().getDatabase()
          .rawSqlQuery(ScanUtils.getODKAppName(), db, tableId, orderedColumns, selection,
              selectionArgs, empty, null, null, null);

      if (data.getNumberOfRows() >= 1) {
        String foundUuidStr = data.getRowAtIndex(0)
            .getRawDataOrMetadataByElementKey(DataTableColumns.ID);
        //String uriStr = ScanUtils
        //    .getSurveyUriForInstanceAndDisplayContents(subformId, foundUuidStr);
        setIntentToReturn(tableId, subformId, foundUuidStr, RESULT_OK);
        finish();
        return;
      }

      Log.i(LOG_TAG, "Transfering the values from the JSON output into the survey instance");

      // Have to address multiple page scans
      // Not sure what this means for survey
      File dirToMake = null;
      String dirId = null;
      String cssDir = ScanUtils.getAppFormDirPath(subformId);
      StringBuilder cssStr;
      StringBuilder dbValuesToWrite;

      boolean writeOutCustomCss = false;
      if (fieldsLength > 0) {
        // We used to manufacture a rowId here and create
        // the appropriate directories - this is being done later
        // in the code now
        // Now this is being done while looping through the subgroups

        File customCssFile = new File(cssDir + customCssFileNameStr);
        if (!customCssFile.exists()) {
          writeOutCustomCss = true;
        }
      }

      JSONArray subformGroups = subforms.getJSONObject(0).getJSONArray("groups");
      JSONArray subformFieldNames = subforms.getJSONObject(0).getJSONObject("fields").names();

      // Going to loop through subform items instead of fields now
      for (int i = 0; i < subformGroups.length(); i++) {
        tablesValues = new ContentValues();
        cssStr = new StringBuilder();
        dbValuesToWrite = new StringBuilder();

        // For each subgroup -
        // manufacture a rowId for this record...
        // for directory name to store the image files
        String uuidStr = UUID.randomUUID().toString();
        rowId = "uuid:" + uuidStr;
        dirToMake = new File(
            ODKFileUtils.getInstanceFolder(ScanUtils.getODKAppName(), subformId, rowId));
        dirId = dirToMake.getAbsolutePath()
            .substring(dirToMake.getAbsolutePath().lastIndexOf("/") + 1);

        JSONObject group = subformGroups.getJSONObject(i);
        for (int k = 0; k < subformFieldNames.length(); k++) {
          String subformFieldName = subformFieldNames.getString(k);

          if (group.isNull(subformFieldName)) {
            continue;
          }
          String scanFieldName = group.getString(subformFieldName);

          for (int m = 0; m < fieldsLength; m++) {
            JSONObject field = fields.optJSONObject(m);
            if (scanFieldName.equals(field.getString("name"))) {
              mapScanInstanceToSurveyInstance(field, subformFieldName, tablesValues,
                  writeOutCustomCss, cssStr, dbValuesToWrite, dirToMake, dirId, subformId);
              break;
            }
          }
        }

        // Copy raw output values
        if (fieldsLength > 0) {
          String fullFileName = rawOutputFileName + "_" + dirId + ".json";
          InputStream fis = new FileInputStream(
              ScanUtils.getJsonPath(photoNames.get(photoNames.size() - 1)));
          File outputFile = new File(dirToMake.getAbsolutePath(), fullFileName);
          FileOutputStream fos = new FileOutputStream(outputFile.getAbsolutePath());
          // Transfer bytes from in to out
          byte[] buf = new byte[1024];
          int len;
          while ((len = fis.read(buf)) > 0) {
            fos.write(buf, 0, len);
          }
          fos.close();
          fis.close();
          // ---End of copying

          // database changes require that images have a field named
          // image_uriFragment and image_contentType
          String rawOutputFileName_uriFragment = rawOutputFileName + "_uriFragment";
          String rawOutputFileName_contentType = rawOutputFileName + "_contentType";

          tablesValues.put(rawOutputFileName_uriFragment, fullFileName);
          tablesValues.put(rawOutputFileName_contentType, "application/json");
        }

        // For each subgroup check if it is ready to be written out or not
        if (tablesValues.size() > 0) {
          // Add scan metadata here for the photo taken
          tablesValues
              .put(scanOutputDir, ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1)));

          if (writeOutCustomCss) {
            writeOutToFile(cssDir, customCssFileNameStr, cssStr.toString());
            writeOutCustomCss = false;
          }

          Log.i(LOG_TAG,
              "Writing db values for row:" + rowId + " values:" + dbValuesToWrite.toString());
          Scan.getInstance().getDatabase()
              .insertRowWithId(ScanUtils.getODKAppName(), db, tableId, orderedColumns, tablesValues,
                  rowId);
        }
      }
      setIntentToReturn(tableId, subformId, rowId, RESULT_OK);
    } catch (Exception e) {
      // indicate that the processing did not complete successfully
      setIntentToReturn(tableId, subformId, rowId, RESULT_CANCELED);
      e.printStackTrace();
      Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableId);
    } finally {
      if (db != null) {
        try {
          Scan.getInstance().getDatabase().closeDatabase(ScanUtils.getODKAppName(), db);
        } catch (RemoteException e) {
          e.printStackTrace();
          Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableId);
        }
      }
    }
    finish();
  }

  /**
   * Function to check whether or not to write a String value into ContentValues
   * - we don't want to insert empty strings
   *
   * @param tableValue
   * @param key
   * @param value
   */
  public void addStringValueToTableContentValue(ContentValues tableValue, String key,
      String value) {
    if (value != "") {
      tableValue.put(key, value);
    }
  }

  /**
   * Write out string data to a file in a given directory
   *
   * @param directory
   * @param fileName
   * @param data
   */
  public static void writeOutToFile(String directory, String fileName, String data) {
    File dirToMake = new File(directory);
    dirToMake.mkdirs();
    String instanceFilePath = new File(dirToMake.getAbsolutePath(), fileName).getAbsolutePath();

    try {
      FileWriter finalWriter;
      File finalJson = new File(instanceFilePath);
      finalJson.createNewFile();
      finalWriter = new FileWriter(finalJson);
      finalWriter.write(data);
      finalWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Check that the string is a valid xml tag
   *
   * @param string
   * @throws Exception
   */
  private static String validate(String string) throws Exception {
    if (Pattern.matches("[a-zA-Z][a-zA-Z_0-9]*", string)) {
      return string;
    } else {
      throw new Exception("Field name cannot be used in xform: " + string);
    }
  }

  /**
   * Check if any of the provided file paths were modified after the given date.
   * <p/>
   * This function may be useful when checking for version issues?
   *
   * @param tableId
   * @param formId
   * @param rowId
   * @param resultCode e.g., RESULT_OK
   */
  private void setIntentToReturn(String tableId, String formId, String rowId, int resultCode) {

    Intent intent = new Intent();

    /* Uncomment to launch Tables
    Bundle args = new Bundle();
    args.putString(APP_NAME, ScanUtils.getODKAppName());
    args.putString(TABLE_ID, formId);
    args.putString(FILE_NAME, ScanUtils.getTablesUriForInstanceWithScanOutputDir(formId, ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1))));
    args.putString(TABLE_DISPLAY_TYPE, TABLES_DISPLAY_LIST);
    intent.putExtras(args);
    */

    // Launch Survey
    intent.setData(
        Uri.parse(ScanUtils.getSurveyUriForInstanceAndDisplayContents(tableId, formId, rowId)));

    setResult(resultCode, intent);
  }

  public void databaseAvailable() {
    if (Scan.getInstance().getDatabase() != null) {

      // We are going to start using the FormDesigner's XLSX conversion files
      // First we are going to check if the file exist - formDef.json
      // If it does then we are going to go to a completely separate function for
      // now
      // Essentially this function will do the following things
      // 1. Check if there is a subform in the directory
      // 3. Loop through the output.json and create survey instances for the
      // subforms
      // 4. Write that data to the database
      // 5. Move only the subform files over to the proper tables directory
      // 6. Launch Tables - there will have to be an index.html defined for the
      // forms
      // or the user will have to know that they need to go to the appropriate
      // name and
      // view their detail and list view forms.
      if (checkForSubforms(rootTemplatePath)) {
        createSurveyInstanceBasedOnFormDesignerForms(rootTemplatePath);
        return;
      } else {
        // Check if there is a registered Survey instance or create one
        createSurveyInstance(formId);
      }
    }
  }

  public void databaseUnavailable() {
    // TODO Auto-generated method stub

  }

  public String getAppName() {
    return ScanUtils.getODKAppName();
  }
}
