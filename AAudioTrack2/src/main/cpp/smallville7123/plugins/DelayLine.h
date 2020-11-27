//
// Created by matthew good on 27/11/20.
//

#ifndef AAUDIOTRACK_DELAYLINE_H
#define AAUDIOTRACK_DELAYLINE_H

#include <cstdint>

using namespace ARDOUR_TYPEDEFS;

class DelayLine {
public:

#define N 48000

    ENGINE_FORMAT A[N] = {0};
    ENGINE_FORMAT *rptr = A; // read ptr
    ENGINE_FORMAT *wptr = A; // write ptr

    void setdelay(int M) {
        rptr = wptr - M;
        while (rptr < A) { rptr += N; }
    }

    ENGINE_FORMAT delayline(ENGINE_FORMAT x)
    {
        ENGINE_FORMAT y;
        *wptr = x;
        wptr++;
        y = *rptr;
        rptr++;
        if ((wptr-A) >= N) { wptr -= N; }
        if ((rptr-A) >= N) { rptr -= N; }
        return y;
    }

#undef N

};


#endif //AAUDIOTRACK_DELAYLINE_H
