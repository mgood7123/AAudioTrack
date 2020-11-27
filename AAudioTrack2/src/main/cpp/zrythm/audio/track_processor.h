/*
 * Copyright (C) 2019-2020 Alexandros Theodotou <alex at zrythm dot org>
 *
 * This file is part of Zrythm
 *
 * Zrythm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Zrythm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Zrythm.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * \file
 *
 * Track processor.
 */

#ifndef __AUDIO_TRACK_PROCESSOR_H__
#define __AUDIO_TRACK_PROCESSOR_H__

//#include "midi.h
#include "port.h"
#include "Track.h"
//#include "types.h"

#include "../../ardour/AudioEngine/typedefs.h"


typedef struct StereoPorts StereoPorts;
typedef struct Port Port;
typedef struct Track Track;

#define TRACK_PROCESSOR_MAGIC 81213128
#define IS_TRACK_PROCESSOR(tr) \
  ((tr) && (tr)->magic == TRACK_PROCESSOR_MAGIC)

/**
 * Automatable MIDI signals.
 */
typedef enum TrackProcessorMidiAutomatable
{
    MIDI_AUTOMATABLE_MOD_WHEEL,
    MIDI_AUTOMATABLE_PITCH_BEND,
    NUM_MIDI_AUTOMATABLES,
} TrackProcessorMidiAutomatable;

/**
 * A TrackProcessor is a processor that is used as
 * the first entry point when processing a track.
 */
typedef struct TrackProcessor
{
    /**
     * L & R audio input ports, if audio.
     */
    StereoPorts *    stereo_in;

    /** Mono toggle, if audio. */
    Port *           mono;

    /** Input gain, if audio. */
    Port *           input_gain;

    /**
     * L & R audio output ports, if audio.
     */
    StereoPorts *    stereo_out;

    /**
     * MIDI in Port.
     *
     * This port is for receiving MIDI signals from
     * an external MIDI source.
     *
     * This is also where piano roll, midi in and midi
     * manual press will be routed to and this will
     * be the port used to pass midi to the plugins.
     */
    Port *           midi_in;

    /**
     * MIDI out port, if MIDI.
     */
    Port *           midi_out;

    /**
     * MIDI input for receiving MIDI signals from
     * the piano roll (i.e., MIDI notes inside
     * regions) or other sources.
     *
     * This will not be a separately exposed port
     * during processing. It will be processed by
     * the TrackProcessor internally.
     */
    Port *           piano_roll;

    /** MIDI CC control ports, 16 channels. */
    Port *           midi_automatables[NUM_MIDI_AUTOMATABLES * 16];

    /** Last processed values, to avoid re-sending
     * messages when the value is the same. */
    float            last_automatable_vals[NUM_MIDI_AUTOMATABLES * 16];

    /**
     * Current dBFS after procesing each output port.
     *
     * Transient variables only used by the GUI.
     */
    float            l_port_db;
    float            r_port_db;

    /** Position of parent Track. */
    int              track_pos;

    /** Pointer to parent track. */
    Track *          track;

    /** Whether this is part of the project (as
     * opposed to a clone). */
    bool             is_project;

    int              magic;
} TrackProcessor;

/**
 * Inits a TrackProcessor after a project is loaded.
 */
void
track_processor_init_loaded (
        TrackProcessor * self,
        bool             is_project);

void
track_processor_set_is_project (
        TrackProcessor * self,
        bool             is_project);

/**
 * Creates a new track processor for the given
 * track.
 */
TrackProcessor *
track_processor_new (
        Track *          track);

/**
 * Copy port values from \ref src to \ref dest.
 */
void
track_processor_copy_values (
        TrackProcessor * dest,
        TrackProcessor * src);

/**
 * Clears all buffers.
 */
void
track_processor_clear_buffers (
        TrackProcessor * self);

/**
 * Disconnects all ports connected to the
 * TrackProcessor.
 */
void
track_processor_disconnect_all (
        TrackProcessor * self);

Track *
track_processor_get_track (
        TrackProcessor * self);

/**
 * Process the TrackProcessor.
 *
 * @param g_start_frames The global start frames.
 * @param local_offset The local start frames.
 * @param nframes The number of frames to process.
 */
void
track_processor_process (
        TrackProcessor * self,
        const long       g_start_frames,
        const ARDOUR_TYPEDEFS::frames_t  local_offset,
        const ARDOUR_TYPEDEFS::frames_t  nframes);

/**
 * Disconnect the TrackProcessor's stereo out ports
 * from the prefader.
 *
 * Used when there is no plugin in the channel.
 */
void
track_processor_disconnect_from_prefader (
        TrackProcessor * self);

/**
 * Connects the TrackProcessor's stereo out ports to
 * the Channel's prefader in ports.
 *
 * Used when deleting the only plugin left.
 */
void
track_processor_connect_to_prefader (
        TrackProcessor * self);

/**
 * Disconnect the TrackProcessor's out ports
 * from the Plugin's input ports.
 */
//void
//track_processor_disconnect_from_plugin (
//        TrackProcessor * self,
//        Plugin         * pl);

/**
 * Connect the TrackProcessor's out ports to the
 * Plugin's input ports.
 */
//void
//track_processor_connect_to_plugin (
//        TrackProcessor * self,
//        Plugin         * pl);

void
track_processor_set_track_pos (
        TrackProcessor * self,
        int              pos);

void
track_processor_append_ports (
        TrackProcessor * self,
        Port ***         ports,
        int *            size,
        bool             is_dynamic,
        int *            max_size);

/**
 * Frees the TrackProcessor.
 */
void
track_processor_free (
        TrackProcessor * self);

/**
 * @}
 */

#endif