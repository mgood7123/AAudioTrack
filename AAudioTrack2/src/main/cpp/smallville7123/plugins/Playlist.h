//
// Created by matthew good on 28/11/20.
//

#ifndef AAUDIOTRACK_PLAYLIST_H
#define AAUDIOTRACK_PLAYLIST_H

#include "../../ardour/Backends/PortUtils2.h"
#include "Mixer.h"
#include "Sampler.h"
#include "Delay.h"
#include "../HostInfo.h"
#include "../Rack.h"
#include "../Channel_Generator.h"
#include "../PianoRoll.h"
#include "../Track.h"
#include "../TrackList.h"
#include "../TrackGroup.h"

class Playlist : Plugin_Base {
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

    TrackGroup trackGroup;

    Playlist() {
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
        //00:27 AndroidDAW: for example, for manipulating channel racks and tracks (eg track 1, track 2, ect) and so on
        //00:27 AndroidDAW: channel/effect racks*
        //00:28 AndroidDAW: where a ring buffer would not be suitible as the data must be persistant for the duration of the programs runtime
        //00:29 AndroidDAW: eg the channel/effect racks and tracks should not generally impose any limits on how many the user can create
        //00:30 AndroidDAW: (tho i think its common to have something like a 999 limit but even that could be exceeded, especially in a playlist view)
        //00:31 fundamental: If the audio thread goes to execute, the audio thread cannot be blocked by anything, it cannot allocate memory on the heap, nor free it to the heap, and if data is unavailable due to another thread it must be able to continue without it
        //00:31 AndroidDAW: yea
        //00:32 AndroidDAW: fundamental: eg the allocation could be done by the UI thread instead of the RT thread, however the RT thread would need to be capable of handling this
        //00:33 AndroidDAW: eg it must be able to handle non allocated data and partially allocated data (assuming that is possible)
        //00:34 AndroidDAW: tho partially allocated data is basically just non allocated data, or incomplete data, such as the data being allocated but its size not yet set when the RT thread reads it
        //00:36 AndroidDAW: anyway, a ring buffer would not be a suitible structure for data which must not be recycled/size limited right?
        //00:37 AndroidDAW: for example if you have a ring buffer with a capacity of 5, you cannot give it 7 pieces of data and except it to be able to store, and access, all 7 pieces of data
    }

    ~Playlist() {
        silencePort->deallocatePorts<ENGINE_FORMAT>();
        delete silencePort;
    }

    void writePlugin(Plugin_Base * plugin, HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer,
                     PortUtils2 *out, unsigned int samples) {
        plugin->is_writing = plugin->write(hostInfo, in, mixer,
                                           out, samples);
    }

    void writeEffectRack(EffectRack * effectRack, HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer,
                         PortUtils2 *out, unsigned int samples) {
        effectRack->is_writing = effectRack->write(hostInfo, in, mixer,
                                                   out, samples);
    }

    void writeChannels(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
                       unsigned int samples) {
        // LOGE("writing channels");
        for (int i = 0; i < trackGroup.rack.typeList.size(); ++i) {
            TrackList *trackList = trackGroup.rack.typeList[i];
            if (trackList != nullptr) {
                for (int i = 0; i < trackList->rack.typeList.size(); ++i) {
                    Track *track = trackList->rack.typeList[i];
                    if (track != nullptr) {
                        Channel_Generator *channel = track->channelReference;
                        if (channel != nullptr) {
                            channel->out->allocatePorts<ENGINE_FORMAT>(out);
                            channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                            if (channel->plugin != nullptr) {
                                if (channel->plugin->is_writing == PLUGIN_CONTINUE) {
                                    writePlugin(channel->plugin, hostInfo, in, mixer,
                                                channel->out, samples);
                                }
                            }
                            if (channel->effectRack != nullptr) {
                                if (channel->effectRack->is_writing == PLUGIN_CONTINUE) {
                                    writeEffectRack(channel->effectRack, hostInfo, in,
                                                    mixer,
                                                    channel->out, samples);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int32_t i = 0; i < samples; i ++) {
            for (int i = 0; i < trackGroup.rack.typeList.size(); ++i) {
                TrackList *trackList = trackGroup.rack.typeList[i];
                if (trackList != nullptr) {
                    for (int i = 0; i < trackList->rack.typeList.size(); ++i) {
                        Track *track = trackList->rack.typeList[i];
                        if (track != nullptr) {
                            if (track->hasNote(hostInfo->engineFrame)) {
                                Channel_Generator * channel = track->channelReference;
                                if (channel != nullptr) {
                                    channel->out->allocatePorts<ENGINE_FORMAT>(out);
                                    channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                                    if (channel->plugin != nullptr) {
                                        channel->plugin->stopPlayback();
                                        writePlugin(channel->plugin, hostInfo, in, mixer,
                                                    channel->out, samples);
                                    }
                                    if (channel->effectRack != nullptr) {
                                        writeEffectRack(channel->effectRack, hostInfo, in, mixer,
                                                        channel->out, samples);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            hostInfo->engineFrame++;
            // return from the audio loop
        }
        // LOGE("wrote channels");
    }

    void prepareMixer(Plugin_Type_Mixer * mixer_, PortUtils2 * out) {
        // LOGE("preparing mixer");
        for (int i = 0; i < trackGroup.rack.typeList.size(); ++i) {
            TrackList *trackList = trackGroup.rack.typeList[i];
            if (trackList != nullptr) {
                for (int i = 0; i < trackList->rack.typeList.size(); ++i) {
                    Track *track = trackList->rack.typeList[i];
                    if (track != nullptr) {
                        if (track->channelReference != nullptr) {
                            if (track->channelReference->out->allocated) {
                                mixer_->addPort(track->channelReference->out);
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

    void finalizeMixer(Plugin_Type_Mixer * mixer_) {
        // LOGE("finalizing mixer");
        mixer_->removePort(silencePort);
        silencePort->deallocatePorts<ENGINE_FORMAT>();

        for (int i = 0; i < trackGroup.rack.typeList.size(); ++i) {
            TrackList *trackList = trackGroup.rack.typeList[i];
            if (trackList != nullptr) {
                for (int i = 0; i < trackList->rack.typeList.size(); ++i) {
                    Track *track = trackList->rack.typeList[i];
                    if (track != nullptr) {
                        if (track->channelReference != nullptr) {
                            if (track->channelReference->out->allocated) {
                                mixer_->removePort(track->channelReference->out);
                                track->channelReference->out->deallocatePorts<ENGINE_FORMAT>();
                            }
                        }
                    }
                }
            }
        }
        // LOGE("finalized mixer");
    }

    void mixChannels(HostInfo *hostInfo, PortUtils2 *in, Plugin_Type_Mixer * mixer, PortUtils2 *out,
                     unsigned int samples) {
        prepareMixer(mixer, out);
        mix(hostInfo, in, mixer, out, samples);
        finalizeMixer(mixer);
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
              unsigned int samples) override {
        writeChannels(hostInfo, in, mixer, out, samples);
        mixChannels(hostInfo, in, reinterpret_cast<Plugin_Type_Mixer*>(mixer), out, samples);
        return PLUGIN_CONTINUE;
    }

    void bindChannelToTrack(void *nativeChannel, void *nativeTrack) {
        static_cast<Track*>(nativeTrack)->channelReference = static_cast<Channel_Generator *>(nativeChannel);
    }

    void setPlugin(void *nativeChannel, void *nativePlugin) {
        static_cast<Channel_Generator *>(nativeChannel)->plugin = static_cast<Plugin_Type_Generator *>(nativePlugin);
    }

    TrackList * newTrackList() {
        return trackGroup.newTrackList();
    }

    void deleteTrackList(TrackList * trackList) {
        return trackGroup.removeTrackList(trackList);
    }

    Track * newTrack(TrackList * trackList) {
        return trackList->newTrack();
    }

    void deleteTrack(TrackList * trackList, Track * track) {
        return trackList->removeTrack(track);
    }
};

#endif //AAUDIOTRACK_PLAYLIST_H
