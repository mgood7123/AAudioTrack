//
// Created by matthew good on 26/11/20.
//

#ifndef AAUDIOTRACK_DELAY_H
#define AAUDIOTRACK_DELAY_H

#include <cstdint>
#include "../../ardour/Backends/PortUtils2.h"
#include "DelayLine.h"
#include "../Plugin.h"

using namespace ARDOUR_TYPEDEFS;

class Delay : Plugin_Type_Effect {
public:

    DelayLine left;
    DelayLine right;
    Delay() {
        left.setdelay(48000/4);
    }

public:

    bool requires_sample_count() override {
        return Plugin_Type_Effect::requires_sample_count();
    }

    bool requires_mixer() override {
        return Plugin_Type_Effect::requires_mixer();
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        for (int i = 0; i < out->ports.samples; i += 2) {
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->l->buf)[i] = left.delayline(reinterpret_cast<ENGINE_FORMAT *>(in->ports.outputStereo->l->buf)[i]);
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->r->buf)[i] = right.delayline(reinterpret_cast<ENGINE_FORMAT *>(in->ports.outputStereo->r->buf)[i]);
        }
        return PLUGIN_CONTINUE;
    }
};

#endif //AAUDIOTRACK_DELAY_H
