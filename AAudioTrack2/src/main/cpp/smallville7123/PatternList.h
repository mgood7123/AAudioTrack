//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PATTERNLIST_H
#define AAUDIOTRACK_PATTERNLIST_H


#include "Pattern.h"

class PatternList {
public:
    Rack<Pattern> rack;

    Pattern *newPattern() {
        return rack.newType();
    }

    void removePattern(Pattern * pattern) {
        rack.removeType(pattern);
    }

    static PatternList * cast(void * pointer) {
        return static_cast<PatternList *>(pointer);
    }
};


#endif //AAUDIOTRACK_PATTERNLIST_H
