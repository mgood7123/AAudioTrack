//
// Created by matthew good on 26/11/20.
//

#ifndef AAUDIOTRACK_DELAY_H
#define AAUDIOTRACK_DELAY_H

#include <cstdint>
#include "../../ardour/Backends/PortUtils2.h"
#include "DelayLine.h"

using namespace ARDOUR_TYPEDEFS;

class Delay {
public:

    DelayLine left;
    DelayLine right;
    Delay() {
        left.setdelay(48000/4);
    }
    /**
     * return true if we still have data to write, otherwise false
     */
    bool write(PortUtils2 * in, PortUtils2 * out, unsigned int samples) {
        for (int i = 0; i < out->ports.samples; i += 2) {
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->l->buf)[i] = left.delayline(reinterpret_cast<ENGINE_FORMAT *>(in->ports.outputStereo->l->buf)[i]);
            reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->r->buf)[i] = right.delayline(reinterpret_cast<ENGINE_FORMAT *>(in->ports.outputStereo->r->buf)[i]);
        }
        return true;
    }
};

#endif //AAUDIOTRACK_DELAY_H
