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

        // how does a timeline play a midi track
        // like would the timeline send MIDI ON and MIDI OFF messages to the midi track?
        // in which when the MIDI track recieves MIDI ON,
        // it starts playing its midi data,
        // and when it recieves MIDI OFF, it stops playing its midi data?

        // welp imma just assume that the timeline uses a MIDI based approach,
        // and that, it sends MIDI note on/off messages to MIDI tracks when
        // appropriate similar to how a piano roll sends MIDI note on/off messages
        // to its associated plugin

        // this causes the current pattern list
        // in the step sequencer's channel rack
        // to partially play
        for (int i = 0; i < trackGroup.rack.typeList.size(); ++i) {
            TrackList *trackList = trackGroup.rack.typeList[i];
            if (trackList != nullptr) {
                for (int i = 0; i < trackList->rack.typeList.size(); ++i) {
                    Track *track = trackList->rack.typeList[i];
//                    if (track->hasNote(hostInfo->engineFrame)) {
                        for (int i = 0; i < PatternGroup::cast(
                                hostInfo->patternGroup)->rack.typeList.size(); ++i) {
                            PatternList *patternList = PatternGroup::cast(
                                    hostInfo->patternGroup)->rack.typeList[i];
                            if (patternList != nullptr) {
                                for (int i = 0; i < patternList->rack.typeList.size(); ++i) {
                                    Pattern *pattern = patternList->rack.typeList[i];
                                    if (pattern != nullptr) {
                                        Channel_Generator *channel = pattern->channelReference;
                                        if (channel != nullptr) {
                                            channel->out->allocatePorts<ENGINE_FORMAT>(out);
                                            channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                                            if (channel->plugin != nullptr) {
                                                if (channel->plugin->is_writing ==
                                                    PLUGIN_CONTINUE) {
                                                    writePlugin(channel->plugin, hostInfo, in,
                                                                mixer,
                                                                channel->out, samples);
                                                }
                                            }
                                            if (channel->effectRack != nullptr) {
                                                if (channel->effectRack->is_writing ==
                                                    PLUGIN_CONTINUE) {
                                                    writeEffectRack(channel->effectRack, hostInfo,
                                                                    in,
                                                                    mixer,
                                                                    channel->out, samples);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
//                    }
                }
            }
        }
        for (int32_t i = 0; i < samples; i ++) {
            for (int i = 0; i < trackGroup.rack.typeList.size(); ++i) {
                TrackList *trackList = trackGroup.rack.typeList[i];
                if (trackList != nullptr) {
                    for (int i = 0; i < trackList->rack.typeList.size(); ++i) {
                        Track *track = trackList->rack.typeList[i];
                        if (track->hasNote(hostInfo->engineFrame)) {
                            for (int i = 0; i < PatternGroup::cast(
                                    hostInfo->patternGroup)->rack.typeList.size(); ++i) {
                                PatternList *patternList = PatternGroup::cast(
                                        hostInfo->patternGroup)->rack.typeList[i];
                                if (patternList != nullptr) {
                                    for (int i = 0; i < patternList->rack.typeList.size(); ++i) {
                                        Pattern *pattern = patternList->rack.typeList[i];
                                        if (pattern != nullptr) {
                                            if (false) {
                                                Channel_Generator *channel = pattern->channelReference;
                                                if (channel != nullptr) {
                                                    channel->out->allocatePorts<ENGINE_FORMAT>(out);
                                                    channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                                                    if (channel->plugin != nullptr) {
                                                        channel->plugin->stopPlayback();
                                                        writePlugin(channel->plugin, hostInfo, in,
                                                                    mixer,
                                                                    channel->out, samples);
                                                    }
                                                    if (channel->effectRack != nullptr) {
                                                        writeEffectRack(channel->effectRack,
                                                                        hostInfo, in, mixer,
                                                                        channel->out, samples);
                                                    }
                                                }
                                            }
                                        }
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

    void mixChannels(HostInfo *hostInfo, PortUtils2 *in, Plugin_Type_Mixer * mixer, PortUtils2 *out,
                     unsigned int samples) {
        prepareMixer(hostInfo, mixer, out);
        mix(hostInfo, in, mixer, out, samples);
        finalizeMixer(hostInfo, mixer);
    }

    bool playing = false;

    int play(Track * track, HostInfo * hostInfo, PortUtils2 *in, Plugin_Base *mixer,
                     PortUtils2 *out, unsigned int samples) {
        if (track->patternListReference != nullptr) {
            for (int i = 0;
                 i < track->patternListReference->rack.typeList.size(); ++i) {
                Pattern *pattern = track->patternListReference->rack.typeList[i];
                if (pattern != nullptr) {
                    Channel_Generator *channel = pattern->channelReference;
                    if (channel != nullptr) {
                        channel->out->allocatePorts<ENGINE_FORMAT>(out);
                        channel->out->fillPortBuffer<ENGINE_FORMAT>(0);
                        if (playing) {
                            hostInfo->fillMidiEvents(
                                    hostInfo->midiInputBuffer,
                                    pattern->pianoRoll.grid,
                                    pattern->pianoRoll.noteData,
                                    samples,
                                    hostInfo->engineFrame
                            );
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
        return PLUGIN_CONTINUE;
    }

    int p = PLUGIN_STOP;
    uint64_t sample;

    void process(Track * track, HostInfo * hostInfo, PortUtils2 *in, Plugin_Base *mixer,
                 PortUtils2 *out, unsigned int samples) {
        size_t size = hostInfo->midiPlaylistInputBuffer.readAvailable();
        if (p == PLUGIN_CONTINUE) {
            playing = true;
            p = play(track, hostInfo, in, mixer, out, samples);
        }
        for (int i = 0; i < size; ++i) {
            smf::MidiEvent *midiEvent = hostInfo->midiPlaylistInputBuffer.at(i);
            LOGE("PROC size = %zu, midiEvent->tick = %d, midiEvent->play = %s", size, midiEvent->tick, midiEvent->isNoteOn() ? "playing" : "paused");
            if (midiEvent->isNoteOn()) {
//                stopPlayback(hostInfo);
                playing = true;
                p = play(track, hostInfo, in, mixer, out, samples);
            } else if (midiEvent->isNoteOff()) {
                stopPlayback(hostInfo);
                play(track, hostInfo, in, mixer, out, samples);
                p = PLUGIN_STOP;
            }
        }
        if (playing) hostInfo->engineFrame += samples;
    }

    void stopPlayback(HostInfo * hostInfo) {
        hostInfo->engineFrame = 0;
        playing = false;
    }

    smf::MidiEvent * lastMidiEvent = nullptr;

    void processPatterns(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
                       unsigned int samples) {
        for (int i = 0; i < trackGroup.rack.typeList.size(); ++i) {
            TrackList *trackList = trackGroup.rack.typeList[i];
            if (trackList != nullptr) {
                for (int i = 0; i < trackList->rack.typeList.size(); ++i) {
                    Track *track = trackList->rack.typeList[i];
                    hostInfo->fillMidiEvents(
                            hostInfo->midiPlaylistInputBuffer,
                            track->pianoRoll.grid,
                            track->pianoRoll.noteData,
                            samples,
                            sample
                    );
                    process(track, hostInfo, in, mixer, out, samples);
                }
            }
        }
        sample += samples;
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin_Base *mixer, PortUtils2 *out,
          unsigned int samples) override {
        processPatterns(hostInfo, in, mixer, out, samples);
        mixChannels(hostInfo, in, reinterpret_cast<Plugin_Type_Mixer*>(mixer), out, samples);
        return PLUGIN_CONTINUE;
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
