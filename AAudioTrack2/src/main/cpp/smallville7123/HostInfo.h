//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_HOSTINFO_H
#define AAUDIOTRACK_HOSTINFO_H

#include "TempoGrid.h"
#include "../midifile/include/MidiEventList.h"
#include "../ringbuffer/ringbuffer.hpp"
#include <cstdint>

class HostInfo {

public:
    TempoGrid tempoGrid = TempoGrid(120);
    uint64_t engineFrame = 0;
    void * channelRack;
    void * patternGroup;
    void * midiFile;
    smf::MidiEventList midiEventList;

    jnk0le::Ringbuffer<smf::MidiEvent, 1048576> midiInputBuffer;

    typedef jnk0le::Ringbuffer<smf::MidiEvent, 1048576> PianoRollRingBuffer;

    template <typename T> T wrap(T frame, T min, T max) {
        return min + ((frame-min) % (max - min));
    }

    void fillMidiEvents(TempoGrid & grid, PianoRollRingBuffer & noteData, unsigned int samples) {
        midiInputBuffer.consumerClear();
        if (noteData.isEmpty()) return;
        int size = noteData.readAvailable();
        uint64_t wrappedFrame = wrap<uint64_t>(engineFrame, 0, grid.samples_per_bar);
        auto endSample = (wrappedFrame + samples);
        for (int i = 0; i < size; ++i) {
            smf::MidiEvent * midiEvent = noteData.at(i);
            if (midiEvent != nullptr) {
                if (midiEvent->tick >= wrappedFrame) {
                    if (midiEvent->tick < endSample) {
                        midiInputBuffer.insert(midiEvent);
                    } else break;
                }
            }
        }
    }
};

#endif //AAUDIOTRACK_HOSTINFO_H
