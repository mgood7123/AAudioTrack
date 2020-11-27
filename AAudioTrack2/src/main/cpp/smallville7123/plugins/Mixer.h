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
#include "../../ardour/Backends/PortUtils2.h"
using namespace ARDOUR_TYPEDEFS;

class Mixer {
public:
    std::vector<PortUtils2*> in;

    template<typename type> type add(type TYPE_MIN, type TYPE_MAX, type lhs, type rhs, bool & overflowed)
    {
        overflowed = false;
        if (lhs >= 0) {
            if (TYPE_MAX - lhs < rhs) {
                overflowed = true;
                return TYPE_MAX;
            }
        }
        else {
            if (rhs < TYPE_MIN - lhs) {
                overflowed = true;
                return TYPE_MAX;
            }
        }
        return lhs + rhs;
    }
    /**
     * return true if we still have data to write, otherwise false
     */
    bool write(PortUtils2 * unused, PortUtils2 * out, unsigned int samples = 0) {
        // a mixer will have no direct input port, and instead manage its own input ports
        if (in.empty()) {
            out->fillPortBuffer<ENGINE_FORMAT>(0);
            return false;
        }
        for (int i = 0; i < out->ports.samples; i += 2) {
            // initialize with silence
            ENGINE_FORMAT sumLeft = 0;
            ENGINE_FORMAT sumRight = 0;
            // sum each input port in the mixer
            for (PortUtils2 * portUtils2 : in) {
                bool overflowed;
                sumLeft = add<ENGINE_FORMAT>(ENGINE_FORMAT_MIN, ENGINE_FORMAT_MAX, sumLeft, reinterpret_cast<ENGINE_FORMAT *>(portUtils2->ports.outputStereo->l->buf)[i], overflowed);
                sumRight = add<ENGINE_FORMAT>(ENGINE_FORMAT_MIN, ENGINE_FORMAT_MAX, sumRight, reinterpret_cast<ENGINE_FORMAT *>(portUtils2->ports.outputStereo->r->buf)[i], overflowed);
                if (overflowed) break;
            }
            // set the output buffer index to the result of the summed audio
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->l->buf)[i] = sumLeft;
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->r->buf)[i] = sumRight;
        }
        return false;
    }
};

#endif //AAUDIOTRACK_MIXER_H
