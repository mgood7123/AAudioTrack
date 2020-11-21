//
// Created by matthew good on 20/11/20.
//

#ifndef AAUDIOTRACK_SESSION_H
#define AAUDIOTRACK_SESSION_H


#include "AudioEngine.h"

class Session {
    sample_count_t _processed_samples = 0;
    void renderAudio(void *output_buffer, frames_t number_of_frames_to_render);
};


#endif //AAUDIOTRACK_SESSION_H
