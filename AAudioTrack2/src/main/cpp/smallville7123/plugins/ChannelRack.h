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
#include "../Rack.h"
#include "../Channel_Generator.h"

class ChannelRack : Plugin_Base {
public:

    Rack<Channel_Generator> rack;

    Channel_Generator *newChannel() {
        return rack.newType();
    }

    Channel_Generator * newSamplerChannel(const char * filename, int channelCount) {
        Channel_Generator * channel = rack.newType();
        channel->plugin = new Sampler();
        // this will call the overloaded load if it exists
        channel->plugin->load(filename, channelCount);
        return channel;
    }

    void removeChannel(Channel_Generator * channel) {
        rack.removeType(channel);
    }

public:
    bool requires_sample_count() override {
        return true;
    }

    bool requires_mixer() override {
        return true;
    }

    PortUtils2 * silencePort = new PortUtils2();

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        silencePort->allocatePorts<ENGINE_FORMAT>(out->ports.samples, out->ports.channelCount);
        silencePort->fillPortBuffer<ENGINE_FORMAT>(0);
        for(auto channel : rack.typeList) {
            channel->out->allocatePorts<ENGINE_FORMAT>(out->ports.samples,
                                                     out->ports.channelCount);
            channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
            if (channel->plugin != nullptr) {
                if (channel->plugin->is_writing == PLUGIN_CONTINUE) {
                    channel->plugin->is_writing = channel->plugin->write(hostInfo, in, mixer,
                                                                         channel->out,
                                                                         channel->out->ports.samples);
                }
                if (channel->effectRack != nullptr) {
                    if (channel->effectRack->is_writing == PLUGIN_CONTINUE) {
                        channel->effectRack->is_writing = channel->effectRack->write(hostInfo, in,
                                                                                     mixer,
                                                                                     channel->out,
                                                                                     channel->out->ports.samples);
                    }
                }
            }
        }
        for (int32_t i = 0; i < out->ports.samples; i += 2) {
            // write sample every beat, 120 bpm, 4 beats per bar
            if (hostInfo->engineFrame == 0 || hostInfo->tempoGrid.sample_matches_samples_per_note(hostInfo->engineFrame)) {
                // if there are events for the current sample
                for(auto channel : rack.typeList) {
                    channel->out->allocatePorts<ENGINE_FORMAT>(out->ports.samples,
                                                             out->ports.channelCount);
                    channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                    if (channel->plugin != nullptr) {
                        channel->plugin->stopPlayback();
                        channel->plugin->is_writing = channel->plugin->write(hostInfo, in, mixer,
                                                                             channel->out,
                                                                             channel->out->ports.samples);
                        if (channel->effectRack != nullptr) {
                            channel->effectRack->is_writing = channel->effectRack->write(hostInfo,
                                                                                         in, mixer,
                                                                                         channel->out,
                                                                                         channel->out->ports.samples);
                        }
                    }
                }
            }
            hostInfo->engineFrame += 2;
            // return from the audio loop
        }
        Plugin_Type_Mixer * mixer_ = reinterpret_cast<Plugin_Type_Mixer*>(mixer);
        mixer_->addPort(silencePort);
        for(auto channel : rack.typeList) mixer_->addPort(channel->out);
        mixer_->write(hostInfo, in, mixer, out, out->ports.samples);
        mixer_->removePort(silencePort);
        silencePort->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
        for(auto channel : rack.typeList) {
            mixer_->removePort(channel->out);
            channel->out->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
        }
        return PLUGIN_CONTINUE;
    }
};

#endif //AAUDIOTRACK_CHANNELRACK_H
