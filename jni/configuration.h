/*
This file is included in:
formAlignment.cpp/.h
PCA_classifier.cpp/.h
Processor.cpp/.h

It defines platform specific constants
*/
#ifndef CONFIGURATION_H
#define CONFIGURATION_H

#define DEFAULT_TRAINING_IMAGE_DIR "/sdcard/mScan/training_examples"

#define EIGENBUBBLES 7

#include <sys/stat.h>
#include "log.h"
#define LOG_COMPONENT "ODKScan"

//Flag for zxing to prevent a build bug
#define NO_ICONV

#endif
