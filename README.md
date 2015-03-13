# ODKScan

## Setting up your environment
This app is a mix of Java and native C++ code, so it can be a bit complicated to work with.

### C++ components

Install swig:

```bash
sudo apt-get install swig
```

If you are using a Mac you can use [Homebrew](http://brew.sh/) to install swig.
 
 ```bash
 homebrew install swig
 ```

Install the Android [NDK](https://developer.android.com/tools/sdk/ndk/index.html) and [SDK](http://developer.android.com/sdk/index.html#Other).

Set the SDK path in `local.properties`

Set the NDK path in `local.env.mk`

### Android 

Install [Eclipse](https://www.eclipse.org/downloads/) and the [ADT Plugin](http://developer.android.com/sdk/installing/installing-adt.html).

This project depends on ODK's AndroidCommon and PlayServices projects, so be sure to clone those as well. ODK Survey and ODK Tables also integrate well with ODK Scan, but are not required.

Import PlayServices and AndroidCommon into Eclipse first. Then import ODK Survey. 

Now you should be ready to build.

## Building the project

### C++ components
Open a terminal in the opendatakit.scan directory.

To clean the C++ components:

 ```bash
make clean
 ```

To build the C++ components:

 ```bash
make
 ```

### Android

Eclipse builds the project automatically by default, but as we change the C++ we may need to manually clean and rebuild the project. Follow the steps below after building the C++:

1. From the top menu bar go to Project->Clean.
2. In the Package Explorer, right click on the project and select refresh.

## Calling the via by intent

The ODKScan alignment code can be launched by itself via intent like so:

```java
Intent i = new Intent("org.opendatakit.scan.android.ALIGN");
i.putExtra("inputPath", "/sdcard/ODKScan/output/taken_2013-03-25_12-46-21/photo.jpg");
i.putExtra("templatePath", "/sdcard/ODKScan/form_templates/example");
i.putExtra("outputPath", "/sdcard/ODKScan/output/taken_2013-03-25_12-46-21/output.jpg");
startActivity(i);
```

## Architecture

The initial training data and form texmplates are included in the assets folder.
When a new version of the app is first launched they are installed into the ODKScan directory by RunSetup.

The processing pipeline includes 3 main components:

* PhotographForm launches the camera app to get a picture of the form, removes duplicates photos and launches the processing services.
* ProcessInBG is a service that runs the ODKScan-core cord in background threads, outside of the activity, and creates notifications when it is complete.
* DisplayProcessedForm displays the marked-up form image and allows the user to save the data to ODK Collect. It is launched from the notifications ProcessInBG creates.

## Source tree information
Quick description of the content in the root folder:

* AndroidManifest.xml -- Manifest of the Android application
* android-opencv.mk   -- Build settings for C++ components (possibly unnecessary)
* Makefile	-- Makefile for C++ components
* build.xml -- Build manifest for the Android application
* default.properties  -- Default build properties for the Android application
* jni -- Source tree for C++ components
* local.* -- Build configuration file
* OpenCV-2.3.1/ -- OpenCV library
* proguard.cfg -- Proguard configuration file
* res -- Source tree for Java resources
* src -- Source tree for Java components

## Acknowledgments:
* Eleanor O'Rourke (eorourke@cs.washington.edu)
* Nicola Dell (nixdell@cs.washington.edu)
* Vincent Lung (vincent.lung@gmail.com)
* VillageReach (villagereach.org)
* Aijia, Baron, and Steve
