//
// Created by matthew good on 22/11/20.
//

#ifndef AAUDIOTRACK_TRACK_H
#define AAUDIOTRACK_TRACK_H

#include "port_identifier.h"

#define MAX_REGIONS 300

#define TRACK_MIN_HEIGHT 24
#define TRACK_DEF_HEIGHT 48

#define TRACK_MAGIC 21890135
#define IS_TRACK(tr) (tr && tr->magic == TRACK_MAGIC)

/**
 * The Track's type.
 */
typedef enum TrackType
{
    /**
     * Instrument tracks must have an Instrument
     * plugin at the first slot and they produce
     * audio output.
     */
    TRACK_TYPE_INSTRUMENT,

    /**
     * Audio tracks can record and contain audio
     * clips. Other than that their channel strips
     * are similar to buses.
     */
    TRACK_TYPE_AUDIO,

    /**
     * The master track is a special type of group
     * track.
     */
    TRACK_TYPE_MASTER,

    /**
     * The chord track contains chords that can be
     * used to modify midi in real time or to color
     * the piano roll.
     */
    TRACK_TYPE_CHORD,

    /**
     * Marker Track's contain named markers at
     * specific Position's in the song.
     */
    TRACK_TYPE_MARKER,

    /**
     * Special track for BPM (tempo) and time
     * signature events.
     */
    TRACK_TYPE_TEMPO,

    /**
     * Special track to contain global Modulator's.
     */
    TRACK_TYPE_MODULATOR,

    /**
     * Buses are channels that receive audio input
     * and have effects on their channel strip. They
     * are similar to Group Tracks, except that they
     * cannot be routed to directly. Buses are used
     * for send effects.
     */
    TRACK_TYPE_AUDIO_BUS,

    /**
     * Group Tracks are used for grouping audio
     * signals, for example routing multiple drum
     * tracks to a "Drums" group track. Like buses,
     * they only contain effects but unlike buses
     * they can be routed to.
     */
    TRACK_TYPE_AUDIO_GROUP,

    /**
     * Midi tracks can only have MIDI effects in the
     * strip and produce MIDI output that can be
     * routed to instrument channels or hardware.
     */
    TRACK_TYPE_MIDI,


    /** Same with audio bus but for MIDI signals. */
    TRACK_TYPE_MIDI_BUS,

    /** Same with audio group but for MIDI signals. */
    TRACK_TYPE_MIDI_GROUP,
} TrackType;

struct Track {
    /** The type of track this is. */
    TrackType           type;

    /**
     * The input signal type (eg audio bus tracks have
     * audio input signals).
     */
    PortType             in_signal_type;

    int                  magic;
};


#endif //AAUDIOTRACK_TRACK_H
