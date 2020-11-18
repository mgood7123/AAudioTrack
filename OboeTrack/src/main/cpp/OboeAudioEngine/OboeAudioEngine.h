//
// Created by matthew good on 18/11/20.
//

#ifndef OBOETRACK_OBOEAUDIOENGINE_H
#define OBOETRACK_OBOEAUDIOENGINE_H

#include <oboe/Oboe.h>
#include <aaudio/AAudio.h>
#include "../oboe/src/common/OboeDebug.h"

using namespace oboe;

class OboeAudioEngine {
public:
    AAudioStreamBuilder *builder;
    AAudioStream *stream;
    int sampleRate = 48000;
    int BufferCapacityInFrames = 192;
    int channelCount = 2;

    int32_t underrunCount = 0;
    int32_t previousUnderrunCount = 0;
    int32_t framesPerBurst = 0;
    int32_t bufferSize = 0;
    int32_t bufferCapacity = 0;

    void * audioData = nullptr;
    size_t audioDataSize = -1;
    std::mutex audioDataLock;

    uint64_t mReadFrameIndex = 0;
    uint64_t mTotalFrames = 0;
    std::atomic<bool> mIsPlaying { true };
    std::atomic<bool> mIsLooping { true };

    void renderAudio(int16_t *audioData, int32_t numFrames);


    bool hasData();

    bool STREAM_STARTED = false; // we only use a single stream for now

    OboeAudioEngine();
    ~OboeAudioEngine();

    bool Oboe_Stream_Start();

    bool Oboe_Stream_Stop();

    static aaudio_data_callback_result_t onAudioReady(
            AAudioStream *stream, void *userData, void *audioData, int32_t numFrames
    );

    static void onError(
            AAudioStream *stream, void *userData, aaudio_result_t error
    );

    void RestartStream();

    aaudio_result_t StartStream();

    aaudio_result_t PauseStream();

    aaudio_result_t StopStream();

    aaudio_result_t FlushStream();

    aaudio_result_t ChangeState(aaudio_stream_state_t inputState, aaudio_stream_state_t nextState);

    void load(const char *string);

    void CreateStream();
};

#endif //OBOETRACK_OBOEAUDIOENGINE_H
