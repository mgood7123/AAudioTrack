//
// Created by matthew good on 20/11/20.
//

#ifndef AAUDIOTRACK_TYPEDEFS_H
#define AAUDIOTRACK_TYPEDEFS_H

#include <cinttypes>
#include <float.h>

namespace ARDOUR_TYPEDEFS {
    typedef int64_t sample_position_t;
    typedef int64_t sample_count_t;
    typedef int32_t samples_t;
    static const sample_position_t max_sample_position = INT64_MAX;

//    typedef int16_t ENGINE_FORMAT;

//    static const auto ENGINE_FORMAT_MIN = INT16_MIN;

//    static const auto ENGINE_FORMAT_MAX = INT16_MAX;

    typedef float ENGINE_FORMAT;

    static const auto ENGINE_FORMAT_MIN = -1.0;

    static const auto ENGINE_FORMAT_MAX = 1.0;
}

#endif //AAUDIOTRACK_TYPEDEFS_H
