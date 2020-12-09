//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PATTERN_H
#define AAUDIOTRACK_PATTERN_H


#include "PianoRoll.h"
#include "Rack.h"

class Pattern {
public:
    PianoRoll pianoRoll;

    bool hasNote(uint64_t frame) {
        return pianoRoll.hasNote(frame);
    }
};


#endif //AAUDIOTRACK_PATTERN_H
