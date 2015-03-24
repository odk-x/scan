LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=off
OPENCV_MK_PATH:=OpenCV-2.3.1/share/OpenCV/OpenCV.mk
ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
	#try to load OpenCV.mk from default install location
	include $(TOOLCHAIN_PREBUILT_ROOT)/user/share/OpenCV/OpenCV.mk
else
	include $(OPENCV_MK_PATH)
endif

LOCAL_LDLIBS += $(OPENCV_LIBS) $(ANDROID_OPENCV_LIBS) -llog -ldl -lGLESv2
    
LOCAL_C_INCLUDES +=  $(OPENCV_INCLUDES) $(ANDROID_OPENCV_INCLUDES)

LOCAL_MODULE := bubblebot

CORE_SRCS := $(wildcard $(LOCAL_PATH)/ODKScan-core/src/*.cpp)
JSON_PARSER_SRCS := $(wildcard $(LOCAL_PATH)/ODKScan-core/jsoncpp-src-0.5.0/src/lib_json/*.cpp)

ZXING_SRCS := $(wildcard $(LOCAL_PATH)/ODKScan-core/zxing/core/src/zxing/*.cpp) $(wildcard $(LOCAL_PATH)/ODKScan-core/zxing/core/src/zxing/*/*.cpp) $(wildcard $(LOCAL_PATH)/ODKScan-core/zxing/core/src/zxing/*/*/*.cpp) $(wildcard $(LOCAL_PATH)/ODKScan-core/zxing/core/src/zxing/*/*/*/*.cpp) $(wildcard $(LOCAL_PATH)/ODKScan-core/zxing/cli/src/*.cpp)
BIGINT_SRCS := $(wildcard $(LOCAL_PATH)/ODKScan-core/zxing/core/src/bigint/*.cc)

#Remove the local path prefix from the srcs and put them in a single list
ODKSCAN_SRCS := $(subst $(LOCAL_PATH),, $(CORE_SRCS) $(JSON_PARSER_SRCS) $(ZXING_SRCS) $(BIGINT_SRCS))

LOCAL_C_INCLUDES += $(LOCAL_PATH)/ODKScan-core/src
LOCAL_C_INCLUDES += $(LOCAL_PATH)/ODKScan-core/jsoncpp-src-0.5.0/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/ODKScan-core/zxing/core/src
LOCAL_C_INCLUDES += $(LOCAL_PATH)/ODKScan-core/zxing/cli/src

LOCAL_SRC_FILES := $(ODKSCAN_SRCS) gen/bubblebot.cpp

include $(BUILD_SHARED_LIBRARY)
