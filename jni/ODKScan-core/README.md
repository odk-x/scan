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

==
	#install pkg-config:
	apt-get install pkg-config
	#add this to your .bashrc:
	export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/lib

Usage
=====

	make
	./ODKScan.run assets/form_templates/example example_input/img0.jpg output/img0

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
