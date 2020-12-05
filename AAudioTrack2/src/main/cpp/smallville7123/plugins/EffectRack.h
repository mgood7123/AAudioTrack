//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_EFFECTRACK_H
#define AAUDIOTRACK_EFFECTRACK_H

#include "../../ardour/Backends/PortUtils2.h"
#include "Mixer.h"
#include "Sampler.h"
#include "Delay.h"
#include "../HostInfo.h"
#include "../Rack.h"
#include "../Channel_Effect.h"

class EffectRack : public Plugin_Base {
public:

    Rack<Channel_Effect> rack;

    Channel_Effect *newChannel() {
        return rack.newType();
    }

    Channel_Effect * newDelayChannel() {
        Channel_Effect * channel = rack.newType();
        channel->plugin = new Delay();
        channel->plugin_is_allocated = true;
        return channel;
    }

    void removeChannel(Channel_Effect * channel) {
        rack.removeType(channel);
    }

public:
    bool requires_sample_count() override {
        return true;
    }

    bool tmp;

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        tmp = PLUGIN_STOP;
        for(auto effect : rack.typeList) {
            if (effect->plugin == nullptr) continue;
            PortUtils2 * tmpPort = new PortUtils2();
            tmpPort->allocatePorts<ENGINE_FORMAT>(out->ports.samples, out->ports.channelCount);
            tmpPort->fillPortBuffer<ENGINE_FORMAT>(0);
            effect->plugin->is_writing = effect->plugin->write(hostInfo, out, mixer, tmpPort, samples);
            out->copyFromPortToPort<ENGINE_FORMAT>(*tmpPort);
            tmpPort->deallocatePorts<ENGINE_FORMAT>(out->ports.channelCount);
            delete tmpPort;
            if (effect->plugin->is_writing == PLUGIN_CONTINUE) tmp = PLUGIN_CONTINUE;
        }
        return tmp;
    }
};

#endif //AAUDIOTRACK_EFFECTRACK_H
