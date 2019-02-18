//
// Created by SteveMac on 15/02/2019.
//
#include <jni.h>

#ifndef SMFRAMEWORK_IMGPROC_H
#define SMFRAMEWORK_IMGPROC_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT
jstring JNICALL Java_com_interpark_smframework_NativeImageProcess_ImageProcessing_stringFromJNI
        (JNIEnv *, jclass);

JNIEXPORT
void Java_com_interpark_smframework_NativeImageProcess_ImageProcessing_callTest
        (JNIEnv *, jclass);

JNIEXPORT
void JNICALL Java_com_interpark_smframework_NativeImageProcess_ImageProcessing_glGrabPixels
        (JNIEnv *, jclass, jint, jint, jobject, jboolean);

#ifdef __cplusplus
}
#endif
#endif //SMFRAMEWORK_IMGPROC_H
