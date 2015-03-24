package org.opendatakit.scan.android;

public class JavaScriptHandler {
	
	JSON2SurveyJSON parentActivity;
    public JavaScriptHandler(JSON2SurveyJSON activity) {
        parentActivity = activity;
    }
 
    public void setResult(String val){    
        this.parentActivity.javascriptCallFinished(val);
    }

}
