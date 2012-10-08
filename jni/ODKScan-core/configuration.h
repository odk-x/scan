/*
This file is included in:
formAlignment.cpp/.h
PCA_classifier.cpp/.h
Processor.cpp/.h

It defines platform specific constants
*/
#ifndef CONFIGURATION_H
#define CONFIGURATION_H

//Maybe rename this define or get rid of it.
//Right now the only place it gets used if for logging.
//I would prefer to put something in the android config.h that
//makes cout or a similar function do logging.
//#define USE_ANDROID_HEADERS_AND_IO

//For printing in a android comapatible way.
#define LOGI(x) (cout << (x) << endl)

//Processor.cpp
#define DEBUG_PROCESSOR
#define SEGMENT_OUTPUT_DIRECTORY "debug_segment_images/"
#define TRAINING_IMAGE_ROOT "training_examples"
#define DEFAULT_TRAINING_IMAGE_DIR "training_examples/android_training_examples"
//FormAlignmnet.cpp
#define DEBUG_ALIGN_IMAGE
#define OUTPUT_DEBUG_IMAGES
//#define ALWAYS_COMPUTE_TEMPLATE_FEATURES
//PCA_classifier.cpp
#define DEBUG_CLASSIFIER
#define OUTPUT_BUBBLE_IMAGES
#define OUTPUT_EXAMPLES
#endif
