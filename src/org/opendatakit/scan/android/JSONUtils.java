package org.opendatakit.scan.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JSONUtils {
	// Prevent instantiations
	private JSONUtils() {
	}

	private static JSONObject inheritFrom(JSONObject child, JSONObject parent)
			throws JSONException {
		Iterator<?> propertyIterator = parent.keys();
		while (propertyIterator.hasNext()) {
			String currentProperty = (String) propertyIterator.next();
			if (!child.has(currentProperty)) {
				child.put(currentProperty, parent.get(currentProperty));
			}
		}
		return child;
	}

	/**
	 * Applies the following inheritance rules to the object and returns the
	 * result: fields inherit from the root JSONObject segments inherit from
	 * fields
	 * 
	 * @throws JSONException
	 */
	public static JSONObject applyInheritance(JSONObject obj)
			throws JSONException {
		JSONArray fields = obj.getJSONArray("fields");
		int fieldsLength = fields.length();
		for (int i = 0; i < fieldsLength; i++) {
			JSONObject field = inheritFrom(fields.getJSONObject(i), obj);

			JSONArray segments = field.getJSONArray("segments");
			for (int j = 0; j < segments.length(); j++) {
				JSONObject segment = segments.getJSONObject(j);
				segments.put(j, inheritFrom(segment, field));
			}
		}
		return obj;
	}

	public static void writeJSONObjectToFile(JSONObject obj, String outPath)
			throws JSONException, IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(outPath));
		out.write(obj.toString(4));
		out.close();
	}

	public static JSONObject parseFileToJSONObject(String bvFilename)
			throws JSONException, IOException {
		File jsonFile = new File(bvFilename);

		// Read text from file
		StringBuilder text = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		String line;

		while ((line = br.readLine()) != null) {
			text.append(line);
		}
		
		br.close();

		return new JSONObject(text.toString());
	}

	public static JSONObject[] JSONArray2Array(JSONArray jsonArray)
			throws JSONException {
		JSONObject[] output = new JSONObject[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			output[i] = jsonArray.getJSONObject(i);
		}
		return output;
	}
}
