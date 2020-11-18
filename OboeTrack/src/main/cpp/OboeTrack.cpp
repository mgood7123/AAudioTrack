//
// Created by matthew good on 18/11/20.
//

#include <jni.h>
#include <string>
#include "OboeAudioEngine/OboeAudioEngine.h"
#include "JniHelpers.h"

extern "C" JNIEXPORT jlong JNICALL
Java_smallville7123_oboetrack_OboeTrack_createNativeInstance(JNIEnv* env, jobject  /* this */) {
    return reinterpret_cast<jlong>(new OboeAudioEngine());
}
extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_oboetrack_OboeTrack_getSampleRate(JNIEnv *env, jobject thiz,
                                                      jlong native__oboe_track_pointer) {
    return reinterpret_cast<OboeAudioEngine*>(native__oboe_track_pointer)->sampleRate;
}
extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_oboetrack_OboeTrack_getChannelCount(JNIEnv *env, jobject thiz,
                                                        jlong native__oboe_track_pointer) {
    return reinterpret_cast<OboeAudioEngine*>(native__oboe_track_pointer)->channelCount;
}
extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_oboetrack_OboeTrack_setTrack(JNIEnv *env, jobject thiz,
                                                 jlong native__oboe_track_pointer, jstring track) {
    const char * path_ = JniHelpers::Strings::newJniStringUTF(env, track);
    reinterpret_cast<OboeAudioEngine*>(native__oboe_track_pointer)->load(path_);
    JniHelpers::Strings::deleteJniStringUTF(&path_);
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_oboetrack_OboeTrack_getUnderrunCount(JNIEnv *env, jobject thiz,
                                                        jlong native__oboe_track_pointer) {
    return reinterpret_cast<OboeAudioEngine*>(native__oboe_track_pointer)->underrunCount;
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_oboetrack_OboeTrack_getCurrentFrame(JNIEnv *env, jobject thiz,
                                                        jlong native__oboe_track_pointer) {
    return reinterpret_cast<OboeAudioEngine*>(native__oboe_track_pointer)->mReadFrameIndex;
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_oboetrack_OboeTrack_getTotalFramesFrame(JNIEnv *env, jobject thiz,
                                                            jlong native__oboe_track_pointer) {
    return reinterpret_cast<OboeAudioEngine*>(native__oboe_track_pointer)->mTotalFrames;
}