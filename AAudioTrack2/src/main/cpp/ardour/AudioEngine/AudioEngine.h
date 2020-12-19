//
// Created by matthew good on 18/11/20.
//

#ifndef AAUDIOTRACK_AAUDIOENGINE_H
#define AAUDIOTRACK_AAUDIOENGINE_H

#include <aaudio/AAudio.h>
#include <atomic>
#include <thread>
#include <optional>
#include <map>
#include "typedefs.h"
#include "PortEngine/port_engine.h"
#include "../Backends/PortUtils2.h"
#include "../../smallville7123/plugins/Mixer.h"
#include "../../smallville7123/plugins/ChannelRack.h"
#include "../../smallville7123/plugins/EffectRack.h"
#include "../../smallville7123/plugins/Playlist.h"


namespace ARDOUR {
    using namespace ARDOUR_TYPEDEFS;
    class Session;
    class AudioBackend;
    struct AudioBackendInfo;
    static std::shared_ptr<AudioBackend> _backend;
    class MTDM;
    class MIDIDM;

    class AudioEngine {
    public:

        int DSPLoadInt;
        double DSPLoadDouble;
        uint64_t processingTime;
        uint64_t bufferLength;

        Mixer mixer;
        ChannelRack channelRack;
        EffectRack effectRack;
        Playlist playlist;

        /* latency measurement */

        MTDM* mtdm() { return _mtdm; }
        MIDIDM* mididm() { return _mididm; }

        int  prepare_for_latency_measurement ();
        int  start_latency_detection (bool);
        void stop_latency_detection ();
        void set_latency_input_port (const std::string&);
        void set_latency_output_port (const std::string&);
        uint32_t latency_signal_delay () const { return _latency_signal_latency; }

        enum LatencyMeasurement {
            MeasureNone,
            MeasureAudio,
            MeasureMIDI
        };

        LatencyMeasurement measuring_latency () const { return _measuring_latency; }


        bool                      _running;
        /// number of samples between each check for changes in monitor input
        sample_count_t            monitor_check_interval;
        /// time of the last monitor check in samples
        sample_count_t            last_monitor_check;
        /// the number of samples processed since start() was called
        sample_count_t           _processed_samples;
        std::string               _last_backend_error_string;
        uint32_t                  _start_cnt;
        MTDM*                     _mtdm;
        MIDIDM*                   _mididm;
        LatencyMeasurement        _measuring_latency;
        PortEngine::PortPtr       _latency_input_port;
        PortEngine::PortPtr       _latency_output_port;
        sample_count_t            _latency_flush_samples;
        std::string               _latency_input_name;
        std::string               _latency_output_name;
        sample_count_t            _latency_signal_latency;
        bool                      _stopped_for_latency;
        bool                      _started_for_latency;
        bool                      _in_destructor;

        int set_device_name (const std::string&);
        int set_sample_rate (float);
        int set_buffer_size (uint32_t);
        int set_interleaved (bool yn);
        int set_input_channels (uint32_t);
        int set_output_channels (uint32_t);
        int set_systemic_input_latency (uint32_t);
        int set_systemic_output_latency (uint32_t);

        AudioEngine ();
        ~AudioEngine ();
        static AudioEngine* create ();
        static void destroy ();

        int discover_backends();
        std::vector<const AudioBackendInfo*> available_backends() const;
        std::string current_backend_name () const;
        std::shared_ptr<AudioBackend> set_backend (const std::string&, const std::string& arg1, const std::string& arg2);
        std::shared_ptr<AudioBackend> current_backend() const;
        bool setup_required () const;

        /* START BACKEND PROXY API
         *
         * See audio_backend.h for full documentation and semantics. These wrappers
         * just forward to a backend implementation.
         */

        int            start (bool for_latency_measurement=false);
        int            stop (bool for_latency_measurement=false);
        std::string    get_last_backend_error () const { return _last_backend_error_string; }

        static AudioEngine *instance() { return _instance; }

        static AudioEngine*       _instance;

        void died();

        typedef std::map<std::string, AudioBackendInfo *> BackendMap;
        BackendMap _backends;

        AudioBackendInfo *backend_discover(const std::string &);

        void drop_backend();

        void renderAudio(PortUtils2 * in, PortUtils2 * out);

        void load(void * nativeChannel, const char *string);

        sample_position_t sample_time();

        void setGridResolution(void *nativePattern, int size);

        void setPlugin(void *pVoid, void *pVoid1);

        void bindChannelToPattern(void *nativeChannel, void *nativePattern);

        PatternList * createPatternList();

        void deletePatternList(void *patternList);

        Pattern *createPattern(void *patternList);

        void deletePattern(void *patternList, void *pattern);

        void bindChannelToTrack(void *nativeChannel, void *nativeTrack);

        TrackList * createTrackList();

        void deleteTrackList(void *trackList);

        Track *createTrack(void *trackList);

        void deleteTrack(void *trackList, void *track);
    };
}

#endif //AAUDIOTRACK_AAUDIOENGINE_H
