//
// Created by matthew good on 20/11/20.
//

#ifndef AAUDIOTRACK_TYPEDEFS_H
#define AAUDIOTRACK_TYPEDEFS_H

#include <cinttypes>
#include <aaudio/AAudio.h>

namespace ARDOUR {
    typedef int64_t sample_position_t;
    typedef int64_t sample_count_t;
    typedef int32_t frames_t;
    static const sample_position_t max_sample_position = INT64_MAX;
    typedef int16_t ENGINE_FORMAT;
}

#endif //AAUDIOTRACK_TYPEDEFS_H
