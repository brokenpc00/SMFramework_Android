//
// Created by SteveMac on 15/02/2019.
//

#include "ImgProc.h"

#include <jni.h>
#include <string>
#include <stdio.h>
#include <android/log.h>
#include <android/api-level.h>

#include <android/bitmap.h>
#include <GLES2/gl2.h>

#include "ImgProcDef.h"

#define  LOG_TAG    "native-lib"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)


JNIEXPORT
jstring JNICALL Java_com_interpark_smframework_NativeImageProcess_ImageProcessing_stringFromJNI(
        JNIEnv *env,
        jclass thiz
)
{
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT
void Java_com_interpark_smframework_NativeImageProcess_ImageProcessing_callTest(
        JNIEnv *env,
        jclass thiz
)
{
    LOGD("[[[[[ callTest!!!!");
}

JNIEXPORT
void JNICALL Java_com_interpark_smframework_NativeImageProcess_ImageProcessing_glGrabPixels(
        JNIEnv *env,
        jclass obj,
        jint x,
        jint y,
        jobject bitmap,
        jboolean zeroNonVisiblePixels
)
{
//    LOGD("[[[[[ glGrabPixels 1!!!!");

    AndroidBitmapInfo info;
    uint32_t  * pixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        // failed to get bitmap info
        LOGE("AndroidBitmap_getInfo(env, bitmap, &info) failed !!!");
        return;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        // made bitmap... for RGBA8888... but...?
        return;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, (void**)&pixels) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed !!!");
        return;
    }

//    LOGD("[[[[[ glGrabPixels 2!!!!");

    glReadPixels(x, y, info.width, info.height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

    if (zeroNonVisiblePixels) {
        int length = info.width * info.height;
        for (int i = 0; i < length; ++i) {
            if (!(*pixels & 0xFF000000)) {
                *pixels = 0;
            }
            pixels++;
        }
    }

//    LOGD("[[[[[ glGrabPixels 3 width : %d, height %d", info.width, info.height);
    AndroidBitmap_unlockPixels(env, bitmap);
}
