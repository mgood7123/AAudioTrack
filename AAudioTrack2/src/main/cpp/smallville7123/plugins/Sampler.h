//
// Created by matthew good on 23/11/20.
//

#ifndef AAUDIOTRACK_SAMPLER_H
#define AAUDIOTRACK_SAMPLER_H

#include <cstdint>
#include <AndroidDAW_SDK/plugin/PortUtils2.h>
#include <AndroidDAW_SDK/plugin/Plugin.h>
#include <AndroidDAW_SDK/midifile/MidiEvent.h>
#include <AndroidDAW_SDK/midifile/MidiEventList.h>

using namespace ARDOUR_TYPEDEFS;

class Sampler : public Plugin {
public:
    int plugin_type() override {
        return PLUGIN_TYPE_GENERATOR;
    }

    int play(HostInfo *hostInfo, PortUtils2 *in, PortUtils2 *out, unsigned int samples) {
        ENGINE_FORMAT * data = reinterpret_cast<ENGINE_FORMAT *>(audioData);
        ENGINE_FORMAT * left = reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->l->buf);
        ENGINE_FORMAT * right = reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->r->buf);
        if (mIsPlaying && audioData != nullptr) {
//            LOGW("playing audio %p at sample index %d", audioData, mReadSampleIndex);
            if (mIsLooping) {
                // we may transition from not looping to looping, upon the EOF being reached
                // if this happens, reset the sample index
                if (mReadSampleIndex >= audioDataTotalSamples) mReadSampleIndex = 0;
                for (uint32_t bufIndex = 0; bufIndex < samples; bufIndex++) {
                    left[bufIndex] = data[mReadSampleIndex + 0];
                    right[bufIndex] = data[mReadSampleIndex + 1];
                    mReadSampleIndex+=2;
                    if (mReadSampleIndex >= audioDataTotalSamples) mReadSampleIndex = 0;
                }
                return PLUGIN_CONTINUE;
            } else {
                // if we are not looping then silence should be emmited when the end of the file is reached
                bool EOF_reached = mReadSampleIndex >= audioDataTotalSamples;
                if (EOF_reached) {
                    // we know that the EOF has been reached before we even start playing
                    // so just output silence with no additional checking
                    out->fillPortBuffer<ENGINE_FORMAT>(0);
                    // and return from the audio loop
                    return PLUGIN_STOP;
                } else {
                    // we know that the EOF has been not reached before we even start playing
                    // so we need to do checking to output silence when EOF has been reached
                    if (mReadSampleIndex >= audioDataTotalSamples) mReadSampleIndex = 0;
                    for (uint32_t bufIndex = 0; bufIndex < samples; bufIndex++) {
                        left[bufIndex] = data[mReadSampleIndex + 0];
                        right[bufIndex] = data[mReadSampleIndex + 1];
                        mReadSampleIndex+=2;
                        if (mReadSampleIndex >= audioDataTotalSamples) {
                            // do not reset the sample index here
                            EOF_reached = true;
                            // output the rest as silence

                            // verification
                            //
                            // if i is 5, and totalSamples is 6
                            // then we need to write 1 sample
                            //
                            // however at this point, a sample has already been written
                            // but the loop is not done, so we need to increment i by 1
                            // so we can correctly check if we still need to write a sample
                            //

                            bufIndex++;

                            // output the remaining samples as silence
                            for (; bufIndex < samples; bufIndex++) {
                                out->setPortBufferIndex(bufIndex, 0);
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
            out->fillPortBuffer<ENGINE_FORMAT>(0);
            return PLUGIN_STOP;
        }
    }

    int p = PLUGIN_STOP;

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin *mixer, PortUtils2 *out, unsigned int samples) override {
        size_t size = hostInfo->midiInputBuffer.readAvailable();
        if (p == PLUGIN_CONTINUE) {
            p = play(hostInfo, in, out, samples);
        }
        for (int i = 0; i < size; ++i) {
            smf::MidiEvent *midiEvent = hostInfo->midiInputBuffer.at(i);
//            LOGE("size = %zu, midiEvent->tick = %d, midiEvent->play = %s", size, midiEvent->tick, midiEvent->isNoteOn() ? "playing" : "paused");
            if (midiEvent->isNoteOn()) {
                stopPlayback();
                startPlayback();
                p = play(hostInfo, in, out, samples);
            } else if (midiEvent->isNoteOff()) {
                stopPlayback();
                p = PLUGIN_STOP;
            }
        }
        return 0;
    }
};

#endif //AAUDIOTRACK_SAMPLER_H
