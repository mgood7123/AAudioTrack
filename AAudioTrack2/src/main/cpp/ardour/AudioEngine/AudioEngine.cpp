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
#include "../../smallville7123/Sampler.h"

using namespace std;

namespace ARDOUR {
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

    bool AudioEngine::hasData() {
        return audioData != nullptr && audioDataSize != -1;
    }

    TempoGrid tempoGrid;
    uint64_t engineFrame = 0;

    Sampler sampler;
    bool sampler_is_writing = false;

    void AudioEngine::renderAudio(frames_t number_of_frames_to_render) {

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

        // DONT FORGET TO MAP!
        if (!tempoGrid.mapped) TempoGrid::map_tempo_to_frame(tempoGrid);

        if (!_backend) {
            LOGE("no backend");
            return;
        }
//        LOGW("writing %G milliseconds (%d samples) of data", 1000 / (_backend->sample_rate() / number_of_frames_to_render), number_of_frames_to_render);
//        if (hasData()) {
//            if (sampler_is_writing) {
//                LOGE("telling the sampler to write %d frames of audio", number_of_frames_to_render);
//                sampler_is_writing = sampler.write(
//                        audioData,
//                        mTotalFrames,
//                        _backend->getPortUtils(),
//                        number_of_frames_to_render
//                );
//            }
//            for (int32_t i = 0; i < number_of_frames_to_render; ++i) {
//                // write sample every beat, 120 bpm, 4 beats per bar
//                if (engineFrame == 0 || tempoGrid.sample_matches_samples_per_note(engineFrame)) {
//                    // if there are events for the current sample
//                    LOGE("writing audio on frame %lld for %lld frames, write every %d frames",
//                         engineFrame, mTotalFrames, tempoGrid.samples_per_note);
//                    sampler.mReadFrameIndex = 0;
//                    sampler.mIsPlaying = true;
//                    sampler.mIsLooping = false;
//                    LOGE("telling the sampler to write %d frames of audio", number_of_frames_to_render-i);
//                    sampler_is_writing = sampler.write(
//                            audioData,
//                            mTotalFrames,
//                            _backend->getPortUtils(),
//                            number_of_frames_to_render-i
//                    );
//                } else {
//                    if (!sampler_is_writing) {
//                        // if there are no events for the current sample then output silence
//                        _backend->getPortUtils().setPortBufferIndex(i, 0);
//                    }
//                }
//                engineFrame++;
//                // return from the audio loop
//            }
//        } else {
//            LOGE("AudioEngine writing %d frames of silence", number_of_frames_to_render);
//            _backend->getPortUtils().fillPortBuffer(0);
//            engineFrame += number_of_frames_to_render;
//        }

// OLD

        PortUtils & backendPortUtils = _backend->getPortUtils();
        if (hasData()) {
//            PortUtils portUtils;
            auto channelCount = backendPortUtils.getChannelCount();
//            portUtils.allocatePorts(channelCount);
//            portUtils.deinterleaveToPortBuffers<int16_t>(audioData, channelCount);

            // Check whether we're about to reach the end of the recording
            if (!mIsLooping && mReadFrameIndex + number_of_frames_to_render >= mTotalFrames) {
                number_of_frames_to_render = mTotalFrames - mReadFrameIndex;
                mIsPlaying = false;
            }

            if (mReadFrameIndex == 0) {
//            GlobalTime.StartOfFile = true;
//            GlobalTime.update(mReadFrameIndex, AudioData);
            }

//            for (int i = 0; i < backendPortUtils.ports.outputStereo->l->buf_size; ++i) {
//                backendPortUtils.setPortBufferIndex<int16_t>(backendPortUtils.ports.outputStereo->l, i, portUtils.ports.outputStereo->l);
//                if (++mReadFrameIndex >= mTotalFrames) {
//                    mReadFrameIndex = 0;
//                }
//            }
//            for (int i = 0; i < backendPortUtils.ports.outputStereo->r->buf_size; ++i) {
//                backendPortUtils.setPortBufferIndex<int16_t>(backendPortUtils.ports.outputStereo->r, i, portUtils.ports.outputStereo->r);
//                if (++mReadFrameIndex >= mTotalFrames) {
//                    mReadFrameIndex = 0;
//                }
//            }

            bool INTERLEAVE = false;
            if (INTERLEAVE) {
                for (int i = 0; i < number_of_frames_to_render/2; ++i) {
                    reinterpret_cast<int16_t*>(backendPortUtils.ports.outputStereo->l->buf)[i] =
                            reinterpret_cast<int16_t *>(audioData)[(mReadFrameIndex * channelCount) +
                                                                   0];
                    reinterpret_cast<int16_t*>(backendPortUtils.ports.outputStereo->r->buf)[i] =
                            reinterpret_cast<int16_t *>(audioData)[(mReadFrameIndex * channelCount) +
                                                                   1];
                    mReadFrameIndex++;
                }
            } else {
                for (int i = 0; i < number_of_frames_to_render; ++i) {
                    int16_t * targetData = reinterpret_cast<int16_t*>(backendPortUtils.ports.buffer);
                    int16_t * AUDIO_DATA = reinterpret_cast<int16_t*>(audioData);
                    for (int j = 0; j < channelCount; ++j) {
                        targetData[(i * channelCount) + j] = AUDIO_DATA[(mReadFrameIndex * channelCount) + j];
                    }

                    if (++mReadFrameIndex >= mTotalFrames) {
                        mReadFrameIndex = 0;
                    }
                }
            }
//            portUtils.deallocatePorts<int16_t>(channelCount);
        } else {
            backendPortUtils.fillPortBuffer(0);
        }
    }

//void metronome(AudioEngine * audioEngine) {
//    if (audioEngine != nullptr) {
//        audioEngine->mIsPlaying.store(true);
//        while (audioEngine->metronomeMode.load()) {
//            audioEngine->mReadFrameIndex = 0;
//            //
//            // the audio thread will pause playback if
//            //
//            // mReadFrameIndex + totalFrames >= mTotalFrames
//            //
//            // this means that, if the current frame index plus the total frames to write,
//            // exceeds the total number of frames in the current audio data
//            // then we should render what we can and then pause the playback
//            //
//            // do note that the current frame index is set to 0
//            // when it becomes equal to, or greater then, the total number of frames
//            // in the current audio data
//            //
//            audioEngine->mIsPlaying.store(true);
//            this_thread::sleep_for(chrono::milliseconds (500));
//        }
//    }
//}

    void AudioEngine::load(const char *filename) {
        int fd;
        size_t len = 0;
        char *o = NULL;
        fd = open(filename, O_RDONLY);
        if (!fd) {
            LOGF("open() failure");
            return;
        }
        len = (size_t) lseek(fd, 0, SEEK_END);
        lseek(fd, 0, 0);
        if (!(o = (char *) malloc(len))) {
            int cl = close(fd);
            if (cl < 0) {
                LOGE("cannot close \"%s\", returned %d\n", filename, cl);
            }
            LOGF("failure to malloc()");
            return;
        }
        if ((read(fd, o, len)) == -1) {
            int cl = close(fd);
            if (cl < 0) {
                LOGE("cannot close \"%s\", returned %d\n", filename, cl);
            }
            LOGF("failure to read()");
            return;
        }
        int cl = close(fd);
        if (cl < 0) {
            LOGF("cannot close \"%s\", returned %d\n", filename, cl);
            return;
        }

        // file has been read into memory
        if (audioData != nullptr) {
            free(audioData);
            audioData = nullptr;
        }
        audioData = o;
        audioDataSize = len;
        mTotalFrames = audioDataSize / (2 * _backend->available_output_channel_count(_backend->device_name()));
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