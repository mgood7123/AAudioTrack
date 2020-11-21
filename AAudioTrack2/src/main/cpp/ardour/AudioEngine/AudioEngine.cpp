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

    void AudioEngine::renderAudio(void *output_buffer, frames_t number_of_frames_to_render) {


        /// 1. calculate next processed frames.

        /// The number of samples that will have been processed when we've finished
        frames_t next_processed_samples;

        if (_processed_samples < 0) {
            _processed_samples = sample_time();
        }

        /* handle wrap around of total samples counter */

        if (max_sample_position - _processed_samples < number_of_frames_to_render) {
            next_processed_samples =
                    number_of_frames_to_render - (max_sample_position - _processed_samples);
        } else {
            next_processed_samples = _processed_samples + number_of_frames_to_render;
        }



//    if (mIsPlaying && hasData()) {
//        int16_t * AUDIO_DATA = reinterpret_cast<int16_t *>(audioData);
//
//        // Check whether we're about to reach the end of the recording
//        if (!mIsLooping && mReadFrameIndex + totalFrames >= mTotalFrames) {
//            totalFrames = mTotalFrames - mReadFrameIndex;
//            mIsPlaying = false;
//        }
//
//        if (mReadFrameIndex == 0) {
////            GlobalTime.StartOfFile = true;
////            GlobalTime.update(mReadFrameIndex, AudioData);
//        }
//
//        if (mIsLooping) {
//            // we may transition from not looping to looping, upon the EOF being reached
//            // if this happens, reset the frame index
//            if (mReadFrameIndex == mTotalFrames) mReadFrameIndex = 0;
//            for (int32_t i = 0; i < totalFrames; ++i) {
//                for (int j = 0; j < channelCount; ++j) {
//                    targetData[(i * channelCount) + j] = AUDIO_DATA[(mReadFrameIndex * channelCount) + j];
//                }
//
//                // Increment and handle wrap-around
//                if (++mReadFrameIndex >= mTotalFrames) {
////                GlobalTime.EndOfFile = true;
////                GlobalTime.update(mReadFrameIndex, AudioData);
//                    mReadFrameIndex = 0;
//                } else {
////                GlobalTime.update(mReadFrameIndex, AudioData);
//                }
//            }
//            // return from the audio loop
//        } else {
//            // if we are not looping then silence should be emmited when the end of the file is reached
//            bool EOF_reached = mReadFrameIndex == mTotalFrames;
//            if (EOF_reached) {
//                // we know that the EOF has been reached before we even start playing
//                // so just output silence with no additional checking
//                for (int32_t i = 0; i < totalFrames; ++i) {
//                    for (int j = 0; j < channelCount; ++j) {
//                        targetData[(i * channelCount) + j] = 0;
//                    }
//                }
//                // and return from the audio loop
//            } else {
//                // we know that the EOF has been not reached before we even start playing
//                // so we need to do checking to output silence when EOF has been reached
//                for (int32_t i = 0; i < totalFrames; ++i) {
//                    for (int j = 0; j < channelCount; ++j) {
//                        targetData[(i * channelCount) + j] = EOF_reached ? 0 : AUDIO_DATA[
//                                (mReadFrameIndex * channelCount) + j];
//                    }
//
//                    // Increment and handle wrap-around
//                    if (++mReadFrameIndex >= mTotalFrames) {
////                GlobalTime.EndOfFile = true;
////                GlobalTime.update(mReadFrameIndex, AudioData);
//
//                        // do not reset the frame index here
//                        EOF_reached = true;
//                        // output the rest as silence
//
//                        // verification
//                        //
//                        // if i is 5, and totalFrames is 6
//                        // then we need to write 1 frame
//                        //
//                        // however at this point, a frame has already been written
//                        // but the loop is not done, so we need to increment i by 1
//                        // so we can correctly check if we still need to write a frame
//                        //
//
//                        i++;
//
//                        // output the remaining frames as silence
//                        for (; i < totalFrames; ++i) {
//                            for (int j = 0; j < channelCount; ++j) {
//                                targetData[(i * channelCount) + j] = 0;
//                            }
//                        }
//                        // and return from the audio loop
//                    } else {
////                        GlobalTime.update(mReadFrameIndex, AudioData);
//                    }
//                }
//            }
//        }
//    } else {
//        for (int32_t i = 0; i < totalFrames; ++i) {
//            for (int j = 0; j < channelCount; ++j) {
//                targetData[(i * channelCount) + j] = 0;
//            }
//        }
//    }
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
}