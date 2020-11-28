/*
 * Copyright (C) 2018-2020 Alexandros Theodotou <alex at zrythm dot org>
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
 * Ports that transfer audio/midi/other signals to
 * one another.
 */

#ifndef __AUDIO_PORTS_H__
#define __AUDIO_PORTS_H__

#include "../../ardour/AudioEngine/typedefs.h"

//#include "zrythm-config.h"

//#include <stdbool.h>

//#include "audio/meter.h"
#include "port_identifier.h"
#include "track_processor.h"
#include "../../other/log.h"
//#include "utils/types.h"
//#include "zix/sem.h"
#include "Track.h"

//struct TrackProcessor;

#ifdef HAVE_JACK
#include "weak_libjack.h"
#endif

#ifdef HAVE_RTMIDI
#include <rtmidi/rtmidi_c.h>
#endif

#ifdef HAVE_RTAUDIO
#include <rtaudio/rtaudio_c.h>
#endif

//typedef struct Plugin Plugin;
//typedef struct MidiEvents MidiEvents;
//typedef struct Fader Fader;
//typedef struct SampleProcessor SampleProcessor;
//typedef struct PassthroughProcessor
//        PassthroughProcessor;
//typedef struct ZixRingImpl ZixRing;
//typedef struct WindowsMmeDevice WindowsMmeDevice;
//typedef struct Lv2Port Lv2Port;
//typedef struct Channel Channel;
//typedef struct Track Track;
//typedef struct SampleProcessor SampleProcessor;
//typedef struct TrackProcessor TrackProcessor;
//typedef struct RtMidiDevice RtMidiDevice;
//typedef struct RtAudioDevice RtAudioDevice;
//typedef struct AutomationTrack AutomationTrack;
//typedef struct TruePeakDsp TruePeakDsp;
//typedef struct ExtPort ExtPort;
//typedef enum PanAlgorithm PanAlgorithm;
//typedef enum PanLaw PanLaw;

/**
 * @addtogroup audio
 *
 * @{
 */

#define PORT_MAGIC 456861194
#define IS_PORT(_p) \
  ((_p) && \
   ((Port *) (_p))->magic == PORT_MAGIC)

#define MAX_DESTINATIONS 600
#define FOREACH_SRCS(port) \
  for (int i = 0; i < port->num_srcs; i++)
#define FOREACH_DESTS(port) \
  for (int i = 0; i < port->num_dests; i++)

#define TIME_TO_RESET_PEAK 4800000

/**
 * Special ID for owner_pl, owner_ch, etc. to indicate that
 * the port is not owned.
 */
#define PORT_NOT_OWNED -1

/**
 * What the internal data is.
 */
typedef enum PortInternalType
{
    INTERNAL_NONE,

    /** Pointer to Lv2Port. */
    INTERNAL_LV2_PORT,

    /** Pointer to jack_port_t. */
    INTERNAL_JACK_PORT,

    /** TODO */
    INTERNAL_PA_PORT,

    /** Pointer to snd_seq_port_info_t. */
    INTERNAL_ALSA_SEQ_PORT,
} PortInternalType;

/**
 * Must ONLY be created via port_new()
 */
typedef struct Port
{
    PortIdentifier      id;

    /**
     * Flag to indicate that this port is exposed
     * to the backend.
     */
    int                 exposed_to_backend;

    /**
     * Buffer to be reallocated every time the buffer
     * size changes.
     *
     * The buffer size is buf_size.
     */
    void *             buf = nullptr;
    uint32_t buf_size = 0;

#ifdef MIDI_SUPPORT
    /**
     * Contains raw MIDI data (MIDI ports only)
     */
    MidiEvents *        midi_events;
#endif

    /**
     * Inputs and Outputs.
     *
     * These should be serialized, and when loading
     * they shall be used to find the original ports
     * and replace the pointer (also freeing the
     * current one).
     */
    struct Port *       srcs[MAX_DESTINATIONS];
    struct Port *       dests[MAX_DESTINATIONS];
    PortIdentifier      src_ids[MAX_DESTINATIONS];
    PortIdentifier      dest_ids[MAX_DESTINATIONS];

    /** These are the multipliers for port connections.
     *
     * They range from 0.f to 1.f and the default is
     * 1.f. They correspond to each destination.
     */
    float               multipliers[MAX_DESTINATIONS];

    /** Same as above for sources. */
    float               src_multipliers[MAX_DESTINATIONS];

    /**
     * These indicate whether the destination Port
     * can be removed or the multiplier edited by the
     * user.
     *
     * These are ignored when connecting things
     * internally and are only used to deter the user
     * from breaking necessary connections.
     *
     * 0 == unlocked, 1 == locked.
     */
    int                 dest_locked[MAX_DESTINATIONS];

    /** Same as above for sources. */
    int                 src_locked[MAX_DESTINATIONS];

    /**
     * These indicate whether the connection is
     * enabled.
     *
     * The user can disable port connections only if
     * they are not locked.
     *
     * 0 == disabled (disconnected),
     * 1 == enabled (connected).
     */
    int                 dest_enabled[MAX_DESTINATIONS];

    /** Same as above for sources. */
    int                 src_enabled[MAX_DESTINATIONS];

    /** Counters. */
    int                 num_srcs;
    int                 num_dests;

    /**
     * Indicates whether data or lv2_port should be
     * used.
     */
    PortInternalType    internal_type;

    /**
     * Minimum, maximum and zero values for this
     * port.
     *
     * Note that for audio, this is the amp (0 - 2)
     * and not the actual values.
     */
    float               minf;
    float               maxf;

    /**
     * The zero position of the port.
     *
     * For example, in balance controls, this will
     * be the middle. In audio ports, this will be
     * 0 amp (silence), etc.
     */
    float               zerof;

    /** Default value, only used for controls. */
    float               deff;

    /** Used for LV2. */
//    Lv2Port *          lv2_port;

    /** VST parameter index, if VST control port. */
    int                vst_param_id;

    /** Index of the control parameter (for Carla
     * plugin ports). */
    int                carla_param_id;

    /**
     * Pointer to arbitrary data.
     *
     * Use internal_type to check what data it is.
     *
     * FIXME just add the various data structs here
     * and remove this ambiguity.
     */
    void *              data;

#ifdef _WOE32
    /**
   * Connections to WindowsMmeDevices.
   *
   * These must be pointers to \ref
   * AudioEngine.mme_in_devs or \ref
   * AudioEngine.mme_out_devs and must not be
   * allocated or free'd.
   */
  WindowsMmeDevice *  mme_connections[40];
  int                 num_mme_connections;

  /** Semaphore for changing the connections
   * atomically. */
  ZixSem              mme_connections_sem;
#endif

    /**
     * Last time the port finished dequeueing
     * MIDI events.
     *
     * Used for some backends only.
     */
    int64_t              last_midi_dequeue;

#ifdef HAVE_RTMIDI
    /**
   * RtMidi pointers for input ports.
   *
   * Each RtMidi port represents a device, and this
   * Port can be connected to multiple devices.
   */
  RtMidiDevice *      rtmidi_ins[128];
  int                 num_rtmidi_ins;

  /** RtMidi pointers for output ports. */
  RtMidiDevice *      rtmidi_outs[128];
  int                 num_rtmidi_outs;
#endif

#ifdef HAVE_RTAUDIO
    /**
   * RtAudio pointers for input ports.
   *
   * Each port can have multiple RtAudio devices.
   */
  RtAudioDevice *    rtaudio_ins[128];
  int                num_rtaudio_ins;
#endif

    /**
     * The control value if control port, otherwise
     * 0.0f.
     *
     * FIXME for fader, this should be the
     * fader_val (0.0 to 1.0) and not the
     * amplitude.
     *
     * This value will be snapped (eg, if integer or
     * toggle).
     */
    float               control;

    /** Unsnapped value, used by widgets. */
    float               unsnapped_control;

    /** Flag that the value of the port changed from
     * reading automation. */
    bool                value_changed_from_reading;

    /**
     * Last timestamp the control changed.
     *
     * This is used when recording automation in
     * "touch" mode.
     */
    int64_t              last_change;

    /* ====== flags to indicate port owner ====== */

#ifdef PLUGIN_SUPPORT
    /**
     * Temporary plugin pointer (used when the
     * plugin doesn't exist yet in its supposed slot).
     */
    Plugin *            tmp_plugin;

    /**
     * Temporary track (used when the track doesn't
     * exist yet in its supposed position).
     */
    Track *             tmp_track;

    SampleProcessor *   sample_processor;
#endif
    /** used when loading projects FIXME needed? */
    int                 initialized;

    /**
     * For control ports, when a modulator is
     * attached to the port the previous value will
     * be saved here.
     *
     * Automation in AutomationTrack's will overwrite
     * this value.
     */
    float               base_value;

    /**
     * When a modulator is attached to a control port
     * this will be set to 1, and set back to 0 when
     * all modulators are disconnected.
     */
    //int                 has_modulators;

    /**
     * Capture latency.
     *
     * See page 116 of "The Ardour DAW - Latency
     * Compensation and Anywhere-to-Anywhere Signal
     * Routing Systems".
     */
    long                capture_latency;

    /**
     * Playback latency.
     *
     * See page 116 of "The Ardour DAW - Latency
     * Compensation and Anywhere-to-Anywhere Signal
     * Routing Systems".
     */
    long                playback_latency;

    /** Port undergoing deletion. */
    int                 deleting;

    /**
     * Flag to indicate if the ring buffers below
     * should be filled or not.
     *
     * If a UI element that needs them becomes
     * mapped (visible), this should be set to
     * 1, and when unmapped (invisible) it should
     * be set to 0.
     */
    bool                write_ring_buffers;

    /** Whether the port has midi events not yet
     * processed by the UI. */
    volatile int        has_midi_events;

    /** Used by the UI to detect when unprocessed
     * MIDI events exist. */
    int64_t              last_midi_event_time;

    /**
     * Ring buffer for saving the contents of the
     * audio buffer to be used in the UI instead of
     * directly accessing the buffer.
     *
     * This should contain blocks of block_length
     * samples and should maintain at least 10
     * cycles' worth of buffers.
     *
     * This is also used for CV.
     */
//    ZixRing *           audio_ring;

    /**
     * Ring buffer for saving MIDI events to be
     * used in the UI instead of directly accessing
     * the events.
     *
     * This should keep pushing MidiEvent's whenever
     * they occur and the reader should empty it
     * after cheking if there are any events.
     *
     * Currently there is only 1 reader for each port
     * so this wont be a problem for now, but we
     * should have one ring for each reader.
     */
//    ZixRing *           midi_ring;

    /** Max amplitude during processing, if audio
     * (fabsf). */
    float               peak;

    /** Last time \ref Port.max_amp was set. */
    int64_t              peak_timestamp;

#ifdef MIDI_SUPPORT
    /**
     * Last known MIDI status byte received.
     *
     * Used for running status (see
     * http://midi.teragonaudio.com/tech/midispec/run.htm).
     *
     * Not needed for JACK.
     */
    midi_byte_t         last_midi_status;
#endif

    /**
     * Automation track this port is attached to.
     *
     * To be set at runtime only (not serialized).
     */
//    AutomationTrack *   at;

    /** Pointer to ExtPort, if hw, for quick
     * access (cache). */
//    ExtPort *           ext_port;

    /** Whether this is a project port. */
    bool                is_project;

    /** Magic number to identify that this is a
     * Port. */
    int                 magic;
} Port;

/**
 * L & R port, for convenience.
 *
 */
typedef struct StereoPorts {
    Port       * l;
    Port       * r;
} StereoPorts;

/**
 * Apply given fader value to port.
 *
 * @param start_frame The start frame offset from
 *   0 in this cycle.
 * @param nframes The number of frames to process.
 */
template<typename type> void
port_apply_fader (
        Port *    port,
        float     amp,
        ARDOUR_TYPEDEFS::frames_t start_frame,
        const ARDOUR_TYPEDEFS::frames_t nframes)
{
    ARDOUR_TYPEDEFS::frames_t end = start_frame + nframes;
    while (start_frame < end)
    {
        reinterpret_cast<type*>(port->buf)[start_frame++] *= amp;
    }
}

#endif
