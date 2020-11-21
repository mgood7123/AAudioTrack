//
// Created by matthew good on 18/11/20.
//

#include <jni.h>
#include <string>
#include <unistd.h>
#include "ardour/ardour.h"
#include "other/JniHelpers.h"

using namespace ARDOUR;
AudioEngine * engine = nullptr;

extern "C" JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_createNativeInstance(JNIEnv* env, jobject  /* this */) {
    // create the engine
    engine = AudioEngine::create();

    // set the backend
    //
    // for some reason, the backend gets destructed when exiting from set_backend
    // i am unable to reproduce this in a test case
    // as a workaround, manually set the _backend to the result of set_backend
    //
    if (!(_backend = engine->set_backend ("AAudio", "Unit-Test", ""))) {
        LOGF("Cannot create Audio/MIDI engine.\n");
    }

    // start the engine
    if (engine->start () != 0) {
        LOGF("Cannot start Audio/MIDI engine.\n");
    }

    // TODO: port Session
    // refer to Zrythm to details of its session implementation
    // Ardour's Session is vastly complex and involves a whole bunch of extra classes
    //     and does a lot of work

    // TODO: actually get audio playing

//    Session* session = new Session (*engine, dir, state, bus_profile_ptr, template_path);
//    engine->set_session (session);
}

extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_startEngine(JNIEnv *env, jobject thiz) {
//    AudioEngine::_instance->start();
}

extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_stopEngine(JNIEnv *env, jobject thiz) {
//    AudioEngine::_instance->stop();
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_getSampleRate(JNIEnv *env, jobject thiz) {
    if (_backend == nullptr) {
        LOGF("getsamplerate: backend dissapeared!");
    }
    return engine->current_backend()->sample_rate();
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_getChannelCount(JNIEnv *env, jobject thiz) {
    if (_backend == nullptr) {
        LOGF("getchannelcount: backend dissapeared!");
    }
    return engine->current_backend()->output_channels();
}
extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_setTrack(JNIEnv *env, jobject thiz, jstring track) {
    if (_backend == nullptr) {
        LOGF("setTrack: backend dissapeared!");
    }
    if (engine == nullptr || engine->current_backend() == nullptr) return;
//    const char * path_ = JniHelpers::Strings::newJniStringUTF(env, track);
//    reinterpret_cast<AudioEngine*>(native_aaudio_track_pointer)->load(path_);
//    JniHelpers::Strings::deleteJniStringUTF(&path_);
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_getUnderrunCount(JNIEnv *env, jobject thiz) {
    if (_backend == nullptr) {
        LOGF("getunderruncount: backend dissapeared!");
    }
//    return engine->current_backend()->output_channels();
    return 0; //reinterpret_cast<AudioEngine*>(native_aaudio_track_pointer)->underrunCount;
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_getCurrentFrame(JNIEnv *env, jobject thiz) {
    if (_backend == nullptr) {
        LOGF("getcurrentframe: backend dissapeared!");
    }
    return 0; //reinterpret_cast<AudioEngine*>(native_aaudio_track_pointer)->mReadFrameIndex;
}

extern "C"
JNIEXPORT jint JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_getTotalFrames(JNIEnv *env, jobject thiz) {
    if (_backend == nullptr) {
        LOGF("gettotalframes: backend dissapeared!");
    }
    return 0; //reinterpret_cast<AudioEngine*>(native_aaudio_track_pointer)->mTotalFrames;
}

extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_resetPlayHead(JNIEnv *env, jobject thiz) {
    if (_backend == nullptr) {
        LOGF("resetplayhead: backend dissapeared!");
    }
//    reinterpret_cast<AudioEngine*>(native_aaudio_track_pointer)->mReadFrameIndex = 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_pause(JNIEnv *env, jobject thiz) {
    if (_backend == nullptr) {
        LOGF("pause: backend dissapeared!");
    }
//    AudioEngine* AE = reinterpret_cast<AudioEngine*>(native_aaudio_track_pointer);
//    if (!AE->metronomeMode.load()) AE->mIsPlaying.store(false);
}

extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_resume(JNIEnv *env, jobject thiz) {
    if (_backend == nullptr) {
        LOGF("resume: backend dissapeared!");
    }
//    AudioEngine* AE = reinterpret_cast<AudioEngine*>(native_aaudio_track_pointer);
//    if (!AE->metronomeMode.load()) AE->mIsPlaying.store(true);
}

extern "C"
JNIEXPORT void JNICALL
Java_smallville7123_aaudiotrack2_AAudioTrack2_loop(JNIEnv *env, jobject thiz,
                                                 jboolean value) {
    if (_backend == nullptr) {
        LOGF("loop: backend dissapeared!");
    }
//    AudioEngine* AE = reinterpret_cast<AudioEngine*>(native_aaudio_track_pointer);
//    if (!AE->metronomeMode.load()) AE->mIsLooping.store(value);
}