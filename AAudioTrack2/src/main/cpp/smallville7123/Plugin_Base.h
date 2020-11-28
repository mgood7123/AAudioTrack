//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_PLUGIN_BASE_H
#define AAUDIOTRACK_PLUGIN_BASE_H

#include "../ardour/Backends/PortUtils2.h"
#include "HostInfo.h"

class Plugin_Base {
public:
    enum {
        PLUGIN_CONTINUE,
        PLUGIN_STOP
    };

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

    virtual int write(HostInfo * hostInfo, PortUtils2 * in, Plugin_Base * mixer, PortUtils2 * out, unsigned int samples) {
        return PLUGIN_STOP;
    };
};

#endif //AAUDIOTRACK_PLUGIN_BASE_H