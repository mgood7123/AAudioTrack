//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_TRACK_H
#define AAUDIOTRACK_TRACK_H


#include "PianoRoll.h"

class Track {
public:
    PianoRoll pianoRoll;
    PatternList * patternListReference = nullptr;

    bool hasNote(uint64_t frame) {
        return false;
    }
};


#endif //AAUDIOTRACK_TRACK_H
