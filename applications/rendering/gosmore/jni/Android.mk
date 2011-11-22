LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

CFLAGS := -g

LOCAL_MODULE := gosmore

LOCAL_CFLAGS := -DANDROID_NDK -DRES_DIR=\"/m8xp2az\"

LOCAL_SRC_FILES := openglespolygon.cpp gosmore.cpp libgosm.cpp 

LOCAL_LDLIBS := -lGLESv1_CM -ldl -llog

include $(BUILD_SHARED_LIBRARY)
