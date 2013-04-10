ODKScan
=====

Setting up your environment
===========================
This app is a mix of Java and native C++ code, so it can be a bit complicated to work with

The [OpenCV guide here](http://docs.opencv.org/doc/tutorials/introduction/android_binary_package/android_dev_intro.html#quick-environment-setup-for-android-development)
suggests using the [The NVIDIA Tegra Android development pack](https://developer.nvidia.com/tegra-android-development-pack) to setup your Android development environment.
It may take a few hours or a day to register for a Nvidia developer account in order to download it, but it can save a lot of trouble.

Install swig:

```bash
sudo apt-get install swig
```

Set the sdk path in `local.properties`

Set the ndk path in `local.env.mk`

Now you should be ready to build.

Building the project
====================

To clean the project:

1. Enter "make clean"
2. Enter "ant clean"

To build the project:

1. Enter "make" to build the C++ components
2. Enter "ant debug" to bulid the Java compononts and the Android application

Alternatively, enter "ant install" in step 2 to install the Android application to the connected
emulator or phone after the application is built.

If you want to set things up in eclipse take another look at the tutorial above.
There's a good chance you will run into some problems, but many can be solved simply by doing this:

1. From the top menu bar go to Project->Clean.
2. In the Package Explorer, right click on the project and select refresh.


Calling the via by intent
=========================

The ODKScan alignment code can be launched by itself via intent like so:

```java
Intent i = new Intent("org.opendatakit.scan.android.ALIGN");
i.putExtra("inputPath", "/sdcard/ODKScan/output/taken_2013-03-25_12-46-21/photo.jpg");
i.putExtra("templatePath", "/sdcard/ODKScan/form_templates/example");
i.putExtra("outputPath", "/sdcard/ODKScan/output/taken_2013-03-25_12-46-21/output.jpg");
startActivity(i);
```

Source tree information
=======================
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

Acknowledgments:
-----------
* Eleanor O'Rourke (eorourke@cs.washington.edu)
* Nicola Dell (nixdell@cs.washington.edu)
* Vincent Lung (vincent.lung@gmail.com)
* VillageReach (villagereach.org)
* Aijia, Baron, and Steve
