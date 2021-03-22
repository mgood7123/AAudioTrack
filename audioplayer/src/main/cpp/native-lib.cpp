#include <jni.h>
#include <string>
#include "../../../../AAudioTrack2/src/main/cpp/smallville7123/Plugin_Type_Generator.h"

class AudioPlayer : public Plugin_Type_Generator {
};

extern "C" JNIEXPORT jlong JNICALL
Java_smallville7123_DAW_audioplayer_AudioPlayer_createNativeInstance(
        JNIEnv* env,
        jobject /* this */) {
    return reinterpret_cast<jlong>(new AudioPlayer());
}

extern "C" JNIEXPORT void JNICALL
Java_smallville7123_DAW_audioplayer_AudioPlayer_play(
        JNIEnv* env,
        jobject /* this */,
        jlong nativeInstance) {
    reinterpret_cast<AudioPlayer *>(nativeInstance)->mIsPlaying = false;
}

extern "C" JNIEXPORT void JNICALL
Java_smallville7123_DAW_audioplayer_AudioPlayer_pause(
        JNIEnv* env,
        jobject /* this */,
        jlong nativeInstance) {
    reinterpret_cast<AudioPlayer *>(nativeInstance)->mIsPlaying = true;
}