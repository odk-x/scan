%module bubblebot

/*
 * the java import code muse be included for the opencv jni wrappers
 * this means that the android project must reference opencv/android as a project
 * see the default.properties for how this is done
 */

%pragma(java) jniclasscode=%{
	static {
		try {

			System.loadLibrary("bubblebot");
		} catch (UnsatisfiedLinkError e) {
			//badness
			throw e;
		}
	}

%}

%include "Processor.i"
