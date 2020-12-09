//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PIANOROLL_H
#define AAUDIOTRACK_PIANOROLL_H

#include "TempoGrid.h"
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

    std::vector<std::pair<uint64_t, uint64_t>> noteData;

    void setNoteData(std::vector<uint64_t> noteData) {
        uint64_t frame = 0;
        for(uint64_t & data : noteData) {
            this->noteData.push_back({frame, data});
            frame += grid.samples_per_note;
        }
    }

    uint64_t wrap(uint64_t frame, uint64_t min, uint64_t max) {
        return min + ((frame-min) % (max - min + 1));
    }

    bool hasNote(uint64_t frame) {
        if (noteData.empty()) return false;

        uint64_t wrappedFrame = wrap(frame, 0, grid.samples_per_note * grid.notes_per_bar);
        for (auto & pair : noteData) {
            if (pair.second == 1) {
                auto &sampleToPlayNoteOn = pair.first;
                if (wrappedFrame == sampleToPlayNoteOn) {
                    return true;
                }
            }
        }
        return false;
    }
};

#endif //AAUDIOTRACK_PIANOROLL_H
