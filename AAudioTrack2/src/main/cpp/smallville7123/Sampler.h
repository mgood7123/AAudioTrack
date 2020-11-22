//
// Created by matthew good on 23/11/20.
//

#ifndef AAUDIOTRACK_SAMPLER_H
#define AAUDIOTRACK_SAMPLER_H

#include <cstdint>

class Sampler {
public:
    uint64_t mReadFrameIndex = 0;
    bool mIsPlaying = true;
    bool mIsLooping = true;
    bool write(void *audioData, uint64_t mTotalFrames, int channelCount, int16_t * targetData, int32_t number_of_frames_to_render) {
        if (mIsPlaying && audioData != nullptr) {
            int16_t * AUDIO_DATA = reinterpret_cast<int16_t *>(audioData);

            // Check whether we're about to reach the end of the recording
            if (!mIsLooping && mReadFrameIndex + number_of_frames_to_render >= mTotalFrames) {
                number_of_frames_to_render = mTotalFrames - mReadFrameIndex;
                mIsPlaying = false;
            }

            if (mReadFrameIndex == 0) {
                //            GlobalTime.StartOfFile = true;
                //            GlobalTime.update(mReadFrameIndex, AudioData);
            }

            if (mIsLooping) {
                // we may transition from not looping to looping, upon the EOF being reached
                // if this happens, reset the frame index
                if (mReadFrameIndex == mTotalFrames) mReadFrameIndex = 0;
                for (int32_t i = 0; i < number_of_frames_to_render; ++i) {
                    for (int j = 0; j < number_of_frames_to_render; ++j) {
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
                // return from the audio loop
            } else {
                // if we are not looping then silence should be emmited when the end of the file is reached
                bool EOF_reached = mReadFrameIndex == mTotalFrames;
                if (EOF_reached) {
                    // we know that the EOF has been reached before we even start playing
                    // so just output silence with no additional checking
                    for (int32_t i = 0; i < number_of_frames_to_render; ++i) {
                        for (int j = 0; j < channelCount; ++j) {
                            targetData[(i * channelCount) + j] = 0;
                        }
                    }
                    // and return from the audio loop
                    return false;
                } else {
                    // we know that the EOF has been not reached before we even start playing
                    // so we need to do checking to output silence when EOF has been reached
                    for (int32_t i = 0; i < number_of_frames_to_render; ++i) {
                        for (int j = 0; j < channelCount; ++j) {
                            targetData[(i * channelCount) + j] = EOF_reached ? 0 : AUDIO_DATA[
                                    (mReadFrameIndex * channelCount) + j];
                        }

                        // Increment and handle wrap-around
                        if (++mReadFrameIndex >= mTotalFrames) {
                            //                GlobalTime.EndOfFile = true;
                            //                GlobalTime.update(mReadFrameIndex, AudioData);

                            // do not reset the frame index here
                            EOF_reached = true;
                            // output the rest as silence

                            // verification
                            //
                            // if i is 5, and totalFrames is 6
                            // then we need to write 1 frame
                            //
                            // however at this point, a frame has already been written
                            // but the loop is not done, so we need to increment i by 1
                            // so we can correctly check if we still need to write a frame
                            //

                            i++;

                            // output the remaining frames as silence
                            for (; i < number_of_frames_to_render; ++i) {
                                for (int j = 0; j < channelCount; ++j) {
                                    targetData[(i * channelCount) + j] = 0;
                                }
                            }
                            // and return from the audio loop
                            return false;
                        } else {
                            //                        GlobalTime.update(mReadFrameIndex, AudioData);
                        }
                    }
                    return true;
                }
            }
        } else {
            for (int32_t i = 0; i < number_of_frames_to_render; ++i) {
                for (int j = 0; j < channelCount; ++j) {
                    targetData[(i * channelCount) + j] = 0;
                }
            }
            return false;
        }
    }
};

#endif //AAUDIOTRACK_SAMPLER_H
