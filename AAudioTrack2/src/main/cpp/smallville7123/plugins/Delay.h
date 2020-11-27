//
// Created by matthew good on 26/11/20.
//

#ifndef AAUDIOTRACK_DELAY_H
#define AAUDIOTRACK_DELAY_H

#include <cstdint>
#include "../../ardour/Backends/PortUtils2.h"
#include "DelayLine.h"

class Delay {
public:

    DelayLine left;
    DelayLine right;
//    Delay() {
//        left.setdelay(5);
//    }
    /**
     * return true if we still have data to write, otherwise false
     */
    bool write(PortUtils2 * in, PortUtils2 * out, unsigned int samples) {
        //
        // called as delay.write(mixerPortA, tmpPort);
        //
        // the value of samples is dependant on the audio engine, but is always equal to the samples
        // remaining in the audio block
        // it is one of two values:
        //
        // value 1: mixerPortA->ports.samples - i
        // value 2: mixerPortA->ports.samples
        //

        for (int i = 0; i < out->ports.samples; i += 2) {
            reinterpret_cast<int16_t *>(out->ports.outputStereo->l->buf)[i] = right.delayline(reinterpret_cast<int16_t *>(in->ports.outputStereo->l->buf)[i]);
            reinterpret_cast<int16_t *>(out->ports.outputStereo->r->buf)[i] = left.delayline(reinterpret_cast<int16_t *>(in->ports.outputStereo->r->buf)[i]);
        }
        return true;
    }
};

#endif //AAUDIOTRACK_DELAY_H
