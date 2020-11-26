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
#include "../ardour/Backends/PortUtils2.h"

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
            out->fillPortBuffer<int16_t>(0);
            return false;
        }
        for (int i = 0; i < out->ports.samples; i += 2) {
            // initialize with silence
            int16_t sumLeft = 0;
            int16_t sumRight = 0;
            // sum each input port in the mixer
            for (PortUtils2 * portUtils2 : in) {
                bool overflowed;
                sumLeft = add<int16_t>(INT16_MIN, INT16_MAX, sumLeft, reinterpret_cast<int16_t *>(portUtils2->ports.outputStereo->l->buf)[i], overflowed);
                sumRight = add<int16_t>(INT16_MIN, INT16_MAX, sumRight, reinterpret_cast<int16_t *>(portUtils2->ports.outputStereo->r->buf)[i], overflowed);
                if (overflowed) break;
            }
            // set the output buffer index to the result of the summed audio
            reinterpret_cast<int16_t *>(out->ports.outputStereo->l->buf)[i] = sumLeft;
            reinterpret_cast<int16_t *>(out->ports.outputStereo->r->buf)[i] = sumRight;
        }
        return false;
    }
};

#endif //AAUDIOTRACK_MIXER_H
