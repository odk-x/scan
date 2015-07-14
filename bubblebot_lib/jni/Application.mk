APP_STL := gnustl_static
APP_CPPFLAGS := -frtti -fexceptions
#Not sure if this helps
APP_OPTIM := release
APP_ABI := armeabi-v7a armeabi
#Some devices (ViewPad7) don't support armabi-v7a and you have to use armeabi (or possibly others) instead:
#to find out if your device is affected run this command: adb shell cat /proc/cpuinf
#The current setting compiles for both instruction sets but this means compile times are even slower.
