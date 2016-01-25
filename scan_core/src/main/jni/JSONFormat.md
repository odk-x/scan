ODKScan requires a JSON document to provide information about how it should Scan a form.
An annotated version of that document is returned containing the information 
collected from a form.
Below is a description of the JSON structure. Annotations added to the output are marked:

```javascript
{
  "fields" : 
	[
		//Each object in this array represents a field.
		{
			//If the fields items have string classifications
			//they are joined into a single string
			//using this delimiter to form the filed value.
			"delimiter" : ", ",
			//The label of a field shown in collect
			"label" : "name",
			//The name of the field that will be used for backend storage
			//When exporting to Collect a "q_" prefix is added
			//and all spaces are replaced with underscores.
			"name" : "1",
			"segments" : 
			[
				//A field can have multiple segments,
				//this can help alignment in some cases,
				//but if I were to redesing this I think
				//a single segments might be better.
				{
					//This sets whether the segment should be aligned by looking
					//for a surrouding border.
					"align_segment" : true,
					//Annotation
					//This is the path the segment image was saved to.
					"image_path" : "output/test/segments/name_image_0.jpg",
					"items" : 
					[
						//Each item is a classifiable object.
						{
							//Annotation
							//Indicates the pixel location of the item
							//relative to the top-left corner of the
							//form image *after alignment*
							"absolute_location" : [ 49, 129 ],
							//Annotation
							"classification" : 
							{
								"value" : "1",
								//The confidence is only shown
								//if the classifier used
								//returns some kind of 
								//confidence metric.
								"confidence" : 1.0
							},
							"item_x" : 18,
							"item_y" : 22.50
						}
					],
					//Annotation
 					//The corners of the aligned segment.
					"quad" : 
					[
						[ 31, 109 ],
						[ 406, 106 ],
						[ 406, 150 ],
						[ 31, 152 ]
					],
					"segment_height" : 44,
					"segment_width" : 375,
					"segment_x" : 31,
					"segment_y" : 107
				}
			],
			//The field's type, mainly for the benefit of Collect.
			"type" : "string",
			//Annotation
			//The field's value.
			"value" : "1111111111"
		}
	],
	
	//Annotation
	//The form might be scaled to a different size when processed.
	//If that is the case the scaling factor is recorded here.
	"form_scale" : 1.0,
	//Annotation
	"templatePath" : "test/template.json"
	//Annotation
	"output_path" : "output/test/",
	//Annotation
	"timestamp" : "2013-05-30.12:02:00",
	"height" : 1088,
	"width" : 832
	"classifier" : {
		//Classification labels
		//can be mapped to different
		//values
		"classification_map": {
			"filled": true,
			"partiallyfilled": true,
		},
		//If the classiciation label is not
		//in the map the default classification
		//is used.
		"default_classification": false,
		//If this property is present 2-class classification
		//will be done so a confidence rating can be returned.
		"negative_label" : "empty"
		//The directory contianing the classifier
		//training examples
		"training_data_uri": "bubbles",
		"classifier_height": 28,
		"classifier_width": 20,
		//If non zero the item locations
		//will be refined to a new location
		//within the given pixel radius
		"alignment_radius": 2.0,
		"advanced": {
			//If your trainin data is symetric
			//you can get more training examples
			//for free by flipping it.
			"flip_training_data": false,
			//Parameter for PCA SVM classifier.
			"eigenvalues" : 13
		}
	}
}
```
