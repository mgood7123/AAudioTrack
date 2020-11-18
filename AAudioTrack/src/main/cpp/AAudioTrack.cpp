//
// Created by matthew good on 18/11/20.
//

#include <jni.h>
#include <string>
#include "AAudioEngine/AAudioEngine.h"
#include "JniHelpers.h"

extern "C" JNIEXPORT jlong JNICALL
Java_smallville7123_aaudiotrack_AAudioTrack_createNativeInstance(JNIEnv* env, jobject  /* this */) {
    return reinterpret_cast<jlong>(new AAudioEngine());
}
extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack_AAudioTrack_getSampleRate(JNIEnv *env, jobject thiz,
                                                      jlong native__AAudioTrack_track_pointer) {
    return reinterpret_cast<AAudioEngine*>(native__AAudioTrack_track_pointer)->sampleRate;
}
extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack_AAudioTrack_getChannelCount(JNIEnv *env, jobject thiz,
                                                        jlong native__AAudioTrack_track_pointer) {
    return reinterpret_cast<AAudioEngine*>(native__AAudioTrack_track_pointer)->channelCount;
}
extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack_AAudioTrack_setTrack(JNIEnv *env, jobject thiz,
                                                 jlong native__AAudioTrack_track_pointer, jstring track) {
    const char * path_ = JniHelpers::Strings::newJniStringUTF(env, track);
    reinterpret_cast<AAudioEngine*>(native__AAudioTrack_track_pointer)->load(path_);
    JniHelpers::Strings::deleteJniStringUTF(&path_);
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack_AAudioTrack_getUnderrunCount(JNIEnv *env, jobject thiz,
                                                        jlong native__AAudioTrack_track_pointer) {
    return reinterpret_cast<AAudioEngine*>(native__AAudioTrack_track_pointer)->underrunCount;
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack_AAudioTrack_getCurrentFrame(JNIEnv *env, jobject thiz,
                                                        jlong native__AAudioTrack_track_pointer) {
    return reinterpret_cast<AAudioEngine*>(native__AAudioTrack_track_pointer)->mReadFrameIndex;
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack_AAudioTrack_getTotalFramesFrame(JNIEnv *env, jobject thiz,
                                                            jlong native__AAudioTrack_track_pointer) {
    return reinterpret_cast<AAudioEngine*>(native__AAudioTrack_track_pointer)->mTotalFrames;
}