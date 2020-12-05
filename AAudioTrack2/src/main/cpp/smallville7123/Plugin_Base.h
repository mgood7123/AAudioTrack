//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_PLUGIN_BASE_H
#define AAUDIOTRACK_PLUGIN_BASE_H

#include "../ardour/Backends/PortUtils2.h"
#include "HostInfo.h"
#include <fcntl.h>
#include <unistd.h>

class Plugin_Base {
public:
    enum {
        PLUGIN_CONTINUE,
        PLUGIN_STOP
    };

    int is_writing = PLUGIN_STOP;

    // add two values and detect overflow, returning TYPE_MAX if overflow occured

    template<typename type> type PLUGIN_HELPERS_add(type TYPE_MIN, type TYPE_MAX, type lhs, type rhs, bool & overflowed)
    {
        overflowed = false;
        if (lhs >= 0) {
            if (TYPE_MAX - lhs < rhs) {
                overflowed = true;
                return TYPE_MAX;
            }
        }
        else {
            if (rhs < TYPE_MIN - lhs) {
                overflowed = true;
                return TYPE_MAX;
            }
        }
        return lhs + rhs;
    }

    virtual bool requires_sample_count() {
        return false;
    };

    virtual bool requires_mixer() {
        return false;
    };

    // TODO: support file loading for sampler plugins

    virtual int write(HostInfo * hostInfo, PortUtils2 * in, Plugin_Base * mixer, PortUtils2 * out, unsigned int samples) {
        return PLUGIN_STOP;
    };

    // SAMPLER

    void * audioData = nullptr;
    size_t audioDataSize = -1;
    int    audioDataTotalFrames = 0;

    virtual void stopPlayback() {}

    void load(const char *filename, int channelCount) {
        int fd;
        size_t len = 0;
        void *o = NULL;
        fd = open(filename, O_RDONLY);
        if (!fd) {
            LOGF("open() failure");
            return;
        }
        len = (size_t) lseek(fd, 0, SEEK_END);
        lseek(fd, 0, 0);
        if (!(o = malloc(len))) {
            int cl = close(fd);
            if (cl < 0) {
                LOGE("cannot close \"%s\", returned %d\n", filename, cl);
            }
            LOGF("failure to malloc()");
            return;
        }
        if ((read(fd, o, len)) == -1) {
            int cl = close(fd);
            if (cl < 0) {
                LOGE("cannot close \"%s\", returned %d\n", filename, cl);
            }
            LOGF("failure to read()");
            return;
        }
        int cl = close(fd);
        if (cl < 0) {
            LOGF("cannot close \"%s\", returned %d\n", filename, cl);
            return;
        }

        // file has been read into memory
        if (audioData != nullptr) {
            free(audioData);
            audioData = nullptr;
        }
        audioDataSize = len;
        audioDataTotalFrames = audioDataSize / (2 * channelCount);

        float * data = static_cast<float *>(malloc(len));
        int16_t * i16 = static_cast<int16_t*>(o);
        for (int i = 0; i < audioDataTotalFrames; i += 2) {
            data[i] = SoapySDR::S16toF32(i16[i]);
            data[i+1] = SoapySDR::S16toF32(i16[i+1]);
        }
        free(o);
        audioData = data;
    }

    inline bool hasAudioData() {
        return audioData != nullptr && audioDataSize != -1;
    }
};

#endif //AAUDIOTRACK_PLUGIN_BASE_H