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
void Java_com_interpark_smframework_NativeImageProcess_ImageProcessing_exitApp(
        JNIEnv *env,
        jclass thiz
)
{
    exit(0);
}

//JNIEXPORT
//jfloatArray JNICALL Java_com_interpark_smframework_NativeImageProcess_ImageProcessing_transformVec4(
//        JNIEnv *env,
//        jclass thiz,
//        jfloatArray  jm,
//        jfloat  jx,
//        jfloat jy,
//        jfloat  jz,
//        jfloat jw,
//        jfloatArray jdst
//)
//{
//    int msize = env->GetArrayLength(jm);
//    float* m = new float[msize];
//    jfloat *el = env->GetFloatArrayElements(jm, 0);
//    for (int i = 0; i < msize; ++i) {
//        m[i] = el[i];
//    }
//
//    float x = jx;
//    float y = jy;
//    float z = jz;
//    float w = jw;
//
//    msize = env->GetArrayLength(jdst);
//    float* dst = new float[msize];
//    el = env->GetFloatArrayElements(jdst, 0);
//    for (int i = 0; i < msize; ++i) {
//        dst[i] = el[i];
//    }
//
//    asm volatile(
//    "ld1    {v0.s}[0],        [%1]    \n\t"    // V[x]
//    "ld1    {v0.s}[1],        [%2]    \n\t"    // V[y]
//    "ld1    {v0.s}[2],        [%3]    \n\t"    // V[z]
//    "ld1    {v0.s}[3],        [%4]    \n\t"    // V[w]
//    "ld1    {v9.4s, v10.4s, v11.4s, v12.4s}, [%5]   \n\t"    // M[m0-m7] M[m8-m15]
//
//
//    "fmul v13.4s, v9.4s, v0.s[0]           \n\t"      // DST->V = M[m0-m3] * V[x]
//    "fmla v13.4s, v10.4s, v0.s[1]           \n\t"    // DST->V += M[m4-m7] * V[y]
//    "fmla v13.4s, v11.4s, v0.s[2]           \n\t"    // DST->V += M[m8-m11] * V[z]
//    "fmla v13.4s, v12.4s, v0.s[3]           \n\t"    // DST->V += M[m12-m15] * V[w]
//
//    //"st1 {v13.4s}, [%0]               \n\t"    // DST->V[x, y] // DST->V[z]
//    "st1 {v13.2s}, [%0], 8               \n\t"
//    "st1 {v13.s}[2], [%0]                \n\t"
//    :
//    : "r"(dst), "r"(&x), "r"(&y), "r"(&z), "r"(&w), "r"(m)
//    : "v0", "v9", "v10","v11", "v12", "v13", "memory"
//    );
//
//    jfloatArray result = env->NewFloatArray(msize);
//    env->SetFloatArrayRegion(result, 0, msize, dst);
//    return result;
//}



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
