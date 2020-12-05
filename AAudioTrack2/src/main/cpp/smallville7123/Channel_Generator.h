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
    PortUtils2 * out = new PortUtils2();
};

#endif //AAUDIOTRACK_CHANNEL_GENERATOR_H
