//
// Created by matthew good on 18/11/20.
//

#include "AudioEngine.h"
#include "../../other/log.h"
#include "../pdb/failed_constructor.h"
#include "../pdb/i18n.h"
#include "AudioBackend.h"
#include "../Backends/AAudio.h"
#include "PortEngine/port_manager.h"
#include <thread>
#include <fcntl.h>
#include <unistd.h>
#include "../../smallville7123/TempoGrid.h"

//
// plugins
//

// mixer plugin
#include "../../smallville7123/plugins/Mixer.h"
// channel rack plugin
#include "../../smallville7123/plugins/ChannelRack.h"

using namespace std;

namespace ARDOUR {
    using namespace ARDOUR_TYPEDEFS;
    AudioEngine* AudioEngine::_instance = 0;

    AudioEngine::AudioEngine ()
            :// session_remove_pending (false)
//            , session_removal_countdown (-1)
            /*,*/ _running (false)
//            , _freewheeling (false)
            , monitor_check_interval (INT32_MAX)
            , last_monitor_check (0)
            , _processed_samples (-1)
//            , m_meter_thread (0)
//            , _main_thread (0)
            , _mtdm (0)
            , _mididm (0)
            , _measuring_latency (MeasureNone)
            , _latency_flush_samples (0)
            , _latency_signal_latency (0)
            , _stopped_for_latency (false)
            , _started_for_latency (false)
            , _in_destructor (false)
            , _last_backend_error_string(AudioBackend::get_error_string(AudioBackend::NoError))
//            , _hw_reset_event_thread(0)
//            , _hw_reset_request_count(0)
//            , _stop_hw_reset_processing(0)
//            , _hw_devicelist_update_thread(0)
//            , _hw_devicelist_update_count(0)
//            , _stop_hw_devicelist_processing(0)
            , _start_cnt (0)
//            , _init_countdown (0)
//            , _pending_playback_latency_callback (0)
//            , _pending_capture_latency_callback (0)
    {
//        reset_silence_countdown ();
//        start_hw_event_processing();
        discover_backends ();
    }

    AudioEngine::~AudioEngine ()
    {
        _in_destructor = true;
//        stop_hw_event_processing();
        drop_backend ();
        for (BackendMap::const_iterator i = _backends.begin(); i != _backends.end(); ++i) {
            i->second->deinstantiate();
        }
//        delete _main_thread;
    }

    AudioEngine*
    AudioEngine::create ()
    {
        if (_instance) {
            return _instance;
        }

        _instance = new AudioEngine ();

        return _instance;
    }

    void
    AudioEngine::destroy ()
    {
        delete _instance;
        _instance = 0;
    }

    int
    AudioEngine::discover_backends ()
    {
        _backends.clear ();

        _backends.insert (make_pair (AAudio::descriptor()->name, AAudio::descriptor()));

        return _backends.size();
    }

    vector<const AudioBackendInfo*>
    AudioEngine::available_backends() const
    {
        vector<const AudioBackendInfo*> r;

        for (BackendMap::const_iterator i = _backends.begin(); i != _backends.end(); ++i) {
            r.push_back (i->second);
        }

        return r;
    }

    void
    AudioEngine::drop_backend ()
    {
        if (_backend) {
            /* see also ::stop() */
            _backend->stop ();
            _running = false;
//            if (_session && !_session->loading() && !_session->deletion_in_progress()) {
//                // it's not a halt, but should be handled the same way:
//                // disable record, stop transport and I/O processign but save the data.
//                _session->engine_halted ();
//            }
//            Port::PortDrop (); /* EMIT SIGNAL */
//            TransportMasterManager& tmm (TransportMasterManager::instance());
//            tmm.engine_stopped ();
//            tmm.set_session (0); // unregister TMM ports

            /* Stopped is needed for Graph to explicitly terminate threads */
//            Stopped (); /* EMIT SIGNAL */
            _backend->drop_device ();
            _backend.reset ();
        }
    }

    std::shared_ptr<AudioBackend>
    AudioEngine::set_backend (const std::string& name, const std::string& arg1, const std::string& arg2)
    {
        BackendMap::iterator b = _backends.find (name);

        if (b == _backends.end()) {
            return std::shared_ptr<AudioBackend>();
        }

        drop_backend ();

        try {
            if (b->second->instantiate (arg1, arg2)) {
                throw failed_constructor ();
            }

            _backend = b->second->factory (*this);

        } catch (exception& e) {
            LOGE("%s", string_compose (_("Could not create backend for %1: %2"), name, e.what()).c_str());
            return std::shared_ptr<AudioBackend>();
        }

        return _backend;
    }

/* BACKEND PROXY WRAPPERS */

    int
    AudioEngine::start (bool for_latency)
    {
        if (!_backend) {
            return -1;
        }

        if (_running && _backend->can_change_systemic_latency_when_running()) {
            _started_for_latency = for_latency;
        }

        if (_running) {
            return 0;
        }

        _processed_samples = 0;
        last_monitor_check = 0;

        int error_code = _backend->start (for_latency);

        if (error_code != 0) {
            _last_backend_error_string = AudioBackend::get_error_string((AudioBackend::ErrorCode) error_code);
            return -1;
        }

        _running = true;

//        if (_session) {
//            _session->set_sample_rate (_backend->sample_rate());
//
//            if (_session->config.get_jack_time_master()) {
//                _backend->set_time_master (true);
//            }
//
//        }

        midi_info_dirty = true;

        if (!for_latency) {
            /* Call the library-wide ::init_post_engine() before emitting
             * running to ensure that its tasks are complete before any
             * signal handlers execute. PBD::Signal does not ensure
             * ordering of signal handlers so even if ::init_post_engine()
             * is connected first, it may not run first.
             */

//            ARDOUR::init_post_engine (_start_cnt);

//            Running (_start_cnt); /* EMIT SIGNAL */

            /* latency start/stop cycles do not count as "starts" */

            _start_cnt++;
        }


        return 0;
    }

    int
    AudioEngine::stop (bool for_latency)
    {
        bool stop_engine = true;

        if (!_backend) {
            return 0;
        }

//        Glib::Threads::Mutex::Lock pl (_process_lock, Glib::Threads::NOT_LOCK);

//        if (running()) {
//            pl.acquire ();
//        }

        if (for_latency && _backend->can_change_systemic_latency_when_running()) {
            stop_engine = false;
            if (_running && _started_for_latency) {
                _backend->start (false); // keep running, reload latencies
            }
        } else {
            if (_backend->stop ()) {
//                if (pl.locked ()) {
//                    pl.release ();
//                }
                return -1;
            }
        }

//        if (pl.locked ()) {
//            pl.release ();
//        }

        const bool was_running_will_stop = (_running && stop_engine);

        if (was_running_will_stop) {
            _running = false;
        }

//        if (_session && was_running_will_stop && !_session->loading() && !_session->deletion_in_progress()) {
//            // it's not a halt, but should be handled the same way:
//            // disable record, stop transport and I/O processign but save the data.
//            _session->engine_halted ();
//        }

        if (was_running_will_stop) {
            if (!for_latency) {
                _started_for_latency = false;
            } else if (!_started_for_latency) {
                _stopped_for_latency = true;
            }
        }
        _processed_samples = 0;
        _measuring_latency = MeasureNone;
        _latency_output_port.reset ();
        _latency_input_port.reset ();

        if (stop_engine) {
//            Port::PortDrop ();
        }

        if (stop_engine) {
//            TransportMasterManager& tmm (TransportMasterManager::instance());
//            tmm.engine_stopped ();
//            Stopped (); /* EMIT SIGNAL */
        }

        return 0;
    }

    string
    AudioEngine::current_backend_name() const
    {
        if (_backend) {
            return _backend->name();
        }
        return string();
    }

    int
    AudioEngine::set_device_name (const std::string& name)
    {
        if (!_backend) {
            return -1;
        }
        return _backend->set_device_name  (name);
    }

    int
    AudioEngine::set_sample_rate (float sr)
    {
        if (!_backend) {
            return -1;
        }

        return _backend->set_sample_rate  (sr);
    }

    int
    AudioEngine::set_buffer_size (uint32_t bufsiz)
    {
        if (!_backend) {
            return -1;
        }
        return _backend->set_buffer_size  (bufsiz);
    }

    int
    AudioEngine::set_interleaved (bool yn)
    {
        if (!_backend) {
            return -1;
        }
        return _backend->set_interleaved  (yn);
    }

    int
    AudioEngine::set_input_channels (uint32_t ic)
    {
        if (!_backend) {
            return -1;
        }
        return _backend->set_input_channels  (ic);
    }

    int
    AudioEngine::set_output_channels (uint32_t oc)
    {
        if (!_backend) {
            return -1;
        }
        return _backend->set_output_channels (oc);
    }

    int
    AudioEngine::set_systemic_input_latency (uint32_t il)
    {
        if (!_backend) {
            return -1;
        }
        return _backend->set_systemic_input_latency  (il);
    }

    int
    AudioEngine::set_systemic_output_latency (uint32_t ol)
    {
        if (!_backend) {
            return -1;
        }
        return _backend->set_systemic_output_latency  (ol);
    }



    // AUDIO ENGINE

    HostInfo hostInfo;

    void AudioEngine::load(const char *filename) {
        for (int i = 0; i < 20; ++i) {
            channelRack.
                newSamplerChannel(
                        filename,
                        _backend->available_output_channel_count(_backend->device_name())
                )
                ->effectRack->newDelayChannel()
                ;
        }
    }

    void AudioEngine::renderAudio(PortUtils2 * in, PortUtils2 * out) {

        // the sample counter is used to synchronise events with frames
        // A timebase that allows sequencing in relation to musical events like beats or bars
        // it may look something like this:
        // data[4] = {MIDI_ON, MIDI_OFF, MIDI_ON, MIDI_OFF};
        // with the data being mapped to a grid of 1/4 notes
        //
        // see TempoGrid for info on tempo mapping
        //
        // data[0] is frame 0, data[1] is frame 24000, data[2] is frame 48000, data[3] is frame 72000
        // bar[0] is frame 0, bar[1] is frame 96000, and so on
        // tempo_grid[4] = {0, 24000, 48000, 72000}; in 1/4 notes, assuming 48k sample rate, 120 bpm
        // when each note is aligned to 1/4 notes
        // eg note quantisation, (snap to resolution, eg snap to 1/4)

        // If the tempo grid is set up to 120 bpm with 4 notes per bar,
        // on each note, if there is an event associated with that note,
        // the associated generators will trigger for that event.
        // For example, a Sampler and a Synth are assigned to play every
        // 1st note of every bar, when the engine reaches this note,
        // it should call the processing callbacks of the Sampler,
        // and the Synth, with their output each in a seperate mixer
        // input port

        // DON'T FORGET TO MAP!
        if (!hostInfo.tempoGrid.mapped) {
            TempoGrid::map_tempo_to_frame(hostInfo.tempoGrid);
        }

        if (!_backend) {
            LOGE("no backend");
            return;
        }
        channelRack.write(&hostInfo, in, &mixer, out, out->ports.samplesPerChannel);
    }

    sample_position_t AudioEngine::sample_time() {
        if (!_backend) {
            return 0;
        }
        return _backend->sample_time();
    }

    shared_ptr<AudioBackend> AudioEngine::current_backend() const {
        return _backend;
    }
}