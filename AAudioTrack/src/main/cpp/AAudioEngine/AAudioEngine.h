//
// Created by matthew good on 18/11/20.
//

#ifndef OBOETRACK_AAUDIOENGINE_H
#define OBOETRACK_AAUDIOENGINE_H

#include <aaudio/AAudio.h>
#include <atomic>

class AAudioEngine {
public:
    AAudioStreamBuilder *builder;
    AAudioStream *stream;
    int sampleRate = 48000;
    int BufferCapacityInSamples = 192;
    int channelCount = 2;

    int32_t underrunCount = 0;
    int32_t previousUnderrunCount = 0;
    int32_t framesPerBurst = 0;
    int32_t bufferSize = 0;
    int32_t bufferCapacity = 0;

    void * audioData = nullptr;
    size_t audioDataSize = -1;

    uint64_t mReadSampleIndex = 0;
    uint64_t mTotalSamples = 0;
    std::atomic<bool> mIsPlaying { false };
    std::atomic<bool> mIsLooping { true };

    void renderAudio(int16_t *audioData, int32_t numSamples);

    bool hasData();

    AAudioEngine();
    ~AAudioEngine();

    static aaudio_data_callback_result_t onAudioReady(
            AAudioStream *stream, void *userData, void *audioData, int32_t numSamples
    );

    static void onError(
            AAudioStream *stream, void *userData, aaudio_result_t error
    );

    aaudio_result_t waitForState(aaudio_stream_state_t streamState);

    aaudio_result_t CreateStream();

    void RestartStreamNonBlocking();
    void RestartStreamBlocking();

    aaudio_result_t StartStreamNonBlocking();
    aaudio_result_t StartStreamBlocking();

    aaudio_result_t PauseStreamNonBlocking();
    aaudio_result_t PauseStreamBlocking();

    aaudio_result_t StopStreamNonBlocking();
    aaudio_result_t StopStreamBlocking();

    aaudio_result_t FlushStreamNonBlocking();
    aaudio_result_t FlushStreamBlocking();

    void load(const char *string);
};

#endif //OBOETRACK_AAUDIOENGINE_H
