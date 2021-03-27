#include <jni.h>
#include <string>
#include <AndroidDAW_SDK/plugin/Plugin.h>

class AudioPlayer : public Plugin {
    int plugin_type() override {
        return PLUGIN_TYPE_GENERATOR;
    }
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