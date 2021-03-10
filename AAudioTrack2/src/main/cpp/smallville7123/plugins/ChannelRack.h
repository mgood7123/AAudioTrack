//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_CHANNELRACK_H
#define AAUDIOTRACK_CHANNELRACK_H

#include "../PortUtils2.h"
#include "Mixer.h"
#include "Sampler.h"
#include "Delay.h"
#include "../HostInfo.h"
#include "../Rack.h"
#include "../Channel_Generator.h"
#include "../PianoRoll.h"
#include "../Pattern.h"
#include "../PatternList.h"
#include "../PatternGroup.h"
#include "../../midifile/include/MidiFile.h"
#include <cstdlib>

class ChannelRack : Plugin_Base {
public:

    Rack<Channel_Generator> rack;

    Channel_Generator *newChannel() {
        return rack.newType();
    }

    void removeChannel(Channel_Generator * channel) {
        rack.removeType(channel);
    }

public:
    bool requires_sample_count() override {
        return true;
    }

    bool requires_mixer() override {
        return true;
    }

    PortUtils2 * silencePort = nullptr;

    ChannelRack() {
        silencePort = new PortUtils2();

        // i dont think a ring buffer can be used, eg assuming the ring buffer IS the buffer, then it would need to re-push all notes in the new order each time it is modified, which would involve shifting and inserting notes, which could result in invalid playback of incorrect notes
        // for example if you have 0,0,1,0 and you want to set 1,0,1,0 then it would need to be 0,0,1,0 > 1,0,0,1 > 0,1,0,0 > 1,0,1,0
        // in which the audio thread CAN play any of the notes during the modification of the ring buffer

        // lock-free and wait-free data structures are REQUIRED for communication
        // between the audio thread and low priority threads such as the UI thread

        //00:21 AndroidDAW: https://en.wikipedia.org/wiki/Non-blocking_algorithm#Wait-freedom *
        //00:21 falktx: well anyway, for dsp->ui you very likely want to post data to some ringbuffer, then signal that there is data to read from it on the UI side5~
        //00:21 falktx: the UI side basically just polls at regular intervals to check if there is data or not
        //00:21 falktx: the usual stuff
        //
        //00:24 AndroidDAW: and could i do the same to manipulate for example, arrays which are shared between the RT thread and other non RT threads? for example a piano roll's note data
        //00:24 AndroidDAW: in which the note data can be manipulated by the UI thread, and read by the RT thread
        //00:24 niceplace has joined (~nplace@45.83.91.136)
        //00:25 AndroidDAW: or would a different lock-free structure be required for this?
        //00:27 AndroidDAW: for example, for manipulating channel racks and patterns (eg pattern 1, pattern 2, ect) and so on
        //00:27 AndroidDAW: channel/effect racks*
        //00:28 AndroidDAW: where a ring buffer would not be suitible as the data must be persistant for the duration of the programs runtime
        //00:29 AndroidDAW: eg the channel/effect racks and patterns should not generally impose any limits on how many the user can create
        //00:30 AndroidDAW: (tho i think its common to have something like a 999 limit but even that could be exceeded, especially in a playlist view)
        //00:31 fundamental: If the audio thread goes to execute, the audio thread cannot be blocked by anything, it cannot allocate memory on the heap, nor free it to the heap, and if data is unavailable due to another thread it must be able to continue without it
        //00:31 AndroidDAW: yea
        //00:32 AndroidDAW: fundamental: eg the allocation could be done by the UI thread instead of the RT thread, however the RT thread would need to be capable of handling this
        //00:33 AndroidDAW: eg it must be able to handle non allocated data and partially allocated data (assuming that is possible)
        //00:34 AndroidDAW: tho partially allocated data is basically just non allocated data, or incomplete data, such as the data being allocated but its size not yet set when the RT thread reads it
        //00:36 AndroidDAW: anyway, a ring buffer would not be a suitible structure for data which must not be recycled/size limited right?
        //00:37 AndroidDAW: for example if you have a ring buffer with a capacity of 5, you cannot give it 7 pieces of data and except it to be able to store, and access, all 7 pieces of data
    }

    ~ChannelRack() {
        silencePort->deallocatePorts<ENGINE_FORMAT>();
        delete silencePort;
    }

    void writePlugin(Plugin_Base * plugin, HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer,
                     PortUtils2 *out, unsigned int samples) {
        plugin->write(hostInfo, in, mixer, out, samples);
    }

    void writeEffectRack(EffectRack * effectRack, HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer,
                         PortUtils2 *out, unsigned int samples) {
        effectRack->write(hostInfo, in, mixer, out, samples);
        effectRack->is_writing = true;
    }

    void writeChannels(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
                       unsigned int samples) {
        for (int i = 0; i < PatternGroup::cast(hostInfo->patternGroup)->rack.typeList.size(); ++i) {
            PatternList *patternList = PatternGroup::cast(hostInfo->patternGroup)->rack.typeList[i];
            if (patternList != nullptr) {
                for (int i = 0; i < patternList->rack.typeList.size(); ++i) {
                    Pattern *pattern = patternList->rack.typeList[i];
                    if (pattern != nullptr) {
                        Channel_Generator * channel = pattern->channelReference;
                        if (channel != nullptr) {
                            hostInfo->fillMidiEvents(
                                    hostInfo->midiInputBuffer,
                                    pattern->pianoRoll.grid,
                                    pattern->pianoRoll.noteData,
                                    samples,
                                    hostInfo->engineSample
                            );
                            channel->out->allocatePorts<ENGINE_FORMAT>(out);
                            channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                            if (channel->plugin != nullptr) {
                                channel->plugin->write(hostInfo, in, mixer,
                                                       channel->out, samples);
                            }
                            if (channel->effectRack != nullptr) {
                                channel->effectRack->write(hostInfo, in, mixer,
                                                           channel->out, samples);
                            }
                        }
                    }
                }
            }
        }
        hostInfo->engineSample += samples;
        // LOGE("wrote channels");
    }

    static constexpr int NO_EVENT = 0;
    static constexpr int EVENT_NOTE_ON = 1;
    static constexpr int EVENT_NOTE_OFF = 2;

    void writeChannelsDirect(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
                       unsigned int samples) {
        for (int i = 0; i < rack.typeList.size(); ++i) {
            Channel_Generator * channel = rack.typeList[i];
            if (channel != nullptr) {
                channel->out->allocatePorts<ENGINE_FORMAT>(out);
                channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                if (channel->plugin != nullptr) {
                    hostInfo->midiInputBuffer.consumerClear();
                    if (!channel->plugin->eventBuffer.isEmpty()) {
                        int *tmp = channel->plugin->eventBuffer.peek();
                        int event;
                        if (tmp == nullptr) {
                            event = NO_EVENT;
                        } else {
                            event = *tmp;
                            channel->plugin->eventBuffer.remove();
                        }
                        switch(event) {
                            case NO_EVENT:
                                break;
                            case EVENT_NOTE_ON: {
                                smf::MidiEvent midiEvent;
                                midiEvent.tick = hostInfo->engineSample;
                                midiEvent.makeNoteOn(0, 0, 127);
                                hostInfo->midiInputBuffer.insert(midiEvent);
                                break;
                            }
                            case EVENT_NOTE_OFF: {
                                smf::MidiEvent midiEvent;
                                midiEvent.tick = hostInfo->engineSample;
                                midiEvent.makeNoteOff(0, 0, 0);
                                hostInfo->midiInputBuffer.insert(midiEvent);
                                break;
                            }
                        }
                    }
                    channel->plugin->write(hostInfo, in, mixer, channel->out, samples);
                }
                if (channel->effectRack != nullptr) {
                    channel->effectRack->write(hostInfo, in, mixer, channel->out, samples);
                }
            }
        }
        hostInfo->engineSample += samples;
        // LOGE("wrote channels");
    }

    void prepareMixerDirect(HostInfo * hostInfo, Plugin_Type_Mixer * mixer_, PortUtils2 * out) {
        // LOGE("preparing mixer");
        for (int i = 0; i < rack.typeList.size(); ++i) {
            Channel_Generator * channel = rack.typeList[i];
            if (channel != nullptr) {
                if (channel->out->allocated) {
                    mixer_->addPort(channel->out);
                }
            }
        }
        silencePort->allocatePorts<ENGINE_FORMAT>(out);
        mixer_->addPort(silencePort);
        silencePort->fillPortBuffer<ENGINE_FORMAT>(0);
        // LOGE("prepared mixer");
    }


    void prepareMixer(HostInfo * hostInfo, Plugin_Type_Mixer * mixer_, PortUtils2 * out) {
        // LOGE("preparing mixer");
        for (int i = 0; i < PatternGroup::cast(hostInfo->patternGroup)->rack.typeList.size(); ++i) {
            PatternList *patternList = PatternGroup::cast(hostInfo->patternGroup)->rack.typeList[i];
            if (patternList != nullptr) {
                for (int i = 0; i < patternList->rack.typeList.size(); ++i) {
                    Pattern *pattern = patternList->rack.typeList[i];
                    if (pattern != nullptr) {
                        if (pattern->channelReference != nullptr) {
                            if (pattern->channelReference->out->allocated) {
                                mixer_->addPort(pattern->channelReference->out);
                            }
                        }
                    }
                }
            }
        }
        silencePort->allocatePorts<ENGINE_FORMAT>(out);
        mixer_->addPort(silencePort);
        silencePort->fillPortBuffer<ENGINE_FORMAT>(0);
        // LOGE("prepared mixer");
    }

    void mix(HostInfo *hostInfo, PortUtils2 *in, Plugin_Type_Mixer * mixer, PortUtils2 *out,
             unsigned int samples) {
        // LOGE("mixing");
        mixer->write(hostInfo, in, mixer, out, samples);
        // LOGE("mixed");
    }

    void finalizeMixerDirect(HostInfo * hostInfo, Plugin_Type_Mixer * mixer_) {
        // LOGE("finalizing mixer");
        mixer_->removePort(silencePort);
        silencePort->deallocatePorts<ENGINE_FORMAT>();

        for (int i = 0; i < rack.typeList.size(); ++i) {
            Channel_Generator * channel = rack.typeList[i];
            if (channel != nullptr) {
                if (channel->out->allocated) {
                    mixer_->removePort(channel->out);
                    channel->out->deallocatePorts<ENGINE_FORMAT>();
                }
            }
        }
        // LOGE("finalized mixer");
    }

    void finalizeMixer(HostInfo * hostInfo, Plugin_Type_Mixer * mixer_) {
        // LOGE("finalizing mixer");
        mixer_->removePort(silencePort);
        silencePort->deallocatePorts<ENGINE_FORMAT>();

        for (int i = 0; i < PatternGroup::cast(hostInfo->patternGroup)->rack.typeList.size(); ++i) {
            PatternList *patternList = PatternGroup::cast(hostInfo->patternGroup)->rack.typeList[i];
            if (patternList != nullptr) {
                for (int i = 0; i < patternList->rack.typeList.size(); ++i) {
                    Pattern *pattern = patternList->rack.typeList[i];
                    if (pattern != nullptr) {
                        if (pattern->channelReference != nullptr) {
                            if (pattern->channelReference->out->allocated) {
                                mixer_->removePort(pattern->channelReference->out);
                                pattern->channelReference->out->deallocatePorts<ENGINE_FORMAT>();
                            }
                        }
                    }
                }
            }
        }
        // LOGE("finalized mixer");
    }

    void mixChannelsDirect(HostInfo *hostInfo, PortUtils2 *in, Plugin_Type_Mixer * mixer, PortUtils2 *out,
                           unsigned int samples) {
        prepareMixerDirect(hostInfo, mixer, out);
        mix(hostInfo, in, mixer, out, samples);
        finalizeMixerDirect(hostInfo, mixer);
    }

    void mixChannels(HostInfo *hostInfo, PortUtils2 *in, Plugin_Type_Mixer * mixer, PortUtils2 *out,
                     unsigned int samples) {
        prepareMixer(hostInfo, mixer, out);
        mix(hostInfo, in, mixer, out, samples);
        finalizeMixer(hostInfo, mixer);
    }

    int writeDirect(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) {
        writeChannelsDirect(hostInfo, in, mixer, out, samples);
        mixChannelsDirect(hostInfo, in, reinterpret_cast<Plugin_Type_Mixer*>(mixer), out, samples);
        return PLUGIN_CONTINUE;
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        writeChannels(hostInfo, in, mixer, out, samples);
        mixChannels(hostInfo, in, reinterpret_cast<Plugin_Type_Mixer*>(mixer), out, samples);
        return PLUGIN_CONTINUE;
    }

    void bindChannelToPattern(void *nativeChannel, void *nativePattern) {
        static_cast<Pattern*>(nativePattern)->channelReference = static_cast<Channel_Generator *>(nativeChannel);
    }

    void setPlugin(void *nativeChannel, void *nativePlugin) {
        static_cast<Channel_Generator *>(nativeChannel)->plugin = static_cast<Plugin_Type_Generator *>(nativePlugin);
    }

    void loop(void * nativeChannel, bool value) {
        Plugin_Type_Generator * plugin = static_cast<Channel_Generator *>(nativeChannel)->plugin;
        if (plugin != nullptr) plugin->loop(value);
    }

    void sendEvent(void *nativeChannel, int event) {
        Plugin_Type_Generator * plugin = static_cast<Channel_Generator *>(nativeChannel)->plugin;
        if (plugin != nullptr) plugin->addEvent(event);
    }

    PatternList * newPatternList(HostInfo * hostInfo) {
        return PatternGroup::cast(hostInfo->patternGroup)->newPatternList();
    }

    void deletePatternList(HostInfo * hostInfo, PatternList * patternList) {
        return PatternGroup::cast(hostInfo->patternGroup)->removePatternList(patternList);
    }

    Pattern * newPattern(PatternList * patternList) {
        return patternList->newPattern();
    }

    void deletePattern(PatternList * patternList, Pattern * pattern) {
        return patternList->removePattern(pattern);
    }
};

#endif //AAUDIOTRACK_CHANNELRACK_H
