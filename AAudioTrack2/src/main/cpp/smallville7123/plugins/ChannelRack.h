//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_CHANNELRACK_H
#define AAUDIOTRACK_CHANNELRACK_H

#include "../../ardour/Backends/PortUtils2.h"
#include "Mixer.h"
#include "Sampler.h"
#include "Delay.h"
#include "../HostInfo.h"

class ChannelRack {
public:
    Sampler sampler;
    bool sampler_is_writing = false;
    Delay delay;
    bool delay_is_writing = false;

    void write(HostInfo & hostInfo, PortUtils2 * in, Mixer & mixer, PortUtils2 * out) {
        PortUtils2 * mixerPortA = new PortUtils2();
        mixerPortA->allocatePorts<ENGINE_FORMAT>(out->ports.samples, out->ports.channelCount);
        mixerPortA->fillPortBuffer<ENGINE_FORMAT>(0);
        PortUtils2 * mixerPortB = new PortUtils2();
        mixerPortB->allocatePorts<ENGINE_FORMAT>(out->ports.samples, out->ports.channelCount);
        mixerPortB->fillPortBuffer<ENGINE_FORMAT>(0);
        if (sampler_is_writing) {
            sampler_is_writing = sampler.write(in, mixerPortA, mixerPortA->ports.samples);
        }
        if (delay_is_writing) {
            PortUtils2 * tmpPort = new PortUtils2();
            tmpPort->allocatePorts<ENGINE_FORMAT>(out->ports.samples, out->ports.channelCount);
            tmpPort->fillPortBuffer<ENGINE_FORMAT>(0);
            delay_is_writing = delay.write(mixerPortA, tmpPort, mixerPortA->ports.samples);
            mixerPortA->copyFromPortToPort<ENGINE_FORMAT>(*tmpPort);
            tmpPort->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
            delete tmpPort;
        }
        for (int32_t i = 0; i < out->ports.samples; i += 2) {
            // write sample every beat, 120 bpm, 4 beats per bar
            if (hostInfo.engineFrame == 0 || hostInfo.tempoGrid.sample_matches_samples_per_note(hostInfo.engineFrame)) {
                // if there are events for the current sample
                LOGE("writing audio on frame %lld for %d frames, write every %d frames",
                     hostInfo.engineFrame, sampler.mTotalFrames, hostInfo.tempoGrid.samples_per_note);
                sampler.mReadFrameIndex = 0;
                sampler.mIsPlaying = true;
                sampler.mIsLooping = false;
                sampler_is_writing = sampler.write(in, mixerPortA, mixerPortA->ports.samples - i);
                {
                    PortUtils2 * tmpPort = new PortUtils2();
                    tmpPort->allocatePorts<ENGINE_FORMAT>(out->ports.samples - 1, out->ports.channelCount);
                    tmpPort->fillPortBuffer<ENGINE_FORMAT>(0);
//                        delay.init(mixerPortA, tmpPort);
                    delay_is_writing = delay.write(mixerPortA, tmpPort, mixerPortA->ports.samples - i);
                    mixerPortA->copyFromPortToPort<ENGINE_FORMAT>(*tmpPort);
                    tmpPort->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
                    delete tmpPort;
                }
//                    delay_is_writing = delay.write(mixerPortA, mixerPortA, mixerPortA->ports.samples - i);
//                    synth_is_writing = synth.write(in, mixerPortB, mixerPortB->ports.samples - i);
            } else {
                if (!sampler_is_writing && !delay_is_writing) {// && !synth_is_writing && !delay_is_writing) {
                    // if there are no events for the current sample then output silence
                    mixerPortA->setPortBufferIndex<ENGINE_FORMAT>(i, 0);
                    mixerPortB->setPortBufferIndex<ENGINE_FORMAT>(i, 0);
                }
            }
            hostInfo.engineFrame += 2;
            // return from the audio loop
        }
        mixer.in.push_back(mixerPortA);
        mixer.in.push_back(mixerPortB);
        mixer.write(in, out);
        mixer.in.pop_back();
        mixer.in.pop_back();
        mixerPortA->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
        mixerPortB->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
        delete mixerPortA;
        delete mixerPortB;
    }
};

#endif //AAUDIOTRACK_CHANNELRACK_H
