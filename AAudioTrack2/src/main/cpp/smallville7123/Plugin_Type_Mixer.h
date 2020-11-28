//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_PLUGINS_MIXER_H
#define AAUDIOTRACK_PLUGINS_MIXER_H


#include "Plugin_Base.h"

class Plugin_Type_Mixer : public Plugin_Base {
public:

    virtual void addPort(PortUtils2 *port) = 0;
    virtual void removePort(PortUtils2 *port) = 0;

    bool requires_sample_count() override {
        return Plugin_Base::requires_sample_count();
    }

    bool requires_mixer() override {
        return Plugin_Base::requires_mixer();
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        return Plugin_Base::write(hostInfo, in, mixer, out, samples);
    }
};

#endif //AAUDIOTRACK_PLUGINS_MIXER_H
