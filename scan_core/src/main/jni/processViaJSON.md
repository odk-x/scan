processViaJSON
--------------
Scan has a processViaJSON function that allows it to be invoked
just by passing it a JSON configuration string like this one:

```json
{
  "inputImage" : "",
  "outputDirectory" : "",
	"templatePath" : ""
}
````

The JSON below contains all the currently supported properties,
and their default values. More may be added in the future.
The JSON API is forwards compatible so extra properties can be 
passed in that aren't yet supported.

```javascript
{
  "inputImage" : "",
	"outputDirectory" : "",
	"alignedFormOutputPath" : outputDirectory + "aligned.jpg",
	"markedupFormOutputPath" : outputDirectory + "markedup.jpg",
	"jsonOutputPath" : outputDirectory + "output.json",
	"alignForm" : true,
	"processForm" : true,
	"detectOrientation" : true,
	"templatePath" : "",
	"templatePaths" : [],
	"calibrationFilePath" : "",
	"trainingDataDirectory" : "training_examples/"
  "trainingModelDirectory" : "training_models/"
}
```

Scan returns a JSON string like this:

```json
{
	"errorMessage" : "This is only here if there's an error",
	"templatePath" : "path/to/template/that/was/used/for/processing"
}
```

The Android ProcessInBG wraps processViaJSON and expects the
JSON config string to be in the "config" property of its extras.



