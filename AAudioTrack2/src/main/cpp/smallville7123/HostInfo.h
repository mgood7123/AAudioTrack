//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_HOSTINFO_H
#define AAUDIOTRACK_HOSTINFO_H

#include "TempoGrid.h"
#include <cstdint>

class HostInfo {
public:
    TempoGrid tempoGrid = TempoGrid(60);
    uint64_t engineFrame = 0;
};

#endif //AAUDIOTRACK_HOSTINFO_H
