//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PIANOROLL_H
#define AAUDIOTRACK_PIANOROLL_H

#include "TempoGrid.h"
#include "MidiMap.h"
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
        TempoGrid::map_tempo_to_sample(grid);
    }

    // lock free, wait free ring buffer, 20 to the power of 2
    // sample index, should this note be played
    HostInfo::PianoRollRingBuffer noteData;

    int noteindex = -1;

    void setNoteData(bool * noteData, int size) {
        uint64_t sample = 0;
        this->noteData.consumerClear();
        int counter = -1;
        bool x = false;
        bool on = false;
        for (int i = 0; i < size; ++i) {
            if (noteData[i]) {
                if (x) {
                    on = false;
                    smf::MidiEvent midiEvent;
                    midiEvent.tick = sample-1;
                    midiEvent.makeNoteOff(0, counter, 0);
                    this->noteData.insert(midiEvent);
                }
                on = true;
                counter++;
                smf::MidiEvent midiEvent;
                midiEvent.tick = sample;
                midiEvent.makeNoteOn(0, counter, 127);
                this->noteData.insert(midiEvent);
                if (!x) x = true;
            } else {
                on = false;
                smf::MidiEvent midiEvent;
                midiEvent.tick = sample;
                midiEvent.makeNoteOff(0, counter, 0);
                this->noteData.insert(midiEvent);
            }
            sample += grid.samples_per_note;
        }
        if (on) {
            on = false;
            smf::MidiEvent midiEvent;
            midiEvent.tick = sample;
            sample += grid.samples_per_note;
            midiEvent.makeNoteOff(0, counter, 0);
            this->noteData.insert(midiEvent);
        }
    }

    void setNoteData(std::vector<int> noteData) {
        uint64_t sample = 0;
        this->noteData.consumerClear();
        for(int & data : noteData) {
            smf::MidiEvent midiEvent;
            if (data == 1) midiEvent.makeNoteOn(0, 0, 64);
            else midiEvent.makeNoteOff(0, 0, 0);
            midiEvent.tick = sample;
            this->noteData.insert(midiEvent);
            sample += grid.samples_per_note;
        }
    }

    smf::MidiEvent * getMidiEventAt(int i) {
        if (!noteData.isEmpty()) {
            int size = noteData.readAvailable();
            if (size > i) {
                return noteData.at(i);
            }
        }
        return nullptr;
    }

    int getMidiTickAt(void * midiEvent) {
        smf::MidiEvent *midiEvent_ = reinterpret_cast<smf::MidiEvent*>(midiEvent);
        if (midiEvent_ != nullptr) return midiEvent_->tick;
        return -1;
    }

    int getMidiChannelAt(void * midiEvent) {
        smf::MidiEvent *midiEvent_ = reinterpret_cast<smf::MidiEvent*>(midiEvent);
        if (midiEvent_ != nullptr) return midiEvent_->getChannel();
        return -1;
    }

    int getMidiNumberAt(void * midiEvent) {
        smf::MidiEvent *midiEvent_ = reinterpret_cast<smf::MidiEvent*>(midiEvent);
        if (midiEvent_ != nullptr) return midiEvent_->getKeyNumber();
        return -1;
    }

    int getMidiVelocityAt(void * midiEvent) {
        smf::MidiEvent *midiEvent_ = reinterpret_cast<smf::MidiEvent*>(midiEvent);
        if (midiEvent_ != nullptr) return midiEvent_->getVelocity();
        return -1;
    }

    bool isMidiEventNote(void * midiEvent) {
        smf::MidiEvent *midiEvent_ = reinterpret_cast<smf::MidiEvent*>(midiEvent);
        if (midiEvent_ != nullptr) return midiEvent_->isNote();
        return false;
    }

    bool isMidiEventNoteOn(void * midiEvent) {
        smf::MidiEvent *midiEvent_ = reinterpret_cast<smf::MidiEvent*>(midiEvent);
        if (midiEvent_ != nullptr) return midiEvent_->isNoteOn();
        return false;
    }

    bool isMidiEventNoteOff(void * midiEvent) {
        smf::MidiEvent *midiEvent_ = reinterpret_cast<smf::MidiEvent*>(midiEvent);
        if (midiEvent_ != nullptr) return midiEvent_->isNoteOff();
        return false;
    }
};

#endif //AAUDIOTRACK_PIANOROLL_H
