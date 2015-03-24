package org.opendatakit.scan.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.opendatakit.common.android.utilities.DataTypeNamesToRemove;
import org.opendatakit.common.android.utilities.ODKFileUtils;
import org.opendatakit.database.service.OdkDbHandle;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;

public class JSON2SurveyJSON extends BaseActivity {

  private static final String TAG = "JSON2SurveyJSON";
  private static final String LOG_TAG = "ODKScan";

  private static final String scanOutputDir = "scan_output_directory";
  
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
  private String templatePath;
  
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

    templatePath = extras.getString("templatePath");
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

    // String rootTemplatePath = templatePaths.get(0);

    // Set the formId to use for this newly created form
    // We used to just take the directory name from the
    // templatePath and add scan to make for formId name
    // in the case of forms without subforms - Now we are
    // getting the name from the generated formDef.json
    /*
     * String formId = new File(templatePaths.get(0)).getName(); formId =
     * "scan_" + formId; xlsxFormId = formId;
     */
    formId = getFormIdFromFormDef(templatePath);
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
    
    // wait for databaseAvailable to do any further processing
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
  public boolean checkForSubforms(String templatePath) {
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
   * Creating the database table needed from the information provided by the
   * Scan app
   * 
   * @param db
   * @param tableName
   * @param subformFieldsToProcess
   */
  public OrderedColumns createSurveyTables(OdkDbHandle db, String tableName,
      JSONArray fieldsToProcess) {
    List<Column> columns = new ArrayList<Column>();

    // Always add the scan output directory to the table definition
    // This is used to map a Survey instance with a Scan photo
    columns.add(new Column(scanOutputDir, scanOutputDir, ElementDataType.string.name(), "[]"));

    OrderedColumns orderedColumns = null;

    try {
      int fieldsLength = fieldsToProcess.length();
      for (int i = 0; i < fieldsLength; i++) {
        JSONObject field = fieldsToProcess.optJSONObject(i);
        // Not sure how best to deal with notes?
        String fieldName = validate(field.getString("name"));

        JSONArray segments = field.optJSONArray("segments");
        if (segments == null) {
          segments = new JSONArray();
        }
        // Add segment images to columns
        for (int j = 0; j < segments.length(); j++) {
          JSONObject segment = segments.getJSONObject(j);

          // Should I check the image_path for the
          // segment before including it - I don't think so
          if (segment != null) {
            String imageName = fieldName + "_image" + j;
            columns.add(new Column(imageName, imageName, DataTypeNamesToRemove.MIMEURI, "[\""
                + imageName + "_uriFragment\",\"" + imageName + "_contentType\"]"));
            columns.add(new Column(imageName + "_uriFragment", "uriFragment",
                ElementDataType.rowpath.name(), "[]"));
            columns.add(new Column(imageName + "_contentType", "contentType",
                ElementDataType.string.name(), "[]"));
          }
        }

        // Add column for field
        String type = field.getString("type").toUpperCase(Locale.US);
        if (type.equals("INT")) {
          columns.add(new Column(fieldName, fieldName, ElementDataType.integer.name(), "[]"));
        } else if (type.equals("FLOAT")) {
          columns.add(new Column(fieldName, fieldName, ElementDataType.number.name(), "[]"));
        } else {
          columns.add(new Column(fieldName, fieldName, ElementDataType.string.name(), "[]"));
        }
      }

      // Create the database table with the columns
      // TODO: Not have this hardcoded to the appName of tables
      orderedColumns = Scan.getInstance().getDatabase().createOrOpenDBTableWithColumns(
          ScanUtils.getODKAppName(), db, tableName, new ColumnList(columns));
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(LOG_TAG, "Error - Could NOT create table " + tableName + " with columns");
      throw new IllegalArgumentException("Unable to create table");
    }
    return orderedColumns;
  }

  /**
   * Creating the database table needed from the information provided by the
   * form designer. This is used for the subforms case since we can't loop
   * through it as we normally would.
   * 
   * @param db
   * @param tableName
   * @param subformFieldsToProcess
   */
  public OrderedColumns createSurveyTablesFromFormDesigner(OdkDbHandle db,
      String tableName, JSONObject subformFieldsToProcess) {
    List<Column> columns = new ArrayList<Column>();

    // Always add the scan output directory to the table definition
    // This is used to map a Survey instance with a Scan photo
    columns.add(new Column(scanOutputDir, scanOutputDir, ElementDataType.string.name(), "[]"));

    OrderedColumns orderedColumns = null;

    try {
      JSONArray subformFieldNames = subformFieldsToProcess.names();

      for (int i = 0; i < subformFieldNames.length(); i++) {
        String field = subformFieldNames.getString(i);
        // Not sure how best to deal with notes?
        String fieldName = validate(field);

        // We are now making the assumption that all
        // the accepted fields will have only ONE image
        String imageName = fieldName + "_image0";

        // Add column for field
        // It may be better to have ODKFormDesignerDefinedTypes
        // this would be nice to do once the FormDesigner is stable
        String type = subformFieldsToProcess.getString(fieldName);
        if (type.equals("integer")) {
          columns.add(new Column(fieldName, fieldName, ElementDataType.integer.name(), "[]"));
        } else if (type.equals("decimal")) {
          columns.add(new Column(fieldName, fieldName, ElementDataType.number.name(), "[]"));
        } else {
          columns.add(new Column(fieldName, fieldName, ElementDataType.string.name(), "[]"));
        }

        columns.add(new Column(imageName, imageName, DataTypeNamesToRemove.MIMEURI, "[\""
            + imageName + "_contentType\",\"" + imageName + "_uriFragment\"]"));
        columns.add(new Column(imageName + "_contentType", "contentType", ElementDataType.string
            .name(), "[]"));
        columns.add(new Column(imageName + "_uriFragment", "uriFragment", ElementDataType.rowpath
            .name(), "[]"));
      }

      // Create the database table with the columns
      // TODO: Not have this hardcoded to the appName of tables
      orderedColumns = Scan.getInstance().getDatabase().createOrOpenDBTableWithColumns(
          ScanUtils.getODKAppName(), db, tableName, new ColumnList(columns));
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(LOG_TAG, "Error - Could NOT create subform table " + tableName + " with columns");
      throw new IllegalArgumentException("Unable to create subform table");
    }
    return orderedColumns;
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
        File outputPicFile = new File(dirToMake.getAbsolutePath(), imageFileSubstr + "_" + dirId
            + imageFileExt);
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

        addStringValueToTableContentValue(tablesValues, imageName_uriFragment, outputPicFile.getName());
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
  public void createSurveyInstance(String formId) {
    ContentValues tablesValues = new ContentValues();

    String tableName = formId;

    String rowId = null;
    OdkDbHandle db = null;
    OrderedColumns orderedColumns = null;

    StringBuilder dbValuesToWrite = new StringBuilder();
    String uuidStr = UUID.randomUUID().toString();
    boolean successful = false;
    try {
      db = Scan.getInstance().getDatabase().openDatabase( ScanUtils.getODKAppName(), true);

      if (tableName == null) {
        throw new Exception("formId cannot be blank!!");
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

      List<String> tableIds = Scan.getInstance().getDatabase().getAllTableIds(ScanUtils.getODKAppName(), db);
      if ( !tableIds.contains(tableName) ) {
        Log.i(LOG_TAG, "No table definition found for " + tableName
            + ". Creating new table definition");
        orderedColumns = createSurveyTables(db, tableName, fields);
      } else {
        orderedColumns = Scan.getInstance().getDatabase().getUserDefinedColumns(ScanUtils.getODKAppName(), db, 
            tableName);
      }

      String selection = scanOutputDir + "=?";
      String[] selectionArgs = { ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1)) };
      String[] empty = {};
      UserTable data = Scan.getInstance().getDatabase().rawSqlQuery(ScanUtils.getODKAppName(), db,
          tableName, orderedColumns, selection, selectionArgs, empty,  null, null, null);

      // Check if the instance already exists in survey
      if (data.getNumberOfRows() >= 1) {
        //String foundUuidStr = data.getRowAtIndex(0).getRawDataOrMetadataByElementKey(DataTableColumns.ID);
        //String uriStr = ScanUtils.getSurveyUriForInstanceAndDisplayContents(formId, foundUuidStr);
        setIntentToReturn(formId);
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

        dirToMake = new File(ODKFileUtils.getInstanceFolder(ScanUtils.getODKAppName(),
            formId, rowId));
        dirId = dirToMake.getAbsolutePath().substring(
            dirToMake.getAbsolutePath().lastIndexOf("/") + 1);

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

      if (tablesValues.size() > 0) {
        // Add scan metadata here for the photo taken
        tablesValues.put(scanOutputDir,
            ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1)));
        Log.i(LOG_TAG,
            "Writing db values for row:" + rowId + " values:" + dbValuesToWrite.toString());
        Scan.getInstance().getDatabase().insertDataIntoExistingDBTableWithId(ScanUtils.getODKAppName(), db, tableName, orderedColumns,
            tablesValues, rowId);

        if (writeOutCustomCss) {
          writeOutToFile(cssDir, customCssFileNameStr, cssStr.toString());
        }
      }
      successful = true;
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableName);
    } finally {
      if (db != null) {
        try {
          Scan.getInstance().getDatabase().closeTransactionAndDatabase(ScanUtils.getODKAppName(), db, successful);
        } catch (RemoteException e) {
          e.printStackTrace();
          Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableName);
        }
      }
    }

    // String uriStr = ScanUtils.getSurveyUri(formId) + rowId;
    //String uriStr = ScanUtils.getSurveyUriForInstanceAndDisplayContents(formId, rowId);
    setIntentToReturn(formId);
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
    String tableName = null;
    OdkDbHandle db = null;
    String rowId = null;
    OrderedColumns orderedColumns = null;
    boolean successful = false;
    try {
      db = Scan.getInstance().getDatabase().openDatabase(ScanUtils.getODKAppName(), true);
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
          JSONArray photoSubforms = JSONUtils.parseFileToJSONObject(
              ScanUtils.getJsonPath(photoName)).getJSONArray("sub_forms");
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

      tableName = subformId;

      int fieldsLength = fields.length();
      if (fieldsLength == 0) {
        throw new JSONException("There are no fields in the json output file.");
      }

      // For now assume that we are not creating main forms' tables or instances
      // This is for the creation of a subform db table
      List<String> tableIds = Scan.getInstance().getDatabase().getAllTableIds(ScanUtils.getODKAppName(), db);
      if ( !tableIds.contains(tableName) ) {
        Log.i(LOG_TAG, "No table definition found for " + tableName
            + ". Creating new table definition");
        orderedColumns = createSurveyTablesFromFormDesigner(db, tableName, subforms
            .getJSONObject(0).getJSONObject("fields"));
      } else {
        orderedColumns = Scan.getInstance().getDatabase().getUserDefinedColumns(ScanUtils.getODKAppName(), db,
            tableName);
      }

      // Check if the instance already exists in survey
      String selection = scanOutputDir + "=?";
      String[] selectionArgs = { ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1)) };
      String[] empty = {};
      UserTable data = Scan.getInstance().getDatabase().rawSqlQuery(ScanUtils.getODKAppName(), db,
          tableName, orderedColumns, selection, selectionArgs, empty,  null, null, null);
      
      if ( data.getNumberOfRows() >= 1 ) {
        // String foundUuidStr = data.getRowAtIndex(0).getRawDataOrMetadataByElementKey(DataTableColumns.ID);
        // String uriStr = ScanUtils.getSurveyUri(subformId) + foundUuidStr;
        //String uriStr = ScanUtils
        //    .getSurveyUriForInstanceAndDisplayContents(subformId, foundUuidStr);
        setIntentToReturn(subformId);
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
        dirToMake = new File(ODKFileUtils.getInstanceFolder(ScanUtils.getODKAppName(),
            subformId, rowId));
        dirId = dirToMake.getAbsolutePath().substring(
            dirToMake.getAbsolutePath().lastIndexOf("/") + 1);

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

        // For each subgroup check if it is ready to be written out or not
        if (tablesValues.size() > 0) {
          // Add scan metadata here for the photo taken
          tablesValues.put(scanOutputDir,
              ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1)));
          Log.i(LOG_TAG,
              "Writing db values for row:" + rowId + " values:" + dbValuesToWrite.toString());
          Scan.getInstance().getDatabase().insertDataIntoExistingDBTableWithId(ScanUtils.getODKAppName(), db, tableName, orderedColumns,
              tablesValues, rowId);

          if (writeOutCustomCss) {
            writeOutToFile(cssDir, customCssFileNameStr, cssStr.toString());
            writeOutCustomCss = false;
          }
          // Move only formDef.json over for now if it doesn't exist already
          String directoryForSurveyFormDef = ScanUtils.getAppFormDirPath(subformId);
          File surveyFormDef = new File(directoryForSurveyFormDef, "formDef.json");
          if (!surveyFormDef.exists()) {
            String jsonPath = new File(templatePath, subformId + "_formDef.json").getAbsolutePath();
            String val = JSONUtils.parseFileToJSONObject(jsonPath).toString();
            writeOutToFile(ScanUtils.getAppFormDirPath(subformId), "formDef.json", val);
          }
        }
      }
      successful = true;
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableName);
    } finally {
      if (db != null) {
        try {
          Scan.getInstance().getDatabase().closeTransactionAndDatabase(ScanUtils.getODKAppName(), db, successful);
        } catch (RemoteException e) {
          e.printStackTrace();
          Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableName);
        }
      }
    }

    // Return uri
    //String uriStr = ScanUtils.getSurveyUriForInstanceAndDisplayContents(subformId, rowId);
    setIntentToReturn(subformId);
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
  public void addStringValueToTableContentValue(ContentValues tableValue, String key, String value) {
    if (value != "") {
      tableValue.put(key, value);
    }
  }

  /**
   * Callback function for after XLSXConverter JavaScript returns this was used
   * with the buildSurveyFormDef function
   * 
   * @param val
   * @deprecated
   */
  public void javascriptCallFinished(String val) {
    Log.i(LOG_TAG, "The formDef.json from xlsxconverter is" + val);
    writeOutToFile(ScanUtils.getAppFormDirPath(xlsxFormId), "formDef.json", val);
    createSurveyInstance(xlsxFormId);
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
   * Builds an XFrom from a JSON template and writes it out to the specified
   * file. It builds it as a string which really isn't the best way to go.
   * 
   * @param templatePaths
   * @return jsonOutputString
   * @throws Exception
   * @deprecated
   */
  public static String buildSurveyFormDef(ArrayList<String> templatePaths) throws Exception {

    String jsonOutputString = "";
    String title = new File(templatePaths.get(0)).getName();
    String id = "scan_" + title;

    JSONArray initFields = new JSONArray();
    for (String templatePath : templatePaths) {
      String jsonPath = new File(templatePath, "template.json").getAbsolutePath();
      JSONArray templateFields = JSONUtils.applyInheritance(
          JSONUtils.parseFileToJSONObject(jsonPath)).getJSONArray("fields");

      int templateFieldsLength = templateFields.length();
      for (int i = 0; i < templateFieldsLength; i++) {
        initFields.put(templateFields.get(i));
      }
      Log.i(LOG_TAG, "Concated " + templatePath);
    }

    JSONArray fields = new JSONArray();
    int initFieldsLength = initFields.length();

    // Make fields array with no null values
    for (int i = 0; i < initFieldsLength; i++) {
      JSONObject field = initFields.optJSONObject(i);
      if (field != null) {
        fields.put(initFields.getJSONObject(i));
      } else {
        Log.i(LOG_TAG, "Null");
      }
    }

    int fieldsLength = fields.length();

    // Get the field names and labels:
    String[] fieldNames = new String[fieldsLength];
    String[] fieldLabels = new String[fieldsLength];
    for (int i = 0; i < fieldsLength; i++) {
      JSONObject field = fields.getJSONObject(i);
      if (field.has("name")) {
        fieldNames[i] = validate(field.getString("name"));
        fieldLabels[i] = field.optString("label", fieldNames[i]);
      } else {
        Log.i(LOG_TAG, "Field " + i + " has no name.");
        // throw new JSONException("Field " + i + " has no name or label.");
      }
    }

    Log.i(LOG_TAG, "Writing output json string to store in Survey...");

    // Three worksheets need to be created for Survey - survey, choices, and
    // settings.
    // scanOutputDir is added to the model worksheet because we need that to
    // map a Scan photo to a Survey instance, but we don't want to create a
    // prompt in Survey.
    // Choices can be blank if there are no select type questions on the form.
    JSONObject surveyJson = new JSONObject();

    // Fill out the settings Sheet first
    JSONArray settingsList = new JSONArray();

    // settings form_id
    JSONObject formIdObj = new JSONObject();
    formIdObj.put("setting_name", "form_id");
    formIdObj.put("value", id);
    settingsList.put(formIdObj);

    // settings form_version
    // set this to today's date in the format yyyyMMdd
    String format = "yyyyMMdd";
    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
    String formVerStr = sdf.format(new Date());
    JSONObject formVerObj = new JSONObject();
    formVerObj.put("setting_name", "form_version");
    formVerObj.put("value", formVerStr);
    settingsList.put(formVerObj);

    // settings survey
    JSONObject formSurveyObj = new JSONObject();
    formSurveyObj.put("setting_name", "survey");
    formSurveyObj.put("display.title", id);
    settingsList.put(formSurveyObj);

    // Create other worksheets
    JSONArray surveyList = new JSONArray();
    JSONArray choicesList = new JSONArray();
    JSONArray modelList = new JSONArray();

    // Add _row_num to the survey worksheet
    // to enable table of contents
    int rowNum = 1;
    String rowName = "__rowNum__";

    for (int i = 0; i < fieldsLength; i++) {
      JSONObject field = fields.getJSONObject(i);

      JSONArray segments = field.optJSONArray("segments");
      if (segments != null) {
        if (segments.length() > 0) {
          JSONObject begScreen = new JSONObject();
          begScreen.put("clause", "begin screen");
          begScreen.put(rowName, rowNum++);
          surveyList.put(begScreen);
        }
        for (int j = 0; j < segments.length(); j++) {
          JSONObject imagePrompt = new JSONObject();
          String imageName = field.getString("name") + "_image" + j;
          imagePrompt.put("name", imageName);
          imagePrompt.put("type", "read_only_image");
          imagePrompt.put(rowName, rowNum++);
          surveyList.put(imagePrompt);
        }
      }

      JSONObject prompt = new JSONObject();
      String type = field.getString("type");
      if (type.equals("note")) {
        // Not sure what to do with a note here
      } else if (type.equals("int")) {
        // If there is a default value, use assign prompt
        // to set the default value in Survey
        if (field.has("default")) {
          JSONObject def = new JSONObject();
          def.put("calculation", field.getString("default"));
          def.put("name", field.getString("name"));
          def.put("type", "assign");
        }
        String label = field.optString("label");
        if (!label.equals("")) {
          prompt.put("display.text", label);
        }
        prompt.put("name", field.getString("name"));
        prompt.put("type", "integer");
        prompt.put(rowName, rowNum++);
        surveyList.put(prompt);
      } else if (type.equals("string") || type.equals("input")) {
        String label = field.optString("label");
        if (!label.equals("")) {
          prompt.put("display.text", label);
        }
        prompt.put("name", field.getString("name"));
        prompt.put("type", "text");
        prompt.put(rowName, rowNum++);
        surveyList.put(prompt);
      } else if (type.equals("select") || type.equals("select1") || type.equals("select_many")) {
        String label = field.optString("label");
        if (!label.equals("")) {
          prompt.put("display.text", label);
        }
        prompt.put("name", field.getString("name"));

        // Set the correct type
        if (type.equals("select_many")) {
          prompt.put("type", "select_multiple");
        } else {
          prompt.put("type", "select_one");
        }

        String param = field.optString("param");
        if (!param.equals("")) {
          prompt.put("values_list", param);
        } else {
          param = field.getString("name") + "_choices";
          prompt.put("values_list", param);

        }
        prompt.put(rowName, rowNum++);
        surveyList.put(prompt);

        // Get the items from the first segment.
        boolean addChoiceList = true;
        if (choicesList.length() > 0) {
          for (int k = 0; k < choicesList.length(); k++) {
            JSONObject ch = choicesList.getJSONObject(k);
            String chList = ch.getString("choice_list_name");
            if (chList.equals(param)) {
              // If we already have this choice list,
              // don't add it again!!
              addChoiceList = false;
            }
          }
        }
        if (segments != null && addChoiceList) {
          JSONObject segment = segments.optJSONObject(0);
          if (segment != null) {
            JSONArray items = segment.getJSONArray("items");
            for (int j = 0; j < items.length(); j++) {
              // This is the old way of doing this with param
              JSONObject item = items.getJSONObject(j);
              JSONObject choice = new JSONObject();
              choice.put("choice_list_name", param);
              // Not sure of the best way to deal with this currently!!
              if (item.has("value")) {
                choice.put("data_value", item.getString("value"));

                String itemLabel = item.optString("label");
                itemLabel = itemLabel == "" ? item.getString("value") : itemLabel;
                choice.put("display.text", itemLabel);
              } else {
                // If the item does not have a param - do it the new way
                // This is done with grid values
                if (field.getJSONArray("grid_values") != null) {
                  JSONArray gridValues = field.getJSONArray("grid_values");
                  choice.put("data_value", "" + gridValues.getString(j));
                  choice.put("display.text", "" + gridValues.getString(j));
                }
              }
              choicesList.put(choice);
            }
          }
        }

      } else if (type.equals("qrcode")) {
        // Interpreting this as text since I don't
        // think we want users to rescan things here?
        String label = field.optString("label");
        String param = field.optString("param");
        if (!label.equals("")) {
          prompt.put("display.text", label);
        } else if (!param.equals("")) {
          prompt.put("display.text", param);
        }
        prompt.put("name", field.getString("name"));
        prompt.put("type", "text");
        prompt.put(rowName, rowNum++);
        surveyList.put(prompt);
        // New types added
      } else if (type.equals("box")) {
        String label = field.optString("label");
        String param = field.optString("param");
        if (!label.equals("")) {
          prompt.put("display.text", label);
        } else if (!param.equals("")) {
          prompt.put("display.text", param);
        }
        prompt.put("name", field.getString("name"));
        prompt.put("type", "text");
        prompt.put(rowName, rowNum++);
        surveyList.put(prompt);
      } else if (type.equals("tally")) {
        String label = field.optString("label");
        if (!label.equals("")) {
          prompt.put("display.text", label);
        }
        prompt.put("name", field.getString("name"));
        prompt.put("type", "integer");
        prompt.put(rowName, rowNum++);
        surveyList.put(prompt);
      }
      if (segments != null) {
        if (segments.length() > 0) {
          JSONObject endScreen = new JSONObject();
          endScreen.put("clause", "end screen");
          endScreen.put(rowName, rowNum++);
          surveyList.put(endScreen);
        }
      }

    }

    // Add the Scan metadata into the model worksheet
    JSONObject scanMetadata = new JSONObject();
    scanMetadata.put("name", scanOutputDir);
    scanMetadata.put("type", "string");
    modelList.put(scanMetadata);

    // Add the choices, settings, and survey object to the survey json
    if (choicesList.length() > 0) {
      surveyJson.put("choices", choicesList);
    }
    surveyJson.put("model", modelList);
    surveyJson.put("settings", settingsList);
    surveyJson.put("survey", surveyList);

    jsonOutputString = surveyJson.toString();

    // Write JSON that is sent to xlsxconverter to a file for inspection
    writeOutToFile(ScanUtils.getAppFormDirPath("example"), "intermediate.json", jsonOutputString);

    return jsonOutputString;
  }

  /**
   * Check if any of the provided file paths were modified after the given date.
   * 
   * @param templatePaths
   * @param lastModified
   * @return
   */
  // This function may be useful when checking for version issues?
  private boolean anyModifiedAfter(ArrayList<String> templatePaths, long lastModified) {
    for (String tp : templatePaths) {
      if (new File(tp).lastModified() > lastModified) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if any of the provided file paths were modified after the given date.
   * 
   * @param templatePaths
   * @param lastModified
   * @return
   */
  // This function may be useful when checking for version issues?
  private void setIntentToReturn(String formId) {
    
    Intent intent = new Intent();
    setResult(RESULT_OK, intent);
    
    Bundle args = new Bundle();
    args.putString(APP_NAME, ScanUtils.getODKAppName());
    args.putString(TABLE_ID, formId);
    args.putString(FILE_NAME, ScanUtils.getTablesUriForInstanceWithScanOutputDir(formId, ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1))));
    args.putString(TABLE_DISPLAY_TYPE, TABLES_DISPLAY_LIST);
    intent.putExtras(args);
  }

  public void databaseAvailable() {
    if ( Scan.getInstance().getDatabase() != null ) {

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
      if (checkForSubforms(templatePath)) {
        createSurveyInstanceBasedOnFormDesignerForms(templatePath);
        return;
      } else {
        String directoryForFormDef = ScanUtils.getAppFormDirPath(formId);
        File formDefFile = new File(directoryForFormDef, "formDef.json");

        // If the form does exist already, there could be a versioning issue
        if (!formDefFile.exists()) {
          try {
            File formDefToWrite = findFileThatEndsIn(templatePath, "formDef.json");
            JSONObject formDefObjToWrite = getJSONFromFile(formDefToWrite);
            String val = formDefObjToWrite.toString();
            writeOutToFile(ScanUtils.getAppFormDirPath(formId), "formDef.json", val);
          } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Could not write out formDef.json to proper tables directory " + formId);
          }
        }

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
