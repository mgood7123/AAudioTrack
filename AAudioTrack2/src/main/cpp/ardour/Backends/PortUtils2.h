//
// Created by matthew good on 25/11/20.
//

#ifndef AAUDIOTRACK_PORTUTILS2_H
#define AAUDIOTRACK_PORTUTILS2_H


#include "../../zrythm/audio/port.h"
#include <cstdint>

class PortUtils2 {

public:

    struct Ports {
        Port *outputMono = nullptr;
        bool mono = false;
        StereoPorts *outputStereo = nullptr;
        bool stereo = false;
        Port **outputMultiChannel = nullptr;
        bool multiChannel = false;
        uint32_t channelCount = 0;
        uint32_t samples = 0;
        void * buffer;
    } ports;

    template<typename type> void allocatePorts(uint32_t samples, uint32_t channelCount) {
        ports.channelCount = channelCount;
        ports.samples = samples;
        if (ports.channelCount == 0) LOGF("invalid channel count: 0");
        ports.mono = channelCount == 1;
        if (ports.mono) {
            ports.outputMono = new Port();
        } else {
            ports.stereo = channelCount == 2;
            if (ports.stereo) {
                ports.buffer = new type[samples*2];
                ports.outputStereo = new StereoPorts();
                ports.outputStereo->l = new Port();
                ports.outputStereo->r = new Port();
                ports.outputStereo->l->buf = ports.buffer;
                ports.outputStereo->r->buf = reinterpret_cast<type*>(ports.buffer) + samples;
            } else {
                ports.multiChannel = true;
                ports.outputMultiChannel = new Port *[channelCount];
                for (int i = 0; i < channelCount; ++i) {
                    ports.outputMultiChannel[i] = new Port();
                }
            }
        }
    }

    template<typename type>
    void deallocatePorts(uint32_t channelCount) {
        ports.channelCount = 0;
        ports.samples = 0;
        if (ports.outputMono != nullptr) {
            // buf is never allocated for mono
            delete ports.outputMono;
            ports.outputMono = nullptr;
            ports.mono = false;
        }
        if (ports.outputStereo != nullptr) {
            delete ports.outputStereo->l;
            delete ports.outputStereo->r;
            delete ports.outputStereo;
            ports.outputStereo = nullptr;
            delete[] reinterpret_cast<type*>(ports.buffer);
            ports.stereo = false;
        }
        if (ports.outputMultiChannel != nullptr) {
            for (int i = 0; i < channelCount; ++i) {
                if (ports.outputMultiChannel[i]->buf != nullptr) {
                    delete[] reinterpret_cast<type *>(ports.outputMultiChannel[i]->buf);
                    ports.outputMultiChannel[i]->buf = nullptr;
                }
                delete ports.outputMultiChannel[i];
                ports.outputMultiChannel[i] = nullptr;
            }
            delete[] ports.outputMultiChannel;
            ports.outputMultiChannel = nullptr;
            ports.multiChannel = false;
        }
    }
};


#endif //AAUDIOTRACK_PORTUTILS2_H
