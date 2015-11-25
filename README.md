# scan

This project is __*actively maintained*__

It is part of the ODK 2.0 Android tools suite.

ODK Scan takes an image of a filled-in mark-sense form and converts it into a data row accessible from ODK Tables or ODK Survey. Scan forms are generated with the [form designer](http://uw-ictd.github.io/odk-scan-form-designer/).

Instructions on how to use Scan can be found [here](https://opendatakit.org/use/2_0_tools/introduction-to-odk-scan/).

The developer [wiki](https://github.com/opendatakit/opendatakit/wiki) (including release notes) and
[issues tracker](https://github.com/opendatakit/opendatakit/issues) are located under
the [**opendatakit**](https://github.com/opendatakit/opendatakit) project.

The Google group for software engineering questions is: [opendatakit-developers@](https://groups.google.com/forum/#!forum/opendatakit-developers)

## Setting up your environment
This app makes use of the NDK, which is not yet fully integrated into the Android Studio/Gradle environment. Rebuilding the C++ is not necessary if you only want to modify the Java side, but to do NDK work you will need to complete the below steps until Android Studio fully supports NDK development.

We currently have only tested this process on Linux and Mac.

### C++ components

Install swig. If you are using Linux it can be obtained for your package manager: 

```bash
sudo apt-get install swig
```

If you are using a Mac you can use [Homebrew](http://brew.sh/) to install swig.

 ```bash
 homebrew install swig
 ```

If you are using Windows, we recommend using [Cygwin](https://www.cygwin.com/) for a terminal. In the installer you will want to include the swig, make, and gcc. 

Install the Android [NDK](https://developer.android.com/tools/sdk/ndk/index.html).

Set the NDK path in `local.env.mk`

### Android

General instructions for setting up an ODK 2.0 environment can be found at our [DevEnv Setup wiki page](https://github.com/opendatakit/opendatakit/wiki/DevEnv-Setup)

Install [Android Studio](http://developer.android.com/tools/studio/index.html) and the [SDK](http://developer.android.com/sdk/index.html#Other).

This project depends on ODK's [androidlibrary](https://github.com/opendatakit/androidlibrary) and [androidcommon](https://github.com/opendatakit/androidcommon) projects; their binaries will be downloaded automatically fom our maven repository during the build phase. If you wish to modify them yourself, you must clone them into the same parent directory as scan. You directory stucture should resemble the following:

        |-- odk

            |-- androidcommon

            |-- androidlibrary

            |-- scan


  * Note that this only applies if you are modifying the library projects. If you use the maven dependencies (the default option), the projects will not show up in your directory. 
    
ODK [Core](https://github.com/opendatakit/core) __MUST__ be installed on your device, whether by installing the APK or by cloning the project and deploying it. ODK [Survey](https://github.com/opendatakit/survey) and ODK [Tables](https://github.com/opendatakit/tables) also integrate well with ODK Scan, but are not required.

Now you should be ready to build.

## Building the project

### C++ components
**NOTE** Building the C++ components is NOT necessary. The app comes bundled with prebuilt .so files.

To build, open a terminal in the scan/bubblebot\_lib directory.

To clean the C++ components:

 ```bash
make clean
 ```

To build the C++ components:

 ```bash
make
 ```

This will generate new .so files in the scan/src/main/jniLibs directory.

### Android

Open the Scan project in Android Studio. As long as androidlibrary and androidcommon are in the same parent directory, you should be able to select Build->Make Project to build the app.

## Running

Be sure to install ODK Core onto your device before attempting to run Scan.

The first time Scan is run it will take a few minutes to create its file structure and initialize its state. Subsequent launches should be much faster.

## Architecture

The initial training data (except for the number classifier, which uses a neural net) and form texmplates are included in the assets folder.
When a new version of the app is first launched they are installed into the ODKScan directory by RunSetup.

The processing pipeline includes 3 main components:

* PhotographForm launches the camera app to get a picture of the form, removes duplicates photos and launches the processing services.
* ProcessInBG is a service that runs the ODKScan-core cord in background threads, outside of the activity, and creates notifications when it is complete.
* DisplayProcessedForm displays the marked-up form image and allows the user to save the data to the ODK database accessible by the rest of the 2.0 tools. It is launched from the notifications ProcessInBG creates.

## Source tree information
Quick description of the content in the root folder:

.

|-- bubblebot\_lib          -- Source tree for C++ components

    |-- OpenCV-2.3.1        -- OpenCV library

    |-- local.env.mk        -- Build configuration and local path to NDK

    |-- default.properties  -- Default build properties

    |-- Makefile            -- Makefile for C++ components

    |-- jni                 -- Source tree for C++ components

|-- scan\_app               -- Source tree for Java components

    |-- src

       |-- main

          |-- jniLibs       -- C++ build output

          |-- res           -- Source tree for Android resources

          |-- java

             |-- org

                |-- opendatakit

                   |-- scan

                      |-- android   -- The most relevant Java code lives here

## Acknowledgments:
* Eleanor O'Rourke (eorourke@cs.washington.edu)
* Nicola Dell (nixdell@cs.washington.edu)
* Vincent Lung (vincent.lung@gmail.com)
* VillageReach (villagereach.org)
* Aijia, Baron, and Steve
