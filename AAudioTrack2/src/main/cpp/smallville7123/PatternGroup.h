//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PATTERNGROUP_H
#define AAUDIOTRACK_PATTERNGROUP_H


#include "PatternList.h"

// Pattern Groups:
//  pattern group 1
//   pattern 1
//    channel 1
//   pattern 2
//    channel 2
//  pattern group 2
class PatternGroup {
public:
    Rack<PatternList> rack;

    PatternList *newPatternList() {
        return rack.newType();
    }

    void removePatternList(PatternList * patternList) {
        rack.removeType(patternList);
    }
};


#endif //AAUDIOTRACK_PATTERNGROUP_H
