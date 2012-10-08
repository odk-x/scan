package org.opendatakit.scan.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
/**
 * This activity converts a ODKScan JSON file into a XForm for use with Collect
 * then returns a uri for the XForm instance as a result.
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
	
	private String photoName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			//Read in parameters from the intent's extras.
			Bundle extras = getIntent().getExtras();

			if(extras == null){ throw new Exception("No parameters specified"); }
			
			photoName = extras.getString("photoName");
			String jsonOutPath = ScanUtils.getJsonPath(photoName);
			String templatePath = extras.getString("templatePath");
			
			if(jsonOutPath == null){ throw new Exception("jsonOutPath is null"); }
			if(templatePath == null){ throw new Exception("Could not identify template."); }
			
			Log.i(LOG_TAG,"jsonOutPath : " + jsonOutPath);
			Log.i(LOG_TAG,"templatePath : " + templatePath);
			
			String templateName = new File(templatePath).getName();

			String jsonPath = new File(templatePath, "template.json").getPath();
			String xFormPath = new File(templatePath, templateName + ".xml").getPath();

			//////////////
			Log.i(LOG_TAG, "Checking if there is no xform or the xform is out of date.");
			//////////////
			File xformFile = new File(xFormPath);
			if( !xformFile.exists() || 
				new File(jsonPath).lastModified() > xformFile.lastModified()){
				//////////////
				Log.i(LOG_TAG, "Unregistering any existing old versions of xform.");
				//////////////
			    String [] deleteArgs = { templateName };
		        int deleteResult = getContentResolver().delete(COLLECT_FORMS_CONTENT_URI, "jrFormId like ?", deleteArgs);
		        Log.w(LOG_TAG, "Removing " + deleteResult + " rows.");
				//////////////
				Log.i(LOG_TAG, "Creating new xform.");
				//////////////
			    buildXForm(jsonPath, xFormPath, templateName, templateName);
			}
			String jrFormId = verifyFormInCollect(xFormPath, templateName);
			//////////////
			Log.i(LOG_TAG, "Checking if the form instance is already registered with collect.");
			//////////////
			int instanceId;
		    String instanceName = templateName + '_' + photoName;
		    String instancePath = "/sdcard/odk/instances/" + instanceName + "/";
		    (new File(instancePath)).mkdirs();
		    String instanceFilePath = instancePath + instanceName + ".xml";
			String selection = "instanceFilePath = ?";
	        String[] selectionArgs = { instanceFilePath };
	        Cursor c = getContentResolver().query(COLLECT_INSTANCES_CONTENT_URI, null, selection, selectionArgs, null);
	        //Log.i(LOG_TAG, Arrays.toString(c.getColumnNames()));
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
	    		jsonOut2XFormInstance(jsonOutPath, xFormPath, instancePath, instanceName);
	            ContentValues insertValues = new ContentValues();
	            insertValues.put("displayName", instanceName);
	            insertValues.put("instanceFilePath", instanceFilePath);
	            insertValues.put("jrFormId", jrFormId);
	            Uri insertResult = getContentResolver().insert(
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
			                finish();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}
		
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
	 */
	private void jsonOut2XFormInstance(String jsonOutFile, String xFormPath, String instancePath, String instanceName)
			throws JSONException, IOException, XmlPullParserException {
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
        instance.addChild(Node.ELEMENT, instance.createElement("", "xformstarttime"));
        instance.addChild(Node.ELEMENT, instance.createElement("", "xformendtime"));
        //////////////
        Log.i(LOG_TAG, "Parsing the JSON output:");
        //////////////
        JSONObject formRoot = JSONUtils.parseFileToJSONObject(jsonOutFile);
		JSONArray fields = formRoot.getJSONArray("fields");
		int fieldsLength = fields.length();
		if(fieldsLength == 0){
			throw new JSONException("There are no fields in the json output file.");
		}
		//////////////
		Log.i(LOG_TAG, "Transfering the values from the JSON output into the xform instance:");
		//////////////
		for(int i = 0; i < fieldsLength; i++){
			JSONObject field = fields.optJSONObject(i);
        	if(field.getString("type") == "note") {
        		Element fieldElement = instance.createElement("", "autogenerated_note_" + i);
        		instance.addChild(Node.ELEMENT, fieldElement);
        		continue;
        	}
			String fieldName = field.getString("name");
			JSONArray segments = field.getJSONArray("segments");
			//Add segment images
			for(int j = 0; j < segments.length(); j++){
				JSONObject segment = segments.getJSONObject(j);
				String imageName = fieldName + "_image_" + j;
				Element fieldImageElement = instance.createElement("", imageName);
				if(!segment.has("image_path") || segment.isNull("image_path") ){
					fieldImageElement.addChild(Node.TEXT, "segmentNotAligned.jpg");
					instance.addChild(Node.ELEMENT, fieldImageElement);
					continue;
				}
				String imagePath = segment.getString("image_path");
				fieldImageElement.addChild(Node.TEXT, new File(imagePath).getName());
				//Copy segment image
				InputStream fis = new FileInputStream(imagePath);
				FileOutputStream fos = new FileOutputStream(instancePath + new File(imagePath).getName());
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
        //////////////
        Log.i(LOG_TAG, "Outputing the instance file:");
        //////////////
	    String instanceFilePath = instancePath + instanceName + ".xml";
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
     * @param templatePath
     * @param outputPath
     * @param title
     * @param id
     * @throws IOException
     * @throws JSONException 
     */
    public static void buildXForm(String templatePath, String outputPath, String title, String id) throws IOException, JSONException {
		Log.i(LOG_TAG, templatePath);
		JSONObject formRoot = JSONUtils.applyInheritance( JSONUtils.parseFileToJSONObject(templatePath) );

		Log.i(LOG_TAG, "Parsed");
		JSONArray initFields = formRoot.getJSONArray("fields");
		int initFieldsLength = initFields.length();
		
		//Make fields array with no null values
		JSONArray fields = new JSONArray();
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
				fieldNames[i] = field.getString("name");
				fieldLabels[i] = field.optString("label", field.getString("name"));
			}
			else{
				Log.i(LOG_TAG, "Field " + i + " has no name.");
				//throw new JSONException("Field " + i + " has no name or label.");
			}
		}
    	
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
        writer.write("<xformstarttime/>");
        writer.write("<xformendtime/>");
        for(int i = 0; i < fieldsLength; i++){
        	JSONObject field = fields.getJSONObject(i);
        	if(field.getString("type").equals("note")) {
        		writer.write("<autogenerated_note_" + i + "/>");
        		continue;
        	}
        	JSONArray segments = field.getJSONArray("segments");
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
        writer.write("<bind nodeset=\"/data/xformstarttime\" type=\"dateTime\" jr:preload=\"timestamp\" jr:preloadParams=\"start\"/>");
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
        		requiredString = "required=\"";
        		if(field.getBoolean("required")){
        			requiredString += "true()";
        		} else {
        			requiredString += "false()";
        		}
        		requiredString += "\"";
        	}
        	String constraintString = "";
        	if(field.has("constraint")){
        		constraintString = "constraint=\"";
        		//TODO: This should be XML sanitized
        		constraintString += field.getString("constraint");
        		constraintString += "\"";
        	}
        	writer.write("<bind nodeset=\"/data/" + fieldNames[i] +
        			"\" type=\"" + type + "\"" +
        			requiredString + constraintString +
        			" />");
        	
        	JSONArray segments = field.getJSONArray("segments");
        	for(int j = 0; j < segments.length(); j++){
	            writer.write("<bind nodeset=\"/data/" + fieldNames[i] + "_image_" + j + "\" " +
	            		    "appearance=\"web\"" +
				            "readonly=\"true()\" " + 
				            "type=\"binary\"/>");
        	}
        }
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
        	JSONArray segments = field.getJSONArray("segments");
        	
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
}