//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PIANOROLL_H
#define AAUDIOTRACK_PIANOROLL_H

#include "TempoGrid.h"
#include "../ringbuffer/ringbuffer.hpp"
#include "../midifile/include/MidiEvent.h"
#include <cstdarg>
#include <vector>

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

    // lock free, wait free ring buffer, 20 to the power of 2
    // frame index, should this note be played
    HostInfo::PianoRollRingBuffer noteData;

    int noteindex = -1;

    //what would happen if two notes sit side by side?
    //as a frame CANNOT occupy both an ON and an OFF for the same note data
    //
    //i would assume that either:
    //
    //we ignore this, merging both notes into a single note with some kind of RESET/MIDI_PANIC marker located at where the second note begins
    //
    //or, that we put an OFF note exactly 1 frame before the ON note's frame (seems more realistic given MIDI, since a MIDI PANIC would, i think, stop all audio for the target device untill it recieves an ON note again (or is told to process data))

    void setNoteData(bool * noteData, int size) {
        uint64_t sample = 0;
        this->noteData.consumerClear();
        for (int i = 0; i < size; ++i) {
            smf::MidiEvent midiEvent;
            if (noteData[i]) {
                midiEvent.makeNoteOn(0, 0, 64);
                midiEvent.tick = sample;
                this->noteData.insert(midiEvent);
                sample += grid.samples_per_note;
//                smf::MidiEvent midiEventEnd;
//                midiEventEnd.makeNoteOff(0, 0, 0);
//                midiEventEnd.tick = sample-1;
//                this->noteData.insert(midiEventEnd);
            } else {
                midiEvent.makeNoteOff(0, 0, 0);
                midiEvent.tick = sample;
                this->noteData.insert(midiEvent);
                sample += grid.samples_per_note;
            }
        }
    }

    void setNoteData(std::vector<int> noteData) {
        uint64_t frame = 0;
        this->noteData.consumerClear();
        for(int & data : noteData) {
            smf::MidiEvent midiEvent;
            if (data == 1) midiEvent.makeNoteOn(0, 0, 64);
            else midiEvent.makeNoteOff(0, 0, 0);
            midiEvent.tick = frame;
            this->noteData.insert(midiEvent);
            frame += grid.samples_per_note;
        }
    }

};

#endif //AAUDIOTRACK_PIANOROLL_H
