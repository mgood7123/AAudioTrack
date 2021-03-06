//
// Created by matthew good on 26/11/20.
//

#ifndef AAUDIOTRACK_MIXER_H
#define AAUDIOTRACK_MIXER_H

// sum each audio stream together
// mix 3 streams into output
// out[i] = stream[0][i] + stream[1][i] + stream[2][i]
//
// if stream 0 + stream 1 would result in an overflow,
// and the mixer still has stream 2 to add, it can either
// clip and omit stream 2,
// or apply a gain to both streams 0 and 1 and then add stream 2

#include <cstdint>
#include <vector>
#include <AndroidDAW_SDK/plugin/PortUtils2.h>
#include <AndroidDAW_SDK/plugin/Plugin.h>

using namespace AndroidDAW_SDK__Plugin_TYPEDEFS;

class Mixer : public Plugin {
public:
    int plugin_type() override {
        return PLUGIN_TYPE_MIXER;
    }

    int write(HostInfo *hostInfo, PortUtils2 *unused, Plugin *mixer, PortUtils2 *out,
              unsigned int samples) override {
        // a mixer will have no direct input port, and instead manage its own input ports
        // a silence port will always be given
        if (ports.empty() || ports.size() == 1) {
            out->fillPortBuffer<ENGINE_FORMAT>(0);
            return PLUGIN_STOP;
        }

        for (int i = 0; i < samples; i++) {
            // initialize with silence
            ENGINE_FORMAT sumLeft = 0;
            ENGINE_FORMAT sumRight = 0;
            // sum each input port in the mixer
            for (PortUtils2 * portUtils2 : ports) {
                if (portUtils2->allocated) {
                    bool overflowed = false;
                    bool underflowed = false;
                    ENGINE_FORMAT left = static_cast<ENGINE_FORMAT *>(portUtils2->ports.outputStereo->l->buf)[i];
                    sumLeft = PLUGIN_HELPERS_add<ENGINE_FORMAT>(ENGINE_FORMAT_MIN,
                                                                ENGINE_FORMAT_MAX, sumLeft, left,
                                                                overflowed, underflowed);
                    ENGINE_FORMAT right = static_cast<ENGINE_FORMAT *>(portUtils2->ports.outputStereo->r->buf)[i];
                    sumRight = PLUGIN_HELPERS_add<ENGINE_FORMAT>(ENGINE_FORMAT_MIN,
                                                                 ENGINE_FORMAT_MAX, sumRight, right,
                                                                 overflowed, underflowed);
                    if (overflowed || underflowed) break;
                } else {
                    LOGE("cannot mix a deallocated port, skipping");
                }
            }
            // set the output buffer index to the result of the summed audio
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->l->buf)[i] = sumLeft;
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->r->buf)[i] = sumRight;
        }
        return PLUGIN_STOP;
    }
};

#endif //AAUDIOTRACK_MIXER_H
