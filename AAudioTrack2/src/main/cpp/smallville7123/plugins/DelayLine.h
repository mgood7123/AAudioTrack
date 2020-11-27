//
// Created by matthew good on 27/11/20.
//

#ifndef AAUDIOTRACK_DELAYLINE_H
#define AAUDIOTRACK_DELAYLINE_H


#include <cstdint>

class DelayLine {
public:

#define N 300

    int16_t A[N] = {0};
    int16_t *rptr = A; // read ptr
    int16_t *wptr = A; // write ptr

    int16_t setdelay(int M) {
        rptr = wptr - M;
        while (rptr < A) { rptr += N; }
    }

    int16_t delayline(int16_t x)
    {
        int16_t y;
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
