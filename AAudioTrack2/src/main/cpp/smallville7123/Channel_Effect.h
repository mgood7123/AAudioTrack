//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_CHANNEL_EFFECT_H
#define AAUDIOTRACK_CHANNEL_EFFECT_H

#include <AndroidDAW_SDK/plugin/Plugin.h>

class Channel_Effect {
public:
    Plugin * plugin = nullptr;
    bool plugin_is_allocated = false;
    PortUtils2 * out = nullptr;

    Channel_Effect() {
        out = new PortUtils2();
    }

    ~Channel_Effect() {
        if (plugin_is_allocated) delete plugin;
        out->deallocatePorts<ENGINE_FORMAT>();
        delete out;
    }
};

#endif //AAUDIOTRACK_CHANNEL_EFFECT_H
