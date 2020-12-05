//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_CHANNEL_EFFECT_H
#define AAUDIOTRACK_CHANNEL_EFFECT_H

#include "Plugin.h"

class Channel_Effect {
public:
    Plugin_Type_Effect * plugin = nullptr;
    PortUtils2 * out = new PortUtils2();
};

#endif //AAUDIOTRACK_CHANNEL_EFFECT_H
