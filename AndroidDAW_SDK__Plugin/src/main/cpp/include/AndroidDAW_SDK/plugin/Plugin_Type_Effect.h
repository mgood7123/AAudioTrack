//
// Created by matthew good on 28/11/20.
//

#ifndef AndroidDAW_SDK__Plugin_TYPE_EFFECT_H
#define AndroidDAW_SDK__Plugin_TYPE_EFFECT_H

#include "Plugin_Base.h"

class Plugin_Type_Effect : public Plugin_Base {
public:
    virtual bool requires_sample_count() override {
        return Plugin_Base::requires_sample_count();
    }

    virtual bool requires_mixer() override {
        return Plugin_Base::requires_mixer();
    }

    virtual int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
                      unsigned int samples) override {
        return Plugin_Base::write(hostInfo, in, mixer, out, samples);
    }

    virtual void loop(bool value) override {
        Plugin_Base::loop(value);
    }

    virtual void startPlayback() override {
        Plugin_Base::startPlayback();
    }

    virtual void pausePlayback() override {
        Plugin_Base::pausePlayback();
    }

    virtual void stopPlayback() override {
        Plugin_Base::stopPlayback();
    }
};

#endif //AndroidDAW_SDK__Plugin_TYPE_EFFECT_H
