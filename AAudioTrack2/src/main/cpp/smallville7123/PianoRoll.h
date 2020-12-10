//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PIANOROLL_H
#define AAUDIOTRACK_PIANOROLL_H

#include "TempoGrid.h"
#include "../ringbuffer/ringbuffer.hpp"
#include <cstdarg>

class PianoRoll {
public:
    TempoGrid grid = TempoGrid(120);

    void setResolution(uint64_t notes_per_bar) {
        grid.notes_per_bar = notes_per_bar;
    }


    void setBPM(uint64_t beats_per_minute) {
        grid.beats_per_minute = beats_per_minute;
    }

    void updateGrid() {
        TempoGrid::map_tempo_to_frame(grid);
    }

    // lock free, wait free ring buffer
    /* 20 to the power of 2 */
    jnk0le::Ringbuffer<std::pair<uint64_t, bool>, 1048576> noteData;

    void setNoteData(bool * noteData, int size) {
        uint64_t frame = 0;
        this->noteData.consumerClear();
        for (int i = 0; i < size; ++i) {
            this->noteData.insert({frame, noteData[i]});
            frame += grid.samples_per_note;
        }
    }

    void setNoteData(std::vector<int> noteData) {
        uint64_t frame = 0;
        this->noteData.consumerClear();
        for(int & data : noteData) {
            this->noteData.insert({frame, data == 0 ? false : true});
            frame += grid.samples_per_note;
        }
    }

    uint64_t wrap(uint64_t frame, uint64_t min, uint64_t max) {
        return min + ((frame-min) % (max - min + 1));
    }

    bool hasNote(uint64_t frame) {
        if (noteData.isEmpty()) return false;

        uint64_t wrappedFrame = wrap(frame, 0, grid.samples_per_note * grid.notes_per_bar);
        for (int i = 0; i < noteData.readAvailable(); ++i) {
            auto * pair = noteData.at(i);
            if (pair != nullptr) {
                if (pair->second == true) {
                    auto &sampleToPlayNoteOn = pair->first;
                    if (wrappedFrame == sampleToPlayNoteOn) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
};

#endif //AAUDIOTRACK_PIANOROLL_H
