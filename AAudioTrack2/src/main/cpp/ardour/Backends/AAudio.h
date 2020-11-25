//
// Created by matthew good on 20/11/20.
//

#ifndef AAUDIOTRACK_AAUDIO_H
#define AAUDIOTRACK_AAUDIO_H

#include "../ardour.h"
#include <aaudio/AAudio.h>

namespace ARDOUR {
    class AAudio : public AudioBackend {
    public:
        std::string _instance_name;

        static AudioBackendInfo _descriptor;
        static std::shared_ptr<AudioBackend> backend_factory (AudioEngine& e);
        static int instantiate (const std::string& arg1, const std::string& /* arg2 */);
        static int deinstantiate ();
        static bool already_configured ();
        static bool available ();
        static ARDOUR::AudioBackendInfo* descriptor();

        // configuration

        DeviceStatus currentDeviceStatus;
        float currentSampleRate;
        int32_t currentBufferSizeInFrames;
        int32_t currentInputChannelCount;
        int32_t currentOutputChannelCount;

        AAudioStreamBuilder *builder = nullptr;
        AAudioStream *stream = nullptr;
        int32_t underrunCount = 0;
        int32_t previousUnderrunCount = 0;
        sample_count_t _processed_samples = 0;

        AAudio(AudioEngine &audioEngine, AudioBackendInfo &i);
        ~AAudio();

        static aaudio_data_callback_result_t onAudioReady(
                AAudioStream *stream, void *userData, void *audioData,
                frames_t number_of_frames_to_render
        );

        static void onError(
                AAudioStream *stream, void *userData, aaudio_result_t error
        );

        aaudio_result_t waitForState(aaudio_stream_state_t streamState);

        aaudio_result_t CreateStream();
        aaudio_result_t DestroyStream();

        void RestartStreamNonBlocking();

        void RestartStreamBlocking();

        aaudio_result_t StartStreamNonBlocking();

        aaudio_result_t StartStreamBlocking();

        aaudio_result_t PauseStreamNonBlocking();

        aaudio_result_t PauseStreamBlocking();

        aaudio_result_t StopStreamNonBlocking();

        aaudio_result_t StopStreamBlocking();

        aaudio_result_t FlushStreamNonBlocking();

        aaudio_result_t FlushStreamBlocking();

        std::string name() const override;

        bool is_realtime() const override;

        std::vector<DeviceStatus> enumerate_devices() const override;

        std::vector<float> available_sample_rates(const std::string &device) const override;

        std::vector<uint32_t> available_buffer_sizes(const std::string &device) const override;

        uint32_t available_input_channel_count(const std::string &device) const override;

        uint32_t available_output_channel_count(const std::string &device) const override;

        bool can_change_sample_rate_when_running() const override;

        bool can_change_buffer_size_when_running() const override;

        bool can_measure_systemic_latency() const override;

        int set_device_name(const std::string &string) override;

        int set_sample_rate(float d) override;

        int set_buffer_size(uint32_t uint32) override;

        int set_interleaved(bool yn) override;

        int set_input_channels(uint32_t uint32) override;

        int set_output_channels(uint32_t uint32) override;

        int set_systemic_input_latency(uint32_t uint32) override;

        int set_systemic_output_latency(uint32_t uint32) override;

        int set_systemic_midi_input_latency(std::string string, uint32_t uint32) override;

        int set_systemic_midi_output_latency(std::string string, uint32_t uint32) override;

        std::string device_name() const override;

        float sample_rate() const override;

        uint32_t buffer_size() const override;

        bool interleaved() const override;

        uint32_t input_channels() const override;

        uint32_t output_channels() const override;

        uint32_t systemic_input_latency() const override;

        uint32_t systemic_output_latency() const override;

        uint32_t systemic_midi_input_latency(std::string string) const override;

        uint32_t systemic_midi_output_latency(std::string string) const override;

        std::string control_app_name() const override;

        void launch_control_app() override;

        std::vector<std::string> enumerate_midi_options() const override;

        int set_midi_option(const std::string &option) override;

        std::string midi_option() const override;

        std::vector<DeviceStatus> enumerate_midi_devices() const override;

        int set_midi_device_enabled(std::string string, bool b) override;

        bool midi_device_enabled(std::string string) const override;

        bool can_set_systemic_midi_latencies() const override;

        int stop() override;

        int reset_device() override;

        int freewheel(bool start_stop) override;

        float dsp_load() const override;

        sample_position_t sample_time() override;

        sample_position_t sample_time_at_cycle_start() override;

        frames_t samples_since_cycle_start() override;

        int create_process_thread(std::function<void()> func) override;

        int join_process_threads() override;

        bool in_process_thread() override;

        uint32_t process_thread_count() override;

        void update_latencies() override;

        int drop_device() override;

        PortUtils &getPortUtils() override;

    protected:
        int _start(bool for_latency_measurement) override;
    };
}

#endif //AAUDIOTRACK_AAUDIO_H
