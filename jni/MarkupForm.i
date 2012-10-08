/*
 * include the headers required by the generated cpp code
 */
%{
#include "MarkupForm.h"
%}

%typemap(javaimports) MarkupForm "

/** MarkupForm - This class is used to mark up form images with the JSON output.
*/"

%include "ODKScan-core/src/MarkupForm.h"
