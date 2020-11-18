//
// Created by matthew good on 18/11/20.
//

#include "AAudioEngine.h"
#include "AAudioMixer.h"
#include <thread>
#include <fcntl.h>

#include <android/log.h>
#include <unistd.h>

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "AAudio", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "AAudio", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AAudio", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "AAudio", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AAudio", __VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, "AAudio", __VA_ARGS__)

extern AAudioEngine OboeAudioEngine;

bool AAudioEngine::hasData() {
    return audioData != nullptr && audioDataSize != -1;
}

aaudio_data_callback_result_t AAudioEngine::onAudioReady (
        AAudioStream *stream, void *userData, void *audioData, int32_t numFrames
) {
    AAudioEngine * AE = static_cast<AAudioEngine *>(userData);

    // AE->renderAudio will internally memset if there is no data
    AE->renderAudio(static_cast<int16_t *>(audioData), numFrames);

    // Are we getting underruns?
    int32_t tmpuc = AAudioStream_getXRunCount(stream);
    if (tmpuc > AE->previousUnderrunCount) {
        AE->previousUnderrunCount = AE->underrunCount;
        AE->underrunCount = tmpuc;
        // Try increasing the buffer size by one burst
        AE->bufferSize += AE->framesPerBurst;
        AE->bufferSize = AAudioStream_setBufferSizeInFrames(stream, AE->bufferSize);
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

void AAudioEngine::renderAudio(int16_t *targetData, int32_t totalFrames) {
    if (mIsPlaying && hasData()) {
        int16_t * AUDIO_DATA = reinterpret_cast<int16_t *>(audioData);

        // Check whether we're about to reach the end of the recording
        if (!mIsLooping && mReadFrameIndex + totalFrames >= mTotalFrames) {
            totalFrames = mTotalFrames - mReadFrameIndex;
            mIsPlaying = false;
        }

        if (mReadFrameIndex == 0) {
//            GlobalTime.StartOfFile = true;
//            GlobalTime.update(mReadFrameIndex, AudioData);
        }
        for (int i = 0; i < totalFrames; ++i) {
            for (int j = 0; j < channelCount; ++j) {
                targetData[(i * channelCount) + j] = AUDIO_DATA[(mReadFrameIndex * channelCount) + j];
            }

            // Increment and handle wrap-around
            if (++mReadFrameIndex >= mTotalFrames) {
//                GlobalTime.EndOfFile = true;
//                GlobalTime.update(mReadFrameIndex, AudioData);
                mReadFrameIndex = 0;
            } else {
//                GlobalTime.update(mReadFrameIndex, AudioData);
            }
        }
    } else {
        for (int i = 0; i < totalFrames; ++i) {
            for (int j = 0; j < channelCount; ++j) {
                targetData[(i * channelCount) + j] = 0;
            }
        }
    }
}

void AAudioEngine::onError(AAudioStream *stream, void *userData, aaudio_result_t error) {
    if (error == AAUDIO_ERROR_DISCONNECTED) {
        std::function<void(void)> restartFunction = std::bind(&AAudioEngine::RestartStreamNonBlocking,
                                                              static_cast<AAudioEngine *>(userData));
        new std::thread(restartFunction);
    }
}

aaudio_result_t AAudioEngine::waitForState(aaudio_stream_state_t streamState) {
    aaudio_result_t result = AAUDIO_OK;
    aaudio_stream_state_t currentState = AAudioStream_getState(stream);
    aaudio_stream_state_t inputState = currentState;
    while (result == AAUDIO_OK && currentState != streamState) {
        result = AAudioStream_waitForStateChange( stream, inputState, &currentState, 2000);
        inputState = currentState;
    }
    return result;
}

AAudioEngine::AAudioEngine() {
    CreateStream();
    StartStreamNonBlocking();
}

AAudioEngine::~AAudioEngine() {
    StopStreamBlocking();
    if (audioData != nullptr) {
        free(audioData);
        audioData = nullptr;
    }
}

aaudio_result_t AAudioEngine::CreateStream() {
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK) {
        LOGE("FAILED TO CREATE STREAM BUILDER");
        return result;
    }

    AAudioStreamBuilder_setBufferCapacityInFrames(builder, BufferCapacityInFrames*2);
    AAudioStreamBuilder_setDataCallback(builder, onAudioReady, this);
    AAudioStreamBuilder_setErrorCallback(builder, onError, this);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_I16);
    result = AAudioStreamBuilder_openStream(builder, &stream);
    if (result != AAUDIO_OK) {
        LOGE("FAILED TO OPEN THE STREAM");
        return result;
    }
    underrunCount = 0;
    previousUnderrunCount = 0;
    framesPerBurst = AAudioStream_getFramesPerBurst(stream);
    bufferSize = AAudioStream_getBufferSizeInFrames(stream);
    bufferCapacity = AAudioStream_getBufferCapacityInFrames(stream);
    return AAUDIO_OK;
}

void AAudioEngine::RestartStreamNonBlocking() {
    StopStreamNonBlocking();
    FlushStreamNonBlocking();
    CreateStream();
    StartStreamNonBlocking();
}

void AAudioEngine::RestartStreamBlocking() {
    StopStreamBlocking();
    FlushStreamBlocking();
    CreateStream();
    StartStreamBlocking();
}

aaudio_result_t AAudioEngine::StartStreamNonBlocking() {
    aaudio_result_t result =  AAudioStream_requestStart(stream);
    if (result != AAUDIO_OK) LOGE("FAILED TO START THE STREAM");
    return result;
}

aaudio_result_t AAudioEngine::StartStreamBlocking() {
    aaudio_result_t result =  AAudioStream_requestStart(stream);
    if (result != AAUDIO_OK) LOGE("FAILED TO START THE STREAM");
    else {
        result = waitForState(AAUDIO_STREAM_STATE_STARTED);
        if (result != AAUDIO_OK) LOGE("FAILED TO WAIT FOR STATE CHANGE");
    }
    return result;
}

aaudio_result_t AAudioEngine::PauseStreamNonBlocking() {
    aaudio_result_t result = AAudioStream_requestPause(stream);
    if (result != AAUDIO_OK) LOGE("FAILED TO PAUSE THE STREAM");
    return result;
}

aaudio_result_t AAudioEngine::PauseStreamBlocking() {
    aaudio_result_t result = AAudioStream_requestPause(stream);
    if (result != AAUDIO_OK) LOGE("FAILED TO PAUSE THE STREAM");
    else {
        result = waitForState(AAUDIO_STREAM_STATE_PAUSED);
        if (result != AAUDIO_OK) LOGE("FAILED TO WAIT FOR STATE CHANGE");
    }
    return result;
}

aaudio_result_t AAudioEngine::StopStreamNonBlocking() {
    aaudio_result_t result = AAudioStream_requestStop(stream);
    if (result != AAUDIO_OK) LOGE("FAILED TO STOP THE STREAM");
    return result;
}

aaudio_result_t AAudioEngine::StopStreamBlocking() {
    aaudio_result_t result = AAudioStream_requestStop(stream);
    if (result != AAUDIO_OK) LOGE("FAILED TO STOP THE STREAM");
    else {
        result = waitForState(AAUDIO_STREAM_STATE_STOPPED);
        if (result != AAUDIO_OK) LOGE("FAILED TO WAIT FOR STATE CHANGE");
    }
    return result;
}

aaudio_result_t AAudioEngine::FlushStreamNonBlocking() {
    aaudio_result_t result = AAudioStream_requestFlush(stream);
    if (result != AAUDIO_OK) LOGE("FAILED TO FLUSH THE STREAM");
    return result;
}

aaudio_result_t AAudioEngine::FlushStreamBlocking() {
    aaudio_result_t result = AAudioStream_requestFlush(stream);
    if (result != AAUDIO_OK) LOGE("FAILED TO FLUSH THE STREAM");
    else {
        result = waitForState(AAUDIO_STREAM_STATE_FLUSHED);
        if (result != AAUDIO_OK) LOGE("FAILED TO WAIT FOR STATE CHANGE");
    }
    return result;
}

void AAudioEngine::load(const char *filename) {
    int fd;
    size_t len = 0;
    char *o = NULL;
    fd = open(filename, O_RDONLY);
    if (!fd) {
        LOGF("open() failure");
        return;
    }
    len = (size_t) lseek(fd, 0, SEEK_END);
    lseek(fd, 0, 0);
    if (!(o = (char *) malloc(len))) {
        int cl = close(fd);
        if (cl < 0) {
            LOGE("cannot close \"%s\", returned %d\n", filename, cl);
        }
        LOGF("failure to malloc()");
        return;
    }
    if ((read(fd, o, len)) == -1) {
        int cl = close(fd);
        if (cl < 0) {
            LOGE("cannot close \"%s\", returned %d\n", filename, cl);
        }
        LOGF("failure to read()");
        return;
    }
    int cl = close(fd);
    if (cl < 0) {
        LOGF("cannot close \"%s\", returned %d\n", filename, cl);
        return;
    }

    // file has been read into memory
    aaudio_stream_state_t currentState = AAudioStream_getState(stream);
    bool wasPaused = false;
    if (currentState == AAUDIO_STREAM_STATE_STARTING) {
        wasPaused = true;
        if (waitForState(AAUDIO_STREAM_STATE_STARTED) != AAUDIO_OK)
            LOGE("FAILED TO WAIT FOR STATE CHANGE");
        PauseStreamBlocking();
    } else if (currentState == AAUDIO_STREAM_STATE_STARTED) {
        wasPaused = true;
        PauseStreamBlocking();
    }
    if (audioData != nullptr) {
        free(audioData);
        audioData = nullptr;
    }
    audioData = o;
    audioDataSize = len;
    mTotalFrames = audioDataSize / (2 * channelCount);
    if (wasPaused) {
        StartStreamNonBlocking();
    }
}
