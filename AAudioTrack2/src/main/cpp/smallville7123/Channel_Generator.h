//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_CHANNEL_GENERATOR_H
#define AAUDIOTRACK_CHANNEL_GENERATOR_H

#include "Plugin.h"
#include "plugins/EffectRack.h"

class Channel_Generator {
public:
    Plugin_Type_Generator * plugin = nullptr;
    EffectRack * effectRack = nullptr;
    PortUtils2 * out = nullptr;

    Channel_Generator() {
        effectRack = new EffectRack();
        out = new PortUtils2();
    }

    ~Channel_Generator() {
        delete effectRack;
        out->deallocatePorts<ENGINE_FORMAT>();
        delete out;
    }
};

#endif //AAUDIOTRACK_CHANNEL_GENERATOR_H
