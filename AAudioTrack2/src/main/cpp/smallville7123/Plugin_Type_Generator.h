//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_PLUGIN_TYPE_GENERATOR_H
#define AAUDIOTRACK_PLUGIN_TYPE_GENERATOR_H

#include "Plugin_Base.h"

class Plugin_Type_Generator : public Plugin_Base {
public:
    virtual bool requires_sample_count() {
        return Plugin_Base::requires_sample_count();
    }

    virtual bool requires_mixer() {
        return Plugin_Base::requires_mixer();
    }

    virtual int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
                      unsigned int samples) {
        return Plugin_Base::write(hostInfo, in, mixer, out, samples);
    }
};

#endif //AAUDIOTRACK_PLUGIN_TYPE_GENERATOR_H
