//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PATTERN_H
#define AAUDIOTRACK_PATTERN_H


#include "PianoRoll.h"
#include "Rack.h"
#include "Channel_Generator.h"
#include <cstdint>

class Pattern {
public:
    PianoRoll pianoRoll;
    Channel_Generator * channelReference;

    bool hasNote(uint64_t frame) {
        return pianoRoll.hasNote(frame);
    }

    static Pattern * cast(void * pointer) {
        return static_cast<Pattern *>(pointer);
    }
};


#endif //AAUDIOTRACK_PATTERN_H
