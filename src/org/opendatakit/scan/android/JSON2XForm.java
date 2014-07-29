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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
/**
 * This activity converts ODKScan JSON files into XForms and XForm instances for use with Collect.
 * It returns a uri for the XForm instance as a result.
 */
public class JSON2XForm extends Activity {
	
	private static final String LOG_TAG = "ODKScan";

	private static final String COLLECT_FORMS_URI_STRING =
			"content://org.odk.collect.android.provider.odk.forms/forms";
	private static final Uri COLLECT_FORMS_CONTENT_URI =
			Uri.parse(COLLECT_FORMS_URI_STRING);
	private static final String COLLECT_INSTANCES_URI_STRING =
			"content://org.odk.collect.android.provider.odk.instances/instances";
	private static final Uri COLLECT_INSTANCES_CONTENT_URI =
			Uri.parse(COLLECT_INSTANCES_URI_STRING);
	private static final DateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Bundle extras = getIntent().getExtras();

			if(extras == null){ throw new Exception("No parameters specified"); }

			String templatePath = extras.getString("templatePath");
			if(templatePath == null){ throw new Exception("Could not identify template."); }
			ArrayList<String> templatePaths = extras.getStringArrayList("prevTemplatePaths");
			if(templatePaths == null){
				templatePaths = new ArrayList<String>(Arrays.asList(templatePath));
			} else {
				templatePaths.add(templatePath);
			}
			String rootTemplatePath = templatePaths.get(0);
			//Log.i(LOG_TAG,"templatePaths : " + templatePaths);
			
			String photoName = extras.getString("photoName");
			if(photoName == null){ throw new Exception("jsonOutPath is null"); }
			ArrayList<String> photoNames = extras.getStringArrayList("prevPhotoNames");
			if(photoNames == null){
				photoNames = new ArrayList<String>(Arrays.asList(photoName));
			} else {
				photoNames.add(photoName);
			}
			String rootPhotoName = photoNames.get(0);
			Log.i(LOG_TAG,"photoNames : " + photoNames);
			
			String templateName = new File(rootTemplatePath).getName();
			String xFormPath = new File(rootTemplatePath, templateName + ".xml").getPath();

			ContentResolver myContentResolver = getContentResolver();
			if(myContentResolver == null) {
				throw new Exception("Could not get content resolver. Try again.");
			}
			
			//////////////
			Log.i(LOG_TAG, "Checking if there is no xform or the xform is out of date.");
			//////////////
			File xformFile = new File(xFormPath);
			if( !xformFile.exists() || anyModifiedAfter(templatePaths, xformFile.lastModified())){
				//////////////
				Log.i(LOG_TAG, "Unregistering any existing old versions of xform.");
				//////////////
				String [] deleteArgs = { templateName };
				int deleteResult = myContentResolver.delete(COLLECT_FORMS_CONTENT_URI, "jrFormId like ?", deleteArgs);
				Log.w(LOG_TAG, "Removing " + deleteResult + " rows.");
				//TODO: Not sure what happens to the old instances...
				//////////////
				Log.i(LOG_TAG, "Creating new xform.");
				//////////////
				buildXForm(templatePaths, xFormPath);
			}
			String jrFormId = verifyFormInCollect(xFormPath, templateName);
			//////////////
			Log.i(LOG_TAG, "Checking if the form instance is already registered with collect.");
			//Previous instance are found using template and photo name,
			//so Scan doesn't have to keep track of the instanceID.
			//////////////
			int instanceId;
			String instanceName = templateName + '_' + rootPhotoName;
			File instancesDir = new File(new File(Environment.getExternalStorageDirectory(), "odk"), "instances");
			File instanceDir = new File(instancesDir, instanceName);
			instanceDir.mkdirs();
			String instanceFilePath = new File(instanceDir.getAbsolutePath(), instanceName + ".xml").getAbsolutePath();
			String selection = "instanceFilePath = ?";
			String[] selectionArgs = { instanceFilePath };
			Cursor c = myContentResolver.query(COLLECT_INSTANCES_CONTENT_URI, null, selection, selectionArgs, null);
			if(c.moveToFirst()){
				//////////////
				Log.i(LOG_TAG, "Registered odk instance found.");
				//////////////
				instanceId = c.getInt(c.getColumnIndex("_id"));
			}
			else{
				//////////////
				Log.i(LOG_TAG, "Registered odk instance not found, creating one...");
				//////////////
				jsonOut2XFormInstance(photoNames, xFormPath, instanceDir, instanceName);
				ContentValues insertValues = new ContentValues();
				insertValues.put("displayName", instanceName);
				insertValues.put("instanceFilePath", instanceFilePath);
				insertValues.put("jrFormId", jrFormId);
				ContentResolver contentResolver = getContentResolver();
				if(contentResolver == null) {
					throw new Exception("Could not get content resolver. Please try again.");
				}
				Uri insertResult = contentResolver.insert(
						COLLECT_INSTANCES_CONTENT_URI, insertValues);
				instanceId = Integer.valueOf(insertResult.getLastPathSegment());
			}
			c.close();
			Log.i(LOG_TAG, "instanceId: " + instanceId);

			Intent resultData = new Intent();
			resultData.putExtras(extras);
			resultData.setData(Uri.parse(COLLECT_INSTANCES_URI_STRING + "/" + instanceId));
			setResult(RESULT_OK, resultData);
			finish();
		} catch (Exception e) {
			//Display an error dialog if something goes wrong.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(e.toString())
			.setCancelable(false)
			.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					setResult(RESULT_CANCELED);
					finish();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
	/**
	 * Check if any of the provided file paths
	 * were modified after the given date.
	 * @param templatePaths
	 * @param lastModified
	 * @return
	 */
	private boolean anyModifiedAfter(ArrayList<String> templatePaths,
			long lastModified) {
		for (String tp : templatePaths){
			if(new File(tp).lastModified() > lastModified){
				return true;
			}
		}
		return false;
	}
	/**
     * Verify that the form is in collect and put it in collect if it is not.
     * @param filepath
     * @return jrFormId
     */
	private String verifyFormInCollect(String filepath, String jrFormId) {
        String[] projection = { "jrFormId" };
        String selection = "formFilePath = ?";
        String[] selectionArgs = { filepath };
        Cursor c = managedQuery(COLLECT_FORMS_CONTENT_URI, projection,
                selection, selectionArgs, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
            String value = c.getString(c.getColumnIndex("jrFormId"));
            c.close();
            return value;
        }
		//////////////
		Log.i(LOG_TAG, "Registering the new xform with collect.");
		//////////////
        ContentValues insertValues = new ContentValues();
        insertValues.put("displayName", filepath);
        insertValues.put("jrFormId", jrFormId);
        insertValues.put("formFilePath", filepath);
        getContentResolver().insert(COLLECT_FORMS_CONTENT_URI, insertValues);
        return jrFormId;
    }
	/**
	 * Generates an instance of an xform at xFormPath from the JSON output file
	 * @throws Exception 
	 */
	private void jsonOut2XFormInstance(ArrayList<String> photoNames, String xFormPath, File instanceDir, String instanceName)
			throws Exception {
		//////////////
	    Log.i(LOG_TAG, "Reading the xform...");
	    //////////////
	    Document formDoc = new Document();
	    KXmlParser formParser = new KXmlParser();
        formParser.setInput(new FileReader(xFormPath));
        formDoc.parse(formParser);
	    //////////////
	    Log.i(LOG_TAG, "Getting the relevant elements...");
	    //////////////
	    String namespace = formDoc.getRootElement().getNamespace();
        Element hhtmlEl = formDoc.getElement(namespace, "h:html");
        Element hheadEl = hhtmlEl.getElement(namespace, "h:head");
        Element modelEl = hheadEl.getElement(namespace, "model");
        Element instanceEl = modelEl.getElement(namespace, "instance");
        Element dataEl = instanceEl.getElement(0);
		String jrFormId = dataEl.getAttributeValue(namespace, "id");
        Element instance = new Element();
        instance.setName(dataEl.getName());
        instance.setAttribute("", "id", jrFormId);
        //instance.addChild(Node.ELEMENT, instance.createElement("", "xformstarttime"));
        instance.addChild(Node.ELEMENT, instance.createElement("", "xformendtime"));
        //////////////
        Log.i(LOG_TAG, "Parsing the JSON output:");
        //////////////
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
		//////////////
		Log.i(LOG_TAG, "Transfering the values from the JSON output into the xform instance:");
		//////////////
		{
			Element fieldElement = instance.createElement("", "instance_creation_time");
			String dateTimeString = ISO8601.format(new Date());
			Log.i(LOG_TAG, dateTimeString.substring(0, dateTimeString.length() - 2));
			//Remove the last two zeros in the time offset:
			fieldElement.addChild(Node.TEXT, dateTimeString.substring(0, dateTimeString.length() - 2));
			instance.addChild(Node.ELEMENT, fieldElement);
		}
		{
			Element fieldElement = instance.createElement("", "scan_output_directory");
			//For multi-page forms the scan output directory is the output directory of the last page.
			//We can figure out the previous output directories by looking at the (pageN) part.
			fieldElement.addChild(Node.TEXT, ScanUtils.getOutputPath(photoNames.get(photoNames.size() - 1)));
			instance.addChild(Node.ELEMENT, fieldElement);
		}
		for(int i = 0; i < fieldsLength; i++){
			JSONObject field = fields.optJSONObject(i);
        	if(field.getString("type").equals("note")){
        		Element fieldElement = instance.createElement("", "autogenerated_note_" + i);
        		instance.addChild(Node.ELEMENT, fieldElement);
        		continue;
        	}
			String fieldName = validate(field.getString("name"));
        	JSONArray segments = field.optJSONArray("segments");
        	if(segments == null){
        		segments = new JSONArray();
        	}
			//Add segment images
			for(int j = 0; j < segments.length(); j++){
				JSONObject segment = segments.getJSONObject(j);
				String imageName = fieldName + "_image_" + j;
				Element fieldImageElement = instance.createElement("", imageName);
				if(!segment.has("image_path") || segment.isNull("image_path")){
					fieldImageElement.addChild(Node.TEXT, "");
					instance.addChild(Node.ELEMENT, fieldImageElement);
					continue;
				}
				String imagePath = segment.getString("image_path");
				fieldImageElement.addChild(Node.TEXT, new File(imagePath).getName());
				//Copy segment image
				InputStream fis = new FileInputStream(imagePath);
				FileOutputStream fos = new FileOutputStream(
						new File(instanceDir.getAbsolutePath(),
						new File(imagePath).getName())
						.getAbsolutePath());
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = fis.read(buf)) > 0) {
					fos.write(buf, 0, len);
				}
				fos.close();
				fis.close();
					
				instance.addChild(Node.ELEMENT, fieldImageElement);
			}
			//Create instance element for field value:
			Element fieldElement = instance.createElement("", fieldName);
			if(field.has("value")){
				fieldElement.addChild(Node.TEXT, "" + field.getString("value"));
			} else if(field.has("default")){
				fieldElement.addChild(Node.TEXT, "" + field.getString("default"));
			}
			instance.addChild(Node.ELEMENT, fieldElement);
		}
		{
			Element metaElement = instance.createElement("", "meta");
			metaElement.addChild(Node.ELEMENT, instance.createElement("", "instanceID"));
			instance.addChild(Node.ELEMENT, metaElement);
		}
        //////////////
        Log.i(LOG_TAG, "Outputing the instance file:");
        //////////////
	    String instanceFilePath = new File(instanceDir.getAbsolutePath(),
	    		instanceName + ".xml").getAbsolutePath();
	    writeXMLToFile(instance, instanceFilePath);
	}
	/**
	 * Write the given XML tree out to a file
	 * @param elementTree
	 * @param outputPath
	 * @throws IOException
	 */
    private void writeXMLToFile(Element elementTree, String outputPath) throws IOException {
	    File instanceFile = new File(outputPath);
	    instanceFile.createNewFile();
	    FileWriter instanceWriter = new FileWriter(instanceFile);
	    KXmlSerializer instanceSerializer = new KXmlSerializer();
        instanceSerializer.setOutput(instanceWriter);
        elementTree.write(instanceSerializer);
        instanceSerializer.endDocument();
        instanceSerializer.flush();
        instanceWriter.close();
	}
    /**
     * Builds an XFrom from a JSON template and writes it out to the specified file.
     * It builds it as a string which really isn't the best way to go.
     * @param templatePaths
     * @param outputPath
     * @throws Exception 
     */
    public static void buildXForm(ArrayList<String> templatePaths, String outputPath) throws Exception {
    	String title = new File(templatePaths.get(0)).getName();
    	String id = title;
    	JSONArray initFields = new JSONArray();
		for(String templatePath : templatePaths){
			String jsonPath = new File(templatePath, "template.json").toString();
			//TODO: Using inheritance rules here seems unnecessary
			JSONArray templateFields = JSONUtils.applyInheritance( JSONUtils.parseFileToJSONObject(jsonPath) ).getJSONArray("fields");
			
			int templateFieldsLength = templateFields.length();
			for(int i = 0; i < templateFieldsLength; i++){
				initFields.put(templateFields.get(i));
			}
			Log.i(LOG_TAG, "Concated " + templatePath);
		}
		JSONArray fields = new JSONArray();
		int initFieldsLength = initFields.length();
		//Make fields array with no null values
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
		
		//Get the field names and labels:
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
		Log.i(LOG_TAG, "Writing output file...");
        FileWriter writer = new FileWriter(outputPath);
        writer.write("<h:html xmlns=\"http://www.w3.org/2002/xforms\" " +
                "xmlns:h=\"http://www.w3.org/1999/xhtml\" " +
                "xmlns:ev=\"http://www.w3.org/2001/xml-events\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "xmlns:jr=\"http://openrosa.org/javarosa\">");
        writer.write("<h:head>");
        writer.write("<h:title>" + title + "</h:title>");
        writer.write("<model>");
        writer.write("<instance>");
        writer.write("<data id=\"" + id + "\">");
        //writer.write("<xformstarttime/>");
        writer.write("<instance_creation_time/>");
        writer.write("<scan_output_directory/>");
        writer.write("<xformendtime/>");
        for(int i = 0; i < fieldsLength; i++){
        	JSONObject field = fields.getJSONObject(i);
        	if(field.getString("type").equals("note")) {
        		writer.write("<autogenerated_note_" + i + "/>");
        		continue;
        	}
        	JSONArray segments = field.optJSONArray("segments");
        	if(segments == null){
        		segments = new JSONArray();
        	}
        	for(int j = 0; j < segments.length(); j++){
	            writer.write("<" + fieldNames[i] + "_image_" + j + "/>");
        	}
        	if(field.has("default")){
        		writer.write("<" + fieldNames[i] + ">");
        		writer.write(field.getString("default"));
        		writer.write("</" + fieldNames[i] + ">");
        	} else {
        		writer.write("<" + fieldNames[i] + "/>");
        	}
        }
        writer.write("<meta><instanceID/></meta>");
        writer.write("</data>");
        writer.write("</instance>");
        writer.write("<itext>");
        writer.write("<translation lang=\"eng\">");
        for(int i = 0; i < fieldsLength; i++){
        	JSONObject field = fields.getJSONObject(i);
        	if(field.getString("type").equals("note")) {
                writer.write("<text id=\"/data/autogenerated_note_" + i + ":label\">");
                writer.write("<value>" + fieldLabels[i] + "</value>");
                writer.write("</text>");
        		continue;
        	}
            writer.write("<text id=\"/data/" + fieldNames[i] + ":label\">");
            writer.write("<value>" + fieldLabels[i] + "</value>");
            writer.write("</text>");
        }
        writer.write("</translation>");
        writer.write("</itext>");
        //Using the start preload param doesn't seem to work when a premade instance is loaded.
        //writer.write("<bind nodeset=\"/data/xformstarttime\" type=\"dateTime\" jr:preload=\"timestamp\" jr:preloadParams=\"start\"/>");
        writer.write("<bind nodeset=\"/data/instance_creation_time\" type=\"dateTime\" />");
        writer.write("<bind nodeset=\"/data/xformendtime\" type=\"dateTime\" jr:preload=\"timestamp\" jr:preloadParams=\"end\"/>");
        for(int i = 0; i < fieldsLength; i++){
        	JSONObject field = fields.getJSONObject(i);
        	if(field.getString("type").equals("note")) {
        		writer.write("<bind nodeset=\"/data/autogenerated_note_" + i + "\" readonly=\"true()\" type=\"string\"/>");
        		continue;
        	}
        	String type = field.optString("type", "string");
        	if(type.equals("input")){
        		//Deprecate input type?
        		type = "string";
        	}
        	String requiredString = "";
        	if(field.has("required")){
        		requiredString = " required=\"";
        		if(field.getBoolean("required")){
        			requiredString += "true()";
        		} else {
        			requiredString += "false()";
        		}
        		requiredString += "\" ";
        	}
        	String constraintString = "";
        	if(field.has("constraint")){
        		constraintString = " constraint=\"";
        		//TODO: This should be XML sanitized
        		constraintString += field.getString("constraint");
        		constraintString += "\" ";
        	}
        	writer.write("<bind nodeset=\"/data/" + fieldNames[i] +
        			"\" type=\"" + type + "\"" +
        			requiredString + constraintString +
        			" />");
        	
        	JSONArray segments = field.optJSONArray("segments");
        	if(segments == null){
        		segments = new JSONArray();
        	}
        	for(int j = 0; j < segments.length(); j++){
	            writer.write("<bind nodeset=\"/data/" + fieldNames[i] + "_image_" + j + "\" " +
	            		    //"appearance=\"web\" " +
				            "readonly=\"true()\" " + 
				            "type=\"binary\"/>");
        	}
        }
        writer.write("<bind calculate=\"concat(\'uuid:\', uuid())\" nodeset=\"/data/meta/instanceID\" readonly=\"true()\" type=\"string\"/>");
        writer.write("</model>");
        writer.write("</h:head>");
        writer.write("<h:body>");
        for(int i = 0; i < fieldsLength; i++){
        	JSONObject field = fields.getJSONObject(i);
        	if(field.getString("type").equals("note")) {
        		writer.write("<input ref=\"/data/autogenerated_note_" + i + "\">");
        		writer.write("<label ref=\"jr:itext('/data/autogenerated_note_" + i + ":label')\"/>");
        		writer.write("</input>");
        		continue;
        	}
        	
        	JSONArray segments = field.optJSONArray("segments");
        	if(segments == null){
        		segments = new JSONArray();
        	}
        	
        	String type = field.optString("type", "string");
        	String tag = "";
        	if( type.equals("select") || type.equals("select1") ){
        		tag = type;
        	} else {
        		tag = "input";
        	}
        	
        	writer.write("<group appearance=\"field-list\">");
        	writer.write("<" + tag +" ref=\"/data/" + fieldNames[i] + "\">");
        	writer.write("<label ref=\"jr:itext('/data/" + fieldNames[i] + ":label')\"/>");
        	//TODO: Make a xform_body_tag field instead?
        	//Advantage is simpler code and more flexibility for extending
        	if( tag.equals("select") || tag.equals("select1") ){
        		//Get the items from the first segment.
        		JSONObject segment = segments.getJSONObject(0);
                JSONArray items = segment.getJSONArray("items");
                for(int j = 0; j < items.length(); j++){
                	JSONObject item = items.getJSONObject(j);
                	String label = item.optString("label");
                	label = label == "" ? item.getString("value") : label;
	                writer.write("<item>");
	                writer.write("<label>" + label + "</label>");
	                writer.write("<value>" + item.getString("value") + "</value>");
	                writer.write("</item>");
                }
        	}
            writer.write("</" + tag + ">");
            for(int j = 0; j < segments.length(); j++){
	            writer.write("<upload ref=\"/data/" + fieldNames[i] + "_image_" + j  + "\" " +
	    		             "mediatype=\"image/*\" />");
            }
            writer.write("</group>");
        }
        writer.write("</h:body>");
        writer.write("</h:html>");
        writer.close();
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
}