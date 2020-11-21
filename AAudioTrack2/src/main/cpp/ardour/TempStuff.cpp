//
// Created by matthew good on 20/11/20.
//

#include <iostream>
using namespace std;

// classes
class Glib {
public:
    class Threads {
    public:
        static const int TRY_LOCK = 1;
        class Mutex {
        public:
            bool try_lock() {
            }
            class Lock {
                Mutex * mutex_pointer = nullptr;
                bool _locked = false;
            public:

                Lock(Mutex & mutex, int LOCK_TYPE) {
                    mutex_pointer = &mutex;
                    // switch uses jump table, take advantage of this to improve performance
                    switch (LOCK_TYPE) {
                        case TRY_LOCK:
                            _locked = mutex_pointer->try_lock();
                        default:
                            // unknown
                            return;
                    }
                }

                bool locked() {
                    return _locked;
                }
            };
        };
    };
};
class backend {
public:
    samplecnt_t _processed_samples = 0;
    /* Process time */
    samplepos_t
    sample_time ()
    {
        return _processed_samples;
    }
};
class Session {
public:
    void process(pframes_t nframes) {
        /// assume this generates nframes of audio
        /// but it is best to find out what this does
    }
    void reset_xrun_count() {
        /// reset xrun count
    }
};
class PortManager {
public:
    static void cycle_start(pframes_t nframes) {
        /// find out what this does
    }
    static void silence_outputs(pframes_t nframes) {
        /// assume this sends nframes of silence to the backend outputs
        /// but it is best to find out what this does
    }
    static void silence(pframes_t nframes) {
        /// assume this sends nframes of silence to the backend outputs
        /// but it is best to find out what this does
    }
    static void cycle_end(pframes_t nframes) {
        /// find out what this does
    }
};
class ProtoPort {};
class Boost {
public:
    template <typename T> struct shared_ptr {
        explicit operator bool () {
            return true;
        }
    };
};
class Sample {};
class PortEngine {
public:
    typedef Boost::shared_ptr<ProtoPort> PortPtr;

    /// how is struct PortPtr specified? it is a typedef but it is specified as a struct
    /// Typedef 'PortPtr' cannot be referenced with a struct specifier
    Sample *get_buffer(/*struct*/ PortPtr portPtr, pframes_t nframes) {
        /// aquire buffer from somewhere
    }
};
class MTDM {
public:
    void process(pframes_t nframes, Sample * in, Sample * out) {
        /// find out what this does
    }
};
class MIDIDM {
public:
    void process(pframes_t nframes, PortEngine & PE, Sample * in, Sample * out) {
        /// find out what this does
    }
};

// namespaces

namespace PortEngine_ {
    /// _backend conflics with _backend declaration when using this namespace
    /// rename to avoid conflict
    PortEngine * _Pbackend = nullptr;

    PortEngine&
    port_engine()
    {
        assert (_Pbackend);
        return *_Pbackend;
    }
}

using namespace PortEngine_;

// enums
enum LatencyMeasurement {
    MeasureNone,
    MeasureAudio,
    MeasureMIDI
};

// variables
backend * _backend = nullptr;
Session * _session = nullptr;

Glib::Threads::Mutex       _process_lock;
/// the number of samples processed since start() was called
samplecnt_t               _processed_samples;
MTDM*                     _mtdm;
MIDIDM*                   _mididm;
LatencyMeasurement        _measuring_latency;
PortEngine::PortPtr       _latency_input_port;
PortEngine::PortPtr       _latency_output_port;
samplecnt_t               _latency_flush_samples;
uint32_t                  _init_countdown;

samplepos_t
sample_time ()
{
    if (!_backend) {
        return 0;
    }
    return _backend->sample_time ();
}

#ifdef __clang__
__attribute__((annotate("realtime")))
#endif
int Ardour_Audio_Process_Callback(pframes_t nframes) {
    Glib::Threads::Mutex::Lock tm (_process_lock, Glib::Threads::TRY_LOCK);

//    Port::set_speed_ratio (1.0);

//    PT_TIMING_REF;
//    PT_TIMING_CHECK (1);

    /// 1. calculate next processed frames.

    /// The number of samples that will have been processed when we've finished
    pframes_t next_processed_samples;

    if (_processed_samples < 0) {
        /// retrieve sample time from backend
        _processed_samples = sample_time();
        cerr << "IIIIINIT PS to " << _processed_samples << endl;
    }

    /* handle wrap around of total samples counter */

    if (max_samplepos - _processed_samples < nframes) {
        next_processed_samples = nframes - (max_samplepos - _processed_samples);
    } else {
        next_processed_samples = _processed_samples + nframes;
    }

    if (!tm.locked()) {
        /* return having done nothing */
        if (_session) {
            /// PBD::Signal0<void> Xrun;
            /// assume this is a function pointer

            // Xrun();
        }
        /* really only JACK requires this
         * (other backends clear the output buffers
         * before the process_callback. it may even be
         * jack/alsa only). but better safe than sorry.
         */
        PortManager::silence_outputs (nframes);
        return 0;
    }

    if (_session && _init_countdown > 0) {
        --_init_countdown;
        /* Warm up caches */
        PortManager::cycle_start (nframes);
        _session->process (nframes);
        PortManager::silence (nframes);
        PortManager::cycle_end (nframes);
        if (_init_countdown == 0) {
            _session->reset_xrun_count();
        }
        return 0;
    }

    bool return_after_remove_check = false;

    /// 2. measure latency.

    if (_measuring_latency == MeasureAudio && _mtdm) {
        /* run a normal cycle from the perspective of the PortManager
           so that we get silence on all registered ports.
           we overwrite the silence on the two ports used for latency
           measurement.
        */

        PortManager::cycle_start (nframes);
        PortManager::silence (nframes);

        if (_latency_input_port && _latency_output_port) {
            /// obtain a PortEngine, assumably from a backend
            PortEngine& pe (port_engine());

            /// assume here that we obtain a input buffer, and an output buffer
            Sample* in = (Sample*) pe.get_buffer (_latency_input_port, nframes);
            Sample* out = (Sample*) pe.get_buffer (_latency_output_port, nframes);

            _mtdm->process (nframes, in, out);
        }

        PortManager::cycle_end (nframes);
        return_after_remove_check = true;

    } else if (_measuring_latency == MeasureMIDI && _mididm) {
        /* run a normal cycle from the perspective of the PortManager
           so that we get silence on all registered ports.
           we overwrite the silence on the two ports used for latency
           measurement.
        */

        PortManager::cycle_start (nframes);
        PortManager::silence (nframes);

        if (_latency_input_port && _latency_output_port) {
            PortEngine& pe (port_engine());

            _mididm->process (nframes, pe,
                              pe.get_buffer (_latency_input_port, nframes),
                              pe.get_buffer (_latency_output_port, nframes));
        }

        PortManager::cycle_end (nframes);
        return_after_remove_check = true;

    } else if (_latency_flush_samples) {

        /* wait for the appropriate duration for the MTDM signal to
         * drain from the ports before we revert to normal behaviour.
         */

        PortManager::cycle_start (nframes);
        PortManager::silence (nframes);
        PortManager::cycle_end (nframes);

        if (_latency_flush_samples > nframes) {
            _latency_flush_samples -= nframes;
        } else {
            _latency_flush_samples = 0;
        }

        return_after_remove_check = true;
    }

    /// 3. Remove the current session if needed.

//    if (session_remove_pending) {
//
//        /* perform the actual session removal */
//
//        if (session_removal_countdown < 0) {
//
//            /* fade out over 1 second */
//            session_removal_countdown = sample_rate()/2;
//            session_removal_gain = GAIN_COEFF_UNITY;
//            session_removal_gain_step = 1.0/session_removal_countdown;
//
//        } else if (session_removal_countdown > 0) {
//
//            /* we'll be fading audio out.
//               if this is the last time we do this as part
//               of session removal, do a MIDI panic now
//               to get MIDI stopped. This relies on the fact
//               that "immediate data" (aka "out of band data") from
//               MIDI tracks is *appended* after any other data,
//               so that it emerges after any outbound note ons, etc.
//            */
//
//            if (session_removal_countdown <= nframes) {
//                assert (_session);
//                _session->midi_panic ();
//            }
//
//        } else {
//            /* fade out done */
//            _session = 0;
//            session_removal_countdown = -1; // reset to "not in progress"
//            session_remove_pending = false;
//            session_removed.signal(); // wakes up thread that initiated session removal
//        }
//    }

    if (return_after_remove_check) {
        return 0;
    }

    /// 4. Do something with TransportMasterManager.

//    TransportMasterManager& tmm (TransportMasterManager::instance());
//
//    /* make sure the TMM is up to date about the current session */
//
//    if (_session != tmm.session()) {
//        tmm.set_session (_session);
//    }
//
//    if (_session == 0) {
//
//        if (!_freewheeling) {
//            PortManager::silence_outputs (nframes);
//        }
//
//        _processed_samples = next_processed_samples;
//
//        return 0;
//    }
//
//    if (!_freewheeling || Freewheel.empty()) {
//        /* catch_speed is the speed that we estimate we need to run at
//           to catch (or remain locked to) a transport master.
//        */
//        double catch_speed = tmm.pre_process_transport_masters (nframes, sample_time_at_cycle_start());
//        catch_speed = _session->plan_master_strategy (nframes, tmm.get_current_speed_in_process_context(), tmm.get_current_position_in_process_context(), catch_speed);
//        Port::set_speed_ratio (catch_speed);
//        DEBUG_TRACE (DEBUG::Slave, string_compose ("transport master (current=%1) gives speed %2 (ports using %3)\n", tmm.current() ? tmm.current()->name() : string("[]"), catch_speed, Port::speed_ratio()));
//
//#if 0 // USE FOR DEBUG ONLY
//        /* use with Dummy backend, engine pulse and
//		 * scripts/_find_nonzero_sample.lua
//		 * to correlate with recorded region alignment.
//		 */
//		static bool was_rolling = false;
//		bool is_rolling = _session->transport_rolling();
//		if (!was_rolling && is_rolling) {
//			samplepos_t stacs = sample_time_at_cycle_start ();
//			samplecnt_t sr = sample_rate ();
//			samplepos_t tp = _session->transport_sample ();
//			/* Note: this does not take Port latency into account:
//			 * - always add 12 samples (Port::_resampler_quality)
//			 * - ExistingMaterial: subtract playback latency from engine-pulse
//			 *   We assume the player listens and plays along. Recorded region is moved
//			 *   back by playback_latency
//			 */
//			printf (" ******** Starting play at %ld, next pulse: %ld\n", stacs, ((sr - (stacs % sr)) %sr) + tp);
//		}
//		was_rolling = is_rolling;
//#endif
//    }

    /* tell all relevant objects that we're starting a new cycle */

    /// 5. Call the current session's process callback

//    InternalSend::CycleStart (nframes);
//
//    /* tell all Ports that we're starting a new cycle */
//
//    PortManager::cycle_start (nframes, _session);
//
//    /* test if we are freewheeling and there are freewheel signals connected.
//     * ardour should act normally even when freewheeling unless /it/ is
//     * exporting (which is what Freewheel.empty() tests for).
//     */
//
//    if (_freewheeling && !Freewheel.empty()) {
//        Freewheel (nframes);
//    } else {
//        samplepos_t start_sample = _session->transport_sample ();
//        samplecnt_t pre_roll = _session->remaining_latency_preroll ();
//
//        if (Port::cycle_nframes () <= nframes) {
//            _session->process (Port::cycle_nframes ());
//        } else {
//            pframes_t remain = Port::cycle_nframes ();
//            while (remain > 0) {
//                /* keep track of split_cycle() calls by Session::process */
//                samplecnt_t poff = Port::port_offset ();
//                pframes_t nf = std::min (remain, nframes);
//                _session->process (nf);
//                remain -= nf;
//                if (remain > 0) {
//                    /* calculate split-cycle offset */
//                    samplecnt_t delta = Port::port_offset () - poff;
//                    assert (delta >= 0 && delta <= nf);
//                    if (nf > delta) {
//                        split_cycle (nf - delta);
//                    }
//                }
//            }
//        }
//
//        /* send timecode for current cycle */
//        samplepos_t end_sample = _session->transport_sample ();
//        _session->send_ltc_for_cycle (start_sample, end_sample, nframes);
//        /* and MIDI Clock */
//        _session->send_mclk_for_cycle (start_sample, end_sample, nframes, pre_roll);
//    }
//
//    if (_freewheeling) {
//        PortManager::cycle_end (nframes, _session);
//        return 0;
//    }
//
//    if (!_running) {
//        _processed_samples = next_processed_samples;
//        return 0;
//    }
//
//    if (last_monitor_check + monitor_check_interval < next_processed_samples) {
//
//        PortManager::check_monitoring ();
//        last_monitor_check = next_processed_samples;
//    }
//
//#ifdef SILENCE_AFTER_SECONDS
//
//    bool was_silent = (_silence_countdown == 0);
//
//	if (_silence_countdown >= nframes) {
//		_silence_countdown -= nframes;
//	} else {
//		_silence_countdown = 0;
//	}
//
//	if (!was_silent && _silence_countdown == 0) {
//		_silence_hit_cnt++;
//		BecameSilent (); /* EMIT SIGNAL */
//	}
//
//	if (_silence_countdown == 0 || _session->silent()) {
//		PortManager::silence (nframes);
//	}
//
//#else
//    if (_session->silent()) {
//        PortManager::silence (nframes, _session);
//    }
//#endif
//
//    if (session_remove_pending && session_removal_countdown) {
//
//        PortManager::cycle_end_fade_out (session_removal_gain, session_removal_gain_step, nframes, _session);
//
//        if (session_removal_countdown > nframes) {
//            session_removal_countdown -= nframes;
//        } else {
//            session_removal_countdown = 0;
//        }
//
//        session_removal_gain -= (nframes * session_removal_gain_step);
//    } else {
//        PortManager::cycle_end (nframes, _session);
//    }

    _processed_samples = next_processed_samples;

//    PT_TIMING_CHECK (2);

    return 0;
}

uint64_t currentFrame;
int channelCount = 2;
EventList events;
MIDI_SEQUENCER * sequencer;
AudioBuffers buffersTmp;
Mixed mixed;

void theoreticalMixer(int16_t * buffer_to_fill, int32_t number_of_frames) {
    for (int32_t i = 0; i < number_of_frames; ++i) {
        bool needNewMix = mixed.remaining == 0;

        //
        // get midi events from sequencer
        //
        events = sequencer->getEventsForFrame(currentFrame);

        //
        // check if we actually have any events
        //
        if (events.hasEvents) {
            //
            // we have events, generate audio for events
            //
            buffersTmp = sequencer->generate(number_of_frames);

            if (needNewMix) {
                //
                // mix the audio buffers into a new mix
                //
                mixed = mix(buffersTmp);
            } else {
                //
                // mix the audio buffers into an existing mix, extending the mix if required
                //
                mixed = mix(mixed, buffersTmp);
            }
        }

        //
        // call for loops inside conditions to avoid checking the conditions multiple times
        //
        if (!needNewMix) {
            //
            // if we did not need a new mixer, fill the buffer with mix data
            //
            for (int j = 0; j < channelCount; ++j) {
                buffer_to_fill[(i * channelCount) + j] = mixed.data[(i * channelCount) + j];
                mixed.remaining--;
            }
        } else {
            //
            // if we need a new mixer, then:
            //
            if (events.hasEvents) {
                //
                // if we have events, fill the buffer with mix data
                //
                for (int j = 0; j < channelCount; ++j) {
                    buffer_to_fill[(i * channelCount) + j] = mixed.data[(i * channelCount) + j];
                    mixed.remaining--;
                }
            } else {
                //
                // if we do not have events, fill the buffer with silence
                //
                for (int j = 0; j < channelCount; ++j) {
                    buffer_to_fill[(i * channelCount) + j] = 0;
                }
            }
        }
        currentFrame++;
    }
}