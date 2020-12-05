//
// Created by matthew good on 23/11/20.
//

#ifndef AAUDIOTRACK_SAMPLER_H
#define AAUDIOTRACK_SAMPLER_H

#include <cstdint>
#include "../../ardour/Backends/PortUtils2.h"
#include "../Plugin.h"

using namespace ARDOUR_TYPEDEFS;

class Sampler : public Plugin_Type_Generator {
public:
    bool mIsPlaying = true;
    bool mIsLooping = true;

    int mReadFrameIndex = 0;

    bool requires_sample_count() override {
        return true;
    }

    void stopPlayback() override {
        mReadFrameIndex = 0;
        mIsPlaying = true;
        mIsLooping = false;
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        if (mIsPlaying && audioData != nullptr) {
            if (mIsLooping) {
                // we may transition from not looping to looping, upon the EOF being reached
                // if this happens, reset the frame index
                if (mReadFrameIndex == audioDataTotalFrames) mReadFrameIndex = 0;
                for (int32_t i = 0; i < samples; i += 2) {
                    out->setPortBufferIndex<ENGINE_FORMAT>(i, reinterpret_cast<ENGINE_FORMAT*>(audioData)[mReadFrameIndex]);

                    // Increment and handle wrap-around
                    mReadFrameIndex += 2;
                    if (mReadFrameIndex >= audioDataTotalFrames) mReadFrameIndex = 0;
                }
                return PLUGIN_CONTINUE;
            } else {
                // if we are not looping then silence should be emmited when the end of the file is reached
                bool EOF_reached = mReadFrameIndex >= audioDataTotalFrames;
                if (EOF_reached) {
                    // we know that the EOF has been reached before we even start playing
                    // so just output silence with no additional checking
                    out->fillPortBuffer<ENGINE_FORMAT>(0, samples);
                    // and return from the audio loop
                    return PLUGIN_STOP;
                } else {
                    // we know that the EOF has been not reached before we even start playing
                    // so we need to do checking to output silence when EOF has been reached
                    for (int32_t i = 0; i < samples; i += 2) {
                        out->setPortBufferIndex<ENGINE_FORMAT>(i, reinterpret_cast<ENGINE_FORMAT*>(audioData)[mReadFrameIndex]);

                        // Increment and handle wrap-around
                        mReadFrameIndex += 2;
                        if (mReadFrameIndex >= audioDataTotalFrames) {
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
                                out->setPortBufferIndex(i, 0);
                            }
                            // and return from the audio loop
                            mIsPlaying = false;
                            return PLUGIN_STOP;
                        }
                    }
                    return PLUGIN_CONTINUE;
                }
            }
        } else {
            out->fillPortBuffer(0, samples);
            return PLUGIN_STOP;
        }
    }
};

#endif //AAUDIOTRACK_SAMPLER_H
