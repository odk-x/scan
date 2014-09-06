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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.common.android.data.ElementDataType;
import org.opendatakit.common.android.database.DataModelDatabaseHelper;
import org.opendatakit.common.android.database.DataModelDatabaseHelperFactory;
import org.opendatakit.common.android.utilities.DataTypeNamesToRemove;
import org.opendatakit.common.android.utilities.ODKDatabaseUtils;
import org.opendatakit.common.android.utilities.ODKFileUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSON2SurveyJSON extends Activity{
	
	private static final String LOG_TAG = "ODKScan";
	
	private static final String scanOutputDir = "scan_output_directory";
	
	private static String formId;
	
	private static final String customCssFileNameStr = "customStyles.css";
	

	
	private ArrayList<String> photoNames;

	WebView myWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();

		if(extras == null){ try {
			throw new Exception("No parameters specified");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} }

		String templatePath = extras.getString("templatePath");
		if(templatePath == null){ try {
			throw new Exception("Could not identify template.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} }
		
		ArrayList<String> templatePaths = extras.getStringArrayList("prevTemplatePaths");
		if(templatePaths == null){
			templatePaths = new ArrayList<String>(Arrays.asList(templatePath));
		} else {
			templatePaths.add(templatePath);
		}
		
		//String rootTemplatePath = templatePaths.get(0);
		
		// Set the formId to use for this newly created form
    	formId = new File(templatePaths.get(0)).getName();
    	formId = "scan_" + formId;
    	
		String photoName = extras.getString("photoName");
		if(photoName == null){ try {
			throw new Exception("jsonOutPath is null");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} }
		
		photoNames = extras.getStringArrayList("prevPhotoNames");
		if(photoNames == null){
			photoNames = new ArrayList<String>(Arrays.asList(photoName));
		} else {
			photoNames.add(photoName);
		}

		//String rootPhotoName = photoNames.get(0);
		Log.i(LOG_TAG,"photoNames : " + photoNames);
		

		//String templateName = new File(rootTemplatePath).getName();
		//String directoryForFormDef = extStoreStr + surveyDirStr + tablesDirStr + formId + fileSeparatorStr + formsDirStr + formId;
		String directoryForFormDef = ScanUtils.getAppFormDirPath(formId);
		File formDefFile = new File(directoryForFormDef, "formDef.json");

		// If the form does exist already, there could be a versioning issue
		// This will need to be resolved in a better way
		if (!formDefFile.exists()) {
			String theString = null;
			try {
				theString = buildSurveyFormDef(templatePaths);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			myWebView = new WebView(this);
			myWebView.getSettings().setJavaScriptEnabled(true);
			myWebView.setVisibility(View.GONE);
			myWebView.addJavascriptInterface(new JavaScriptHandler(this), "MyHandler");
			myWebView.loadUrl(ScanUtils.getXlsxConverterUri());
			
			final String jsString = "javascript:convert('" + theString + "');";
			
			myWebView.setWebViewClient(new WebViewClient() {
		        public void onPageFinished(WebView view, String url)
		        {
		        	myWebView.loadUrl(jsString);
		        }
		    });
		} else {
			// Check if there is a registered Survey instance or create one
			createSurveyInstance();
		}
	}
	
	public void createSurveyTables(SQLiteDatabase db, String tableName, JSONArray fieldsToProcess)
	{
	  List<Column> columns = new ArrayList<Column>();
		
		// Always add the scan output directory to the table definition
		// This is used to map a Survey instance with a Scan photo
		columns.add(new Column(scanOutputDir, scanOutputDir, ElementDataType.string.name(), "[]"));
			
		try {
			int fieldsLength = fieldsToProcess.length();
			for(int i = 0; i < fieldsLength; i++){
				JSONObject field = fieldsToProcess.optJSONObject(i);
	        	// Not sure how best to deal with notes?
				String fieldName = validate(field.getString("name"));
		
				JSONArray segments = field.optJSONArray("segments");
				if(segments == null){
					segments = new JSONArray();
				}
				// Add segment images to columns
				for(int j = 0; j < segments.length(); j++){
					JSONObject segment = segments.getJSONObject(j);
				
					// Should I check the image_path for the
					// segment before including it - I don't think so
					if (segment != null) {
						String imageName = fieldName + "_image_" + j;
						columns.add(new Column(imageName, imageName, DataTypeNamesToRemove.MIMEURI, 
						    "[\"" + imageName + "_uriFragment\",\"" + imageName + "_contentType\"]"));
						columns.add(new Column(imageName + "_uriFragment", "uriFragment", ElementDataType.rowpath.name(), "[]"));
						columns.add(new Column(imageName + "_contentType", "contentType", ElementDataType.string.name(), "[]"));
					}
				}
				
				// Add column for field 
				String type = field.getString("type").toUpperCase(Locale.US);
				if (type.equals("INT")){
				  columns.add(new Column(fieldName, fieldName, ElementDataType.integer.name(), "[]"));
				} else if (type.equals("FLOAT")){
              columns.add(new Column(fieldName, fieldName, ElementDataType.number.name(), "[]"));
				} else {
              columns.add(new Column(fieldName, fieldName, ElementDataType.string.name(), "[]"));
				}
			}
			
			// Create the database table with the columns
			ODKDatabaseUtils.createOrOpenDBTableWithColumns(db, tableName, columns);
		    
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Error - Could NOT create table " + tableName + " with columns");
		}
	}
	
	public void createSurveyInstance()
	{	
		ContentValues tablesValues = new ContentValues();
		
		// Is there a better place to get the Survey application name?
		DataModelDatabaseHelper dbh = DataModelDatabaseHelperFactory.getDbHelper(this, ScanUtils.getAppNameForSurvey());
		SQLiteDatabase db = dbh.getWritableDatabase();
		
		String tableName = formId;
			
		String rowId = null;
		StringBuilder dbValuesToWrite = new StringBuilder();
		String uuidStr = UUID.randomUUID().toString();
		
		try {
			
			if (tableName == null) {
				throw new Exception("formId cannot be blank!!");
			}
			
			// Get the fields from the output.json file that need to be created in the 
			// database table
			JSONArray fields = new JSONArray();
		    for(String photoName : photoNames){
				JSONArray photoFields = JSONUtils.parseFileToJSONObject(ScanUtils.getJsonPath(photoName)).getJSONArray("fields");
				int photoFieldsLength = photoFields.length();
				for(int i = 0; i < photoFieldsLength; i++){
					fields.put(photoFields.get(i));
				}
				Log.i(LOG_TAG, "Concated " + photoName);
			}
		    
			int fieldsLength = fields.length();
			if(fieldsLength == 0){
				throw new JSONException("There are no fields in the json output file.");
			}
		    
		    Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
		    if (!cursor.moveToFirst()) {
		    	Log.i(LOG_TAG, "No table definition found for " + tableName + ". Creating new table definition");
		    	createSurveyTables(db, tableName, fields);
		    	
		    } 
		    
		    String selection = scanOutputDir + "=?";
		    String[] selectionArgs = {ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1))};
		    cursor = db.query(tableName, null, selection, selectionArgs, null, null, null);
		    
		    // Check if the instance already exists in survey
		    if (cursor.moveToFirst()) {
		    	int ind = cursor.getColumnIndex("_id");
		    	String foudnUuidStr = cursor.getString(ind);
				//String uriStr = surveyUriStr+formId+fileSeparatorStr+instanceIdStr + foudnUuidStr;
				String uriStr = ScanUtils.getSurveyUri(formId) + foudnUuidStr;
				Intent resultData = new Intent();
				resultData.setData(Uri.parse(uriStr));
				setResult(RESULT_OK, resultData);
		    	cursor.close();
				finish();
		    	return;
		    } 

			Log.i(LOG_TAG, "Transfering the values from the JSON output into the survey instance");
	
			// Have to address multiple page scans
			// Not sure what this means for survey
			File dirToMake = null;
			String dirId = null;
			StringBuilder cssStr = new StringBuilder();
			//String cssDir = extStoreStr + surveyDirStr + tablesDirStr  + formId + fileSeparatorStr+ formsDirStr + formId; 
			String cssDir = ScanUtils.getAppFormDirPath(formId);
			
			// Get the screen size in case we need to 
			// write out a css file
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int screenWidth = (int) (displaymetrics.widthPixels / displaymetrics.scaledDensity);
			int screenHeight = (int) (displaymetrics.heightPixels / displaymetrics.scaledDensity);
			
			boolean writeOutCustomCss = false;
			if (fieldsLength > 0) {
				// manufacture a rowId for this record...
				// for directory name to store the image files
			    rowId = "uuid:" + uuidStr;
			    
			    dirToMake = new File(ODKFileUtils.getInstanceFolder(ScanUtils.getAppNameForSurvey(), formId, rowId)); 
			    dirId = dirToMake.getAbsolutePath().substring(dirToMake.getAbsolutePath().lastIndexOf("/")+1);
				
				File customCssFile = new File(cssDir + customCssFileNameStr);
				if (!customCssFile.exists()) {
					writeOutCustomCss = true;
				}
			}
			for(int i = 0; i < fieldsLength; i++){
				JSONObject field = fields.optJSONObject(i);
	        	/*May need to use this to have a note that scan uses to check if
	        	 * the instance is already created?
	        	 * if(field.getString("type").equals("note")){
	        		Element fieldElement = instance.createElement("", "autogenerated_note_" + i);
	        		instance.addChild(Node.ELEMENT, fieldElement);
	        		continue;
	        	}*/
				String fieldName = validate(field.getString("name"));
	        	JSONArray segments = field.optJSONArray("segments");
	        	if(segments == null){
	        		segments = new JSONArray();
	        	}
				//Add segment images - Copy these files to the right location
	        	//and update their database value
				for(int j = 0; j < segments.length(); j++){
					JSONObject segment = segments.getJSONObject(j);
					
					String imageName = fieldName + "_image_" + j;
					String imagePath = segment.getString("image_path");
	
					if(!segment.has("image_path") || segment.isNull("image_path")){
						// I won't add any db value to write
						Log.i(LOG_TAG, "No image_path found " + imageName);
						continue;
					}
					
					String imageFileName = new File(imagePath).getName();
					int dotPos = imageFileName.lastIndexOf(".");
					String imageFileSubstr = imageFileName.substring(0, dotPos);
					String imageFileExt = imageFileName.substring(dotPos);
	
					//---Copy segment image to the correct survey directory------
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
					//---End of copying the image
					
					// Update the row value for the picture
					// Use ObjectMapper to avoid "\/" in uriFragment paths
					// Although, this doesn't cause an issue - leaving
					// this code commented out for now
					/*
					JSONObject picJson = new JSONObject();
	
					picJson.put("uriFragment", tablesDirStr+formId+fileSeparatorStr+instancesDirStr+dirId+fileSeparatorStr+outputPicFile.getName());
					picJson.put("uriFragment", "image/jpg");
					tablesValues.put(imageName, picJson.toString());
					dbValuesToWrite.append(imageName).append("=").append(picJson.toString());
					*/
					ObjectMapper objectMapper = new ObjectMapper();
					Map<String, String> mapObject = new HashMap<String, String>();
					//mapObject.put("uriFragment", tablesDirStr+formId+fileSeparatorStr+instancesDirStr+dirId+fileSeparatorStr+outputPicFile.getName());
					//getAppRelativeInstancesDirPath
					mapObject.put("uriFragment", ScanUtils.getAppRelativeInstancesDirPath(formId, dirId) + outputPicFile.getName());
					mapObject.put("contentType", "image/jpg");
					String picJsonStr = objectMapper.writeValueAsString(mapObject);
					addStringValueToTableContentValue(tablesValues, imageName, picJsonStr);
					
					// Add styling for this image in the css file if no css file is found
					if (writeOutCustomCss) {
						int segHeight = segment.getInt("segment_height");
						int segWidth = segment.getInt("segment_width"); 
						cssStr.append("#").append(imageName).append("{\n");
						boolean segWidthGreater = segWidth > screenWidth ? true : false;
						boolean segHeightGreater = segHeight > screenHeight ? true : false;
						
						if (segWidthGreater|| segHeightGreater) {
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
				if(field.has("value")){
					if (field.getString("type").equals("int") || field.getString("type").equals("tally")) {
						tablesValues.put(field.getString("name"), field.getInt("value"));
						dbValuesToWrite.append(field.getString("name")).append("=").append(field.getInt("value"));
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
							addStringValueToTableContentValue(tablesValues, field.getString("name"), surveyValue);
							dbValuesToWrite.append(field.getString("name")).append("=").append(field.getString("value"));
						}
					}else {
						// Check if the string is empty - if it is don't write anything out
						String value = field.getString("value");
						if (value.length() > 0) {
	 						addStringValueToTableContentValue(tablesValues, field.getString("name"), field.getString("value"));
							dbValuesToWrite.append(field.getString("name")).append("=").append(field.getString("value"));
						}
					}
				} else if(field.has("default")){
					if (field.getString("type").equals("int")) {
						tablesValues.put(field.getString("name"), field.getInt("default"));
						dbValuesToWrite.append(field.getString("name")).append("=").append(field.getInt("default"));
					} else {
						addStringValueToTableContentValue(tablesValues, field.getString("name"), field.getString("default"));
						dbValuesToWrite.append(field.getString("name")).append("=").append(field.getString("default"));
					}
				}
			}
			
			if (tablesValues.size() > 0) {
				// Add scan metadata here for the photo taken
				tablesValues.put(scanOutputDir, ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1)));
				Log.i(LOG_TAG,"Writing db values for row:" + rowId + " values:" + dbValuesToWrite.toString());
				ODKDatabaseUtils.writeDataIntoExistingDBTableWithId(db, tableName, tablesValues, rowId);
				
				if (writeOutCustomCss) {
					writeOutToFile(cssDir, customCssFileNameStr, cssStr.toString());
				}
			}
		} catch (Exception e) {
		      e.printStackTrace();
		      Log.e(LOG_TAG, "Error - Could NOT write data into table " + tableName);
		}
		    
	   	db.close();
	    
		String uriStr = ScanUtils.getSurveyUri(formId) + rowId;
		Intent resultData = new Intent();
		resultData.setData(Uri.parse(uriStr));
		setResult(RESULT_OK, resultData);
		finish();
	}
	
	public void addStringValueToTableContentValue(ContentValues tableValue, String key, String value)
	{
		if (value != "") {
			tableValue.put(key, value);
		}
	}
	
	public void javascriptCallFinished(String val) {
		Log.i(LOG_TAG, "The formDef.json from xlsxconverter is" + val);
		//writeOutToFile(extStoreStr + surveyDirStr + tablesDirStr  + formId + fileSeparatorStr + formsDirStr + formId, "formDef.json", val);
		writeOutToFile(ScanUtils.getAppFormDirPath(formId), "formDef.json", val);
		createSurveyInstance();
	}
	
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
     * Builds an XFrom from a JSON template and writes it out to the specified file.
     * It builds it as a string which really isn't the best way to go.
     * @param templatePaths
     * @param outputPath
     * @throws Exception 
     */
    public static String buildSurveyFormDef(ArrayList<String> templatePaths) throws Exception {
    	
    	String jsonOutputString = "";
    	String title = new File(templatePaths.get(0)).getName();
    	String id = "scan_" + title;
    	
    	JSONArray initFields = new JSONArray();
		for(String templatePath : templatePaths){
			String jsonPath = new File(templatePath, "template.json").getAbsolutePath();
			// CAL: Take this outtoString();
			// TODO: Using inheritance rules here seems unnecessary
			JSONArray templateFields = JSONUtils.applyInheritance( JSONUtils.parseFileToJSONObject(jsonPath) ).getJSONArray("fields");
			
			int templateFieldsLength = templateFields.length();
			for(int i = 0; i < templateFieldsLength; i++){
				initFields.put(templateFields.get(i));
			}
			Log.i(LOG_TAG, "Concated " + templatePath);
		}
		
		JSONArray fields = new JSONArray();
		int initFieldsLength = initFields.length();
		
		// Make fields array with no null values
		for(int i = 0; i < initFieldsLength; i++){
			JSONObject field = initFields.optJSONObject(i);
			if(field != null){
				fields.put(initFields.getJSONObject(i));
			}
			else{
				Log.i(LOG_TAG, "Null");
			}
		}
		
		int fieldsLength = fields.length();
		
		// Get the field names and labels:
		String [] fieldNames = new String[fieldsLength];
		String [] fieldLabels = new String[fieldsLength];
		for(int i = 0; i < fieldsLength; i++){
			JSONObject field = fields.getJSONObject(i);
			if(field.has("name")){
				fieldNames[i] = validate(field.getString("name"));
				fieldLabels[i] = field.optString("label", fieldNames[i]);
			}
			else{
				Log.i(LOG_TAG, "Field " + i + " has no name.");
				//throw new JSONException("Field " + i + " has no name or label.");
			}
		}

		Log.i(LOG_TAG, "Writing output json string to store in Survey...");

		// Three worksheets need to be created for Survey - survey, choices, and settings.  
		// scanOutputDir is added to the model worksheet because we need that to 
		// map a Scan photo to a Survey instance, but we don't want to create a prompt in Survey.
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
		
        for(int i = 0; i < fieldsLength; i++){
        	JSONObject field = fields.getJSONObject(i);
        	
        	JSONArray segments = field.optJSONArray("segments");
        	if(segments != null){
        		if (segments.length() > 0) {
        			JSONObject begScreen = new JSONObject();
        			begScreen.put("clause", "begin screen");
        			begScreen.put(rowName, rowNum++);
        			surveyList.put(begScreen);
        		}
	        	for(int j = 0; j < segments.length(); j++){
	        		JSONObject imagePrompt = new JSONObject();
	        		String imageName = field.getString("name") + "_image_" + j;
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
        		if(field.has("default")){
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
	                    for(int j = 0; j < items.length(); j++){
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
		                    		choice.put("data_value", ""+gridValues.getString(j));
		                    		choice.put("display.text", ""+gridValues.getString(j));
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
        	if(segments != null){
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
        scanMetadata.put("name",scanOutputDir);
        scanMetadata.put("type","string");
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
     * Check that the string is a valid xml tag
     * @throws Exception 
     */
	private static String validate(String string) throws Exception {
		if(Pattern.matches("[a-zA-Z][a-zA-Z_0-9]*", string)){
			return string;
		} else {
			throw new Exception("Field name cannot be used in xform: " + string);
		}
	}
	
	
	/**
	 * Check if any of the provided file paths
	 * were modified after the given date.
	 * @param templatePaths
	 * @param lastModified
	 * @return
	 */
	/* This function may be useful when checking for version issues?
	private boolean anyModifiedAfter(ArrayList<String> templatePaths,
			long lastModified) {
		for (String tp : templatePaths){
			if(new File(tp).lastModified() > lastModified){
				return true;
			}
		}
		return false;
	}
	*/
}
