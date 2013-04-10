ODKScan core
============

ODKScan is a tool for extracting information from images of paper forms.
It does optical mark recognition (so it can handle bubble/checkbox forms).
This repo contains the backend code that is shared between
[the ODKScan web-app](https://github.com/nathanathan/ODKScan_webapp)
and [the ODKScan Android app](https://github.com/villagereach/mScan).

Setup
=====

1. Install OpenCV

	Install guide: http://opencv.willowgarage.com/wiki/InstallGuide
	
	(If you run into any compile errors, disabling some of the features with cmake flags has helped me a couple times.)

2. Set OPENCV_INCLUDES in the makefile. If you're using ubuntu you just need to do this:

```
#install pkg-config:
apt-get install pkg-config
#add this to your .bashrc:
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib
```

Command Line Usage:
===================

The image processing code does not create directories.
It is necessairy to create the output folder and the segments folder inside of it in advance.

```
make
./ODKScan.run assets/form_templates/example example_input/img0.jpg output/img0
```

# Info for further development:

## How to modify the form alignment code:

Aligner.cpp does image alignment using the OpenCV 2D features framework.
This framework consistes of modular components for detecting, describing and matching features respectively.
(The OpenCV Docs have some tutorials on it available [here](http://docs.opencv.org/doc/tutorials/features2d/table_of_content_features2d/table_of_content_features2d.html#table-of-content-feature2d).)
These compontents are set and configured in the Aligner class's initialization function.

OpenCV provides a number of different classes to choose between for each module,
in addition to a number of different ways to parameterize them.
Figuring out which works the best has involved a lot trial and error for me.
(Many of the feature modules are designed with some goal or trade-off in mind
(e.g. speed vs. scale/rotation invariance).)
Setting the SHOW_MATCHES_WINDOW constant will display a debug window during alignments that shows
how various detector/descriptor/matcher settings are perfoming.

Aligner.cpp also transforms the entire input image by computing a homography from the matched features.
Using region based alignment instead may be been better for dealing with deformations.

## Adding new field types:

The field type property is passed though into the output JSON, so most names can be used.
All segments are output into the `segments` folder of the output directory regardless of the type,
so it should be easy to add a stage to the processing pipeline that handles a new type like barcode.
Segment objects in the output JSON have an `image_path` field that could help with this.

Source file information
=======================

* Processor (.h|.cpp) -- Handles JSON parsing and provides an interface.
* Aligner (.h|.cpp) -- Class for detecting and aligning forms.
* SegmentAligner (.h|.cpp) -- Contains code for aligning segments.
* AlignmentUtils (.h|.cpp) -- Functions useful for doing alignment related things like dealing with quads.
* PCA_classifier (.h|.cpp) -- Classifies bubbles using OpenCV's SVM with PCA. Contains code for reading training data from directories and refining bubble positions.
* Addons (.h|.cpp) -- Misc. utility functions that are useful in multiple places.
* FileUtils (.h|.cpp) -- A utility for crawling file trees and returning all the file names.
* MarkupForm (.h|.cpp) -- Contains a function for marking up a form using a JSON form template or bubble vals file.

Code previously available at https://github.com/villagereach/mScan/tree/master/TestSuite
