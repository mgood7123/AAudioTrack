//
// Created by matthew good on 23/11/20.
//

#ifndef AAUDIOTRACK_SAMPLER_H
#define AAUDIOTRACK_SAMPLER_H

#include <cstdint>

class Sampler {
public:
    int mReadFrameIndex = 0;
    bool mIsPlaying = true;
    bool mIsLooping = true;
    bool write(void *audioData, int mTotalFrames, PortUtils2 &in, PortUtils2 &out, unsigned int samples = 0) {
        if (mIsPlaying && audioData != nullptr) {
            if (mIsLooping) {
                // we may transition from not looping to looping, upon the EOF being reached
                // if this happens, reset the frame index
                if (mReadFrameIndex == mTotalFrames) mReadFrameIndex = 0;
                for (int32_t i = 0; i < samples; i += 2) {
                    in.setPortBufferIndex<int16_t>(i, reinterpret_cast<int16_t*>(audioData)[mReadFrameIndex]);

                    // Increment and handle wrap-around
                    mReadFrameIndex += 2;
                    if (mReadFrameIndex >= mTotalFrames) mReadFrameIndex = 0;
                }
                return true;
            } else {
                // if we are not looping then silence should be emmited when the end of the file is reached
                bool EOF_reached = mReadFrameIndex >= mTotalFrames;
                if (EOF_reached) {
                    // we know that the EOF has been reached before we even start playing
                    // so just output silence with no additional checking
                    in.fillPortBuffer<int16_t>(0, samples);
                    // and return from the audio loop
                    return false;
                } else {
                    // we know that the EOF has been not reached before we even start playing
                    // so we need to do checking to output silence when EOF has been reached
                    for (int32_t i = 0; i < samples; i += 2) {
                        in.setPortBufferIndex<int16_t>(i, reinterpret_cast<int16_t*>(audioData)[mReadFrameIndex]);

                        // Increment and handle wrap-around
                        mReadFrameIndex += 2;
                        if (mReadFrameIndex >= mTotalFrames) {
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

                            i += 2;

                            // output the remaining frames as silence
                            for (; i < samples; i += 2) {
                                in.setPortBufferIndex(i, 0);
                            }
                            // and return from the audio loop
                            mIsPlaying = false;
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else {
            in.fillPortBuffer(0, samples);
            return false;
        }
    }
};

#endif //AAUDIOTRACK_SAMPLER_H
