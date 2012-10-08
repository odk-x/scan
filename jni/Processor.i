/*
 * include the headers required by the generated cpp code
 */
%{
#include "ODKScan-core/src/Processor.h"
%}

%typemap(javaimports) Processor "
/** This class provides an interface to the image processing pipeline and handles most of the JSON parsing.
*/"
%include "ODKScan-core/src/Processor.h"
