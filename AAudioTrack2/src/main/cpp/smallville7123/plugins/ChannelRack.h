//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_CHANNELRACK_H
#define AAUDIOTRACK_CHANNELRACK_H

#include <deque>
#include "../../ardour/Backends/PortUtils2.h"
#include "Mixer.h"
#include "Sampler.h"
#include "Delay.h"
#include "../HostInfo.h"
#include "../Rack.h"
#include "../Channel.h"

class ChannelRack : Plugin_Base {
public:

    Rack<Channel> rack;

    Channel *newChannel() {
        return rack.newType();
    }

    Channel * newSamplerChannel() {
        Channel * channel = rack.newType();
        channel->plugin = Sampler();
        return channel;
    }

    void removeChannel(Channel * channel) {
        rack.removeType(channel);
    }

    Sampler sampler;
    int sampler_is_writing = PLUGIN_STOP;
    Delay delay;
    int delay_is_writing = PLUGIN_STOP;

public:
    bool requires_sample_count() override {
        return Plugin_Base::requires_sample_count();
    }

    bool requires_mixer() override {
        return true;
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        PortUtils2 * mixerPortA = new PortUtils2();
        mixerPortA->allocatePorts<ENGINE_FORMAT>(out->ports.samples, out->ports.channelCount);
        mixerPortA->fillPortBuffer<ENGINE_FORMAT>(0);
        PortUtils2 * mixerPortB = new PortUtils2();
        mixerPortB->allocatePorts<ENGINE_FORMAT>(out->ports.samples, out->ports.channelCount);
        mixerPortB->fillPortBuffer<ENGINE_FORMAT>(0);
        if (sampler_is_writing == PLUGIN_CONTINUE) {
            sampler_is_writing = sampler.write(hostInfo, in, mixer, mixerPortA, mixerPortA->ports.samples);
        }
        if (delay_is_writing == PLUGIN_CONTINUE) {
            PortUtils2 * tmpPort = new PortUtils2();
            tmpPort->allocatePorts<ENGINE_FORMAT>(out->ports.samples, out->ports.channelCount);
            tmpPort->fillPortBuffer<ENGINE_FORMAT>(0);
            delay_is_writing = delay.write(hostInfo, mixerPortA, mixer, tmpPort, mixerPortA->ports.samples);
            mixerPortA->copyFromPortToPort<ENGINE_FORMAT>(*tmpPort);
            tmpPort->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
            delete tmpPort;
        }
        for (int32_t i = 0; i < out->ports.samples; i += 2) {
            // write sample every beat, 120 bpm, 4 beats per bar
            if (hostInfo->engineFrame == 0 || hostInfo->tempoGrid.sample_matches_samples_per_note(hostInfo->engineFrame)) {
                // if there are events for the current sample
                LOGE("writing audio on frame %lld for %d frames, write every %d frames",
                     hostInfo->engineFrame, sampler.mTotalFrames, hostInfo->tempoGrid.samples_per_note);
                sampler.mReadFrameIndex = 0;
                sampler.mIsPlaying = true;
                sampler.mIsLooping = true;
                sampler_is_writing = sampler.write(hostInfo, in, mixer, mixerPortA, mixerPortA->ports.samples - i);
                {
                    PortUtils2 * tmpPort = new PortUtils2();
                    tmpPort->allocatePorts<ENGINE_FORMAT>(out->ports.samples - 1, out->ports.channelCount);
                    tmpPort->fillPortBuffer<ENGINE_FORMAT>(0);
                    delay_is_writing = delay.write(hostInfo, mixerPortA, mixer, tmpPort, mixerPortA->ports.samples - i);
                    mixerPortA->copyFromPortToPort<ENGINE_FORMAT>(*tmpPort);
                    tmpPort->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
                    delete tmpPort;
                }
            } else {
                if (sampler_is_writing == PLUGIN_STOP && delay_is_writing == PLUGIN_STOP) {// && !synth_is_writing && !delay_is_writing) {
                    // if there are no events for the current sample then output silence
                    mixerPortA->setPortBufferIndex<ENGINE_FORMAT>(i, 0);
                    mixerPortB->setPortBufferIndex<ENGINE_FORMAT>(i, 0);
                }
            }
            hostInfo->engineFrame += 2;
            // return from the audio loop
        }
        Plugin_Type_Mixer * mixer_ = reinterpret_cast<Plugin_Type_Mixer*>(mixer);
        mixer_->addPort(mixerPortA);
        mixer_->addPort(mixerPortB);
        mixer_->write(hostInfo, in, mixer, out, 0);
        mixer_->removePort(mixerPortA);
        mixer_->removePort(mixerPortB);
        mixerPortA->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
        mixerPortB->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
        delete mixerPortA;
        delete mixerPortB;
        return PLUGIN_CONTINUE;
    }
};

#endif //AAUDIOTRACK_CHANNELRACK_H
