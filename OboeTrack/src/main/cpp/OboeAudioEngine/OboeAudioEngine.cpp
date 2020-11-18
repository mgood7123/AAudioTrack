//
// Created by matthew good on 18/11/20.
//

#include "OboeAudioEngine.h"
#include "OboeMixer.h"
#include <thread>
#include <fcntl.h>

extern OboeAudioEngine OboeAudioEngine;

aaudio_data_callback_result_t OboeAudioEngine::onAudioReady(
        AAudioStream *stream, void *userData, void *audioData, int32_t numFrames
) {
    OboeAudioEngine * AE = static_cast<OboeAudioEngine *>(userData);

    // Zero out the incoming container array
    for (int j = 0; j < numFrames * AE->channelCount; ++j) {
        static_cast<int16_t *>(audioData)[j] = 0;
    }

//    LOGW("AUDIO REQUESTED");

    if (AE->hasData()) AE->renderAudio(static_cast<int16_t *>(audioData), numFrames);


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

void OboeAudioEngine::onError(
        AAudioStream *stream, void *userData, aaudio_result_t error
){
    if (error == AAUDIO_ERROR_DISCONNECTED){
        std::function<void(void)> restartFunction = std::bind(&OboeAudioEngine::RestartStream,
                                                              static_cast<OboeAudioEngine *>(userData));
        new std::thread(restartFunction);
    }
}

void OboeAudioEngine::RestartStream(){
    StopStream();
    FlushStream();
    CreateStream();
}

aaudio_result_t OboeAudioEngine::StartStream() {
    aaudio_result_t result =  AAudioStream_requestStart(stream);
    return result;
}

aaudio_result_t OboeAudioEngine::PauseStream() {
    aaudio_result_t result = AAudioStream_requestPause(stream);
    return result;
}

aaudio_result_t OboeAudioEngine::StopStream() {
    aaudio_result_t result = AAudioStream_requestStop(stream);
    return result;
}

aaudio_result_t OboeAudioEngine::FlushStream() {
    aaudio_result_t result = AAudioStream_requestFlush(stream);
    AAudioStreamBuilder_delete(builder);
    return result;
}

aaudio_result_t
OboeAudioEngine::ChangeState(aaudio_stream_state_t inputState, aaudio_stream_state_t nextState) {
    int64_t timeoutNanos = 100;
    aaudio_result_t result = AAudioStream_waitForStateChange(stream, inputState, &nextState, timeoutNanos);
    return result;
}

OboeAudioEngine::OboeAudioEngine() {
    CreateStream();
}

OboeAudioEngine::~OboeAudioEngine() {
    Oboe_Stream_Stop();
    audioDataLock.lock();
    if (audioData != nullptr) {
        free(audioData);
        audioData = nullptr;
    }
    audioDataLock.unlock();
}

/*
 * IMPORTANT: avoid starting and stopping the `oboe::AudioStream *stream` rapidly
 * exact reason appears to due to a bug in the AAudio Legacy path for Android P (9),
*/

bool OboeAudioEngine::Oboe_Stream_Start() {
    LOGW("Oboe_Init: requesting Start");
    StartStream();
    LOGW("Oboe_Init: requested Start");
    STREAM_STARTED = true;
    return true;
}

/*
 * IMPORTANT: avoid starting and stopping the `oboe::AudioStream *stream` rapidly
 * exact reason appears to due to a bug in the AAudio Legacy path for Android P (9),
*/

bool OboeAudioEngine::Oboe_Stream_Stop() {
    LOGW("Oboe_Init: requesting Stop");
    StopStream();
    LOGW("Oboe_Init: requested Stop");
    STREAM_STARTED = false;
    return true;
}

void OboeAudioEngine::load(const char *filename) {
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
    audioDataLock.lock();
    if (audioData != nullptr) {
        free(audioData);
        audioData = nullptr;
    }
    audioData = o;
    audioDataSize = len;
    mTotalFrames = audioDataSize / (2 * channelCount);
    audioDataLock.unlock();
}

void OboeAudioEngine::renderAudio(int16_t *targetData, int32_t totalFrames) {
    audioDataLock.lock();
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
        audioDataLock.unlock();
    } else {
        audioDataLock.unlock();
        // fill with zeros to output silence
        for (int i = 0; i < totalFrames * channelCount; ++i) {
            targetData[i] = 0;
        }
    }
}

bool OboeAudioEngine::hasData() {
    return audioData != nullptr && audioDataSize != -1;
}

void OboeAudioEngine::CreateStream() {
    aaudio_result_t result = AAudio_createStreamBuilder(&builder);
    if (result != AAUDIO_OK) {
        LOGF("FAILED TO CREATE STREAM BUILDER");
        return;
    }

    AAudioStreamBuilder_setBufferCapacityInFrames(builder, BufferCapacityInFrames*2);
    AAudioStreamBuilder_setDataCallback(builder, onAudioReady, this);
    AAudioStreamBuilder_setErrorCallback(builder, onError, this);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setFormat(builder, AAUDIO_FORMAT_PCM_I16);
    result = AAudioStreamBuilder_openStream(builder, &stream);
    if (result != AAUDIO_OK) {
        LOGF("FAILED TO CREATE OPEN THE STREAM");
        return;
    }
    underrunCount = 0;
    previousUnderrunCount = 0;
    framesPerBurst = AAudioStream_getFramesPerBurst(stream);
    bufferSize = AAudioStream_getBufferSizeInFrames(stream);
    bufferCapacity = AAudioStream_getBufferCapacityInFrames(stream);
    Oboe_Stream_Start();
}
