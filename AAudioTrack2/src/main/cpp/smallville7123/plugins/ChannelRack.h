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
#include "../PianoRoll.h"
#include "../Pattern.h"
#include "../PatternList.h"

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

    PortUtils2 * silencePort = nullptr;

    PatternList patternList;
    Pattern * pattern = nullptr;

    ChannelRack() {
        silencePort = new PortUtils2();
        pattern = patternList.newPattern();
        pattern->pianoRoll.setBPM(120*2);
        pattern->pianoRoll.setResolution(16);
        pattern->pianoRoll.updateGrid();
        pattern->pianoRoll.setNoteData({
            1,1,1,0,
            1,1,1,0,
            1,1,1,1,
            1,1,1,0
        });
    }

    ~ChannelRack() {
        silencePort->deallocatePorts<ENGINE_FORMAT>();
        patternList.removePattern(pattern);
        delete silencePort;
    }

    void writePlugin(Plugin_Base * plugin, HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer,
                     PortUtils2 *out, unsigned int samples) {
        plugin->is_writing = plugin->write(hostInfo, in, mixer,
                                           out, samples);
    }

    void writeEffectRack(EffectRack * effectRack, HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer,
                         PortUtils2 *out, unsigned int samples) {
        effectRack->is_writing = effectRack->write(hostInfo, in, mixer,
                                                   out, samples);
    }

    void writeChannels(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
                       unsigned int samples) {
        // LOGE("writing channels");
        for(int i = 0; i < rack.typeList.size(); i++) {
            auto * channel = rack.typeList[i];
            channel->out->allocatePorts<ENGINE_FORMAT>(out);
            channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
            if (channel->plugin != nullptr) {
                if (channel->plugin->is_writing == PLUGIN_CONTINUE) {
                    writePlugin(channel->plugin, hostInfo, in, mixer,
                                channel->out, samples);
                }
                if (channel->effectRack != nullptr) {
                    if (channel->effectRack->is_writing == PLUGIN_CONTINUE) {
                        writeEffectRack(channel->effectRack, hostInfo, in, mixer,
                                        channel->out, samples);
                    }
                }
            }
        }
        for (int32_t i = 0; i < samples; i ++) {
            // write sample every beat, 120 bpm, 4 beats per bar
            if (pattern != nullptr) {
                if (pattern->hasNote(hostInfo->engineFrame)) {
                    for(auto channel : rack.typeList) {
                        channel->out->allocatePorts<ENGINE_FORMAT>(out);
                        channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                        if (channel->plugin != nullptr) {
                            channel->plugin->stopPlayback();
                            writePlugin(channel->plugin, hostInfo, in, mixer,
                                        channel->out, samples);
                        }
                        // an effect rack should be able to be played even without a plugin
                        // as there may be an effect which acts as both a generator and an effect
                        // for example an effect may add random data to the stream
                        // this would turn it into a generator
                        if (channel->effectRack != nullptr) {
                            writeEffectRack(channel->effectRack, hostInfo, in, mixer,
                                            channel->out, samples);
                        }
                    }
                }
            }
            hostInfo->engineFrame++;
            // return from the audio loop
        }
        // LOGE("wrote channels");
    }

    void prepareMixer(Plugin_Type_Mixer * mixer_, PortUtils2 * out) {
        // LOGE("preparing mixer");
        for(auto channel : rack.typeList) mixer_->addPort(channel->out);
        silencePort->allocatePorts<ENGINE_FORMAT>(out);
        mixer_->addPort(silencePort);
        silencePort->fillPortBuffer<ENGINE_FORMAT>(0);
        // LOGE("prepared mixer");
    }

    void mix(HostInfo *hostInfo, PortUtils2 *in, Plugin_Type_Mixer * mixer, PortUtils2 *out,
             unsigned int samples) {
        // LOGE("mixing");
        mixer->write(hostInfo, in, mixer, out, samples);
        // LOGE("mixed");
    }

    void finalizeMixer(Plugin_Type_Mixer * mixer_) {
        // LOGE("finalizing mixer");
        mixer_->removePort(silencePort);
        silencePort->deallocatePorts<ENGINE_FORMAT>();

        for(auto channel : rack.typeList) {
            mixer_->removePort(channel->out);
            channel->out->deallocatePorts<ENGINE_FORMAT>();
        }
        // LOGE("finalized mixer");
    }

    void mixChannels(HostInfo *hostInfo, PortUtils2 *in, Plugin_Type_Mixer * mixer, PortUtils2 *out,
                     unsigned int samples) {
        prepareMixer(mixer, out);
        mix(hostInfo, in, mixer, out, samples);
        finalizeMixer(mixer);
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        writeChannels(hostInfo, in, mixer, out, samples);
        mixChannels(hostInfo, in, reinterpret_cast<Plugin_Type_Mixer*>(mixer), out, samples);
        return PLUGIN_CONTINUE;
    }
};

#endif //AAUDIOTRACK_CHANNELRACK_H
