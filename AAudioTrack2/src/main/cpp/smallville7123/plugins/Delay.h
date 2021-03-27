//
// Created by matthew good on 26/11/20.
//

#ifndef AAUDIOTRACK_DELAY_H
#define AAUDIOTRACK_DELAY_H

#include <cstdint>
// port utilities, created by me (Matthew Good), for interacting with port buffers
#include <AndroidDAW_SDK/plugin/PortUtils2.h>

// delay line implementation
#include "DelayLine.h"

// plugin interface
#include <AndroidDAW_SDK/plugin/Plugin.h>

// this particular delay implements a panned delay
// letting audio pass on the right channel, but delaying it on the left channel
// NOTE 1 R -
// NOTE 2 - L
// NOTE 3 R -
// NOTE 4 - L

class Delay : public Plugin {
public:

    int plugin_type() override {
        return PLUGIN_TYPE_EFFECT;
    }

    DelayLine left;
    DelayLine right;
    Delay() {
        left.setdelay(48000/8);
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin *mixer, PortUtils2 *out,
              unsigned int samples) override {
        for (int i = 0; i < samples; i += 2) {
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->l->buf)[i] = left.delayline(reinterpret_cast<ENGINE_FORMAT *>(in->ports.outputStereo->l->buf)[i]);
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->r->buf)[i] = right.delayline(reinterpret_cast<ENGINE_FORMAT *>(in->ports.outputStereo->r->buf)[i]);
        }
        return PLUGIN_CONTINUE;
    }
};

#endif //AAUDIOTRACK_DELAY_H
