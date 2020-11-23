//
// Created by matthew good on 23/11/20.
//

#ifndef AAUDIOTRACK_PORTUTILS_H
#define AAUDIOTRACK_PORTUTILS_H

#include "../../zrythm/audio/port.h"
#include "../../../../../../../../Android/Sdk-Mac/ndk/21.1.6352462/toolchains/llvm/prebuilt/darwin-x86_64/sysroot/usr/include/c++/v1/cstdint"
#include <cstdint>

namespace ARDOUR {

    class PortUtils {
    public:
        struct Ports {
            Port *outputMono = nullptr;
            bool mono = false;
            StereoPorts *outputStereo = nullptr;
            bool stereo = false;
            Port **outputMultiChannel = nullptr;
            bool multiChannel = false;
            uint32_t channelCount = 0;
            uint32_t samples = 0;
        } ports;

        template<typename type>
        void setPortBufferIndex(Port *port, int index, type value) {
            if (ports.samples >= index) {
                LOGF("index %d must be less than ports.samples %d", index,
                     ports.samples);
            }
            if (port == nullptr) {
                LOGF("port is nullptr");
            }
            reinterpret_cast<type *>(port->buf)[index] = value;
        }

        template<typename type>
        void setPortBufferIndex(Port *port, int index, Port * value) {
            setPortBufferIndex(port, index, reinterpret_cast<type *>(value->buf)[index]);
        }

        template<typename type>
        void setPortBufferIndex(int index, Ports & value) {
            if (
                    !(ports.mono && value.mono) ||
                    !(ports.stereo && value.stereo) ||
                    !(ports.multiChannel && value.multiChannel)
                    ) LOGF("port mismatch");
            if (value.samples >= index) LOGF("index %d must be less than ports.samples %d", index,
                                             value.samples);
            if (ports.mono) {
                setPortBufferIndex<type>(ports.outputMono, index, value.outputMono);
            } else if (ports.stereo) {
                setPortBufferIndex<type>(ports.outputStereo->l, index, value.outputStereo->l);
                setPortBufferIndex<type>(ports.outputStereo->r, index, value.outputStereo->r);
            } else if (ports.multiChannel) {
                for (int i = 0; i < ports.channelCount; ++i) {
                    setPortBufferIndex<type>(ports.outputMultiChannel[i], index, value.outputMultiChannel[i]);
                }
            }
        }

        template<typename type>
        void setPortBufferIndex(int index, type value) {
            if (ports.mono) {
                setPortBufferIndex<type>(ports.outputMono, index, value);
            } else if (ports.stereo) {
                setPortBufferIndex<type>(ports.outputStereo->l, index, value);
                setPortBufferIndex<type>(ports.outputStereo->r, index, value);
            } else if (ports.multiChannel) {
                for (int i = 0; i < ports.channelCount; ++i) {
                    setPortBufferIndex<type>(ports.outputMultiChannel[i], index, value);
                }
            }
        }

        template<typename type>
        void fillPortBuffer(Port *port, int samples, type value) {
            for (int i = 0; i < samples; ++i) {
                setPortBufferIndex<type>(port, i, value);
            }
        }

        template<typename type>
        void fillPortBuffer(Port *port, int samples, Port * value) {
            for (int i = 0; i < samples; ++i) setPortBufferIndex<type>(port, i, value);
        }

        template<typename type>
        void fillPortBuffer(Ports & value) {
            if (
                    !(ports.mono && value.mono) ||
                    !(ports.stereo && value.stereo) ||
                    !(ports.multiChannel && value.multiChannel)
                    ) LOGF("port mismatch");
            if (value.samples >= ports.samples)
                LOGF("value.samples %d must be less than ports.samples %d", value.samples,
                        ports.samples);
            if (ports.mono) {
                fillPortBuffer<type>(ports.outputMono, ports.samples, value.outputMono);
            } else if (ports.stereo) {
                fillPortBuffer<type>(ports.outputStereo->l, ports.samples, value.outputStereo->l);
                fillPortBuffer<type>(ports.outputStereo->r, ports.samples, value.outputStereo->r);
            } else if (ports.multiChannel) {
                for (int i = 0; i < ports.channelCount; ++i) {
                    fillPortBuffer<type>(ports.outputMultiChannel[i], ports.samples, value.outputMultiChannel[i]);
                }
            }
        }

        template<typename type>
        void fillPortBuffer(type value, uint32_t length) {
            if (ports.mono) {
                fillPortBuffer<type>(ports.outputMono, length, value);
            } else if (ports.stereo) {
                fillPortBuffer<type>(ports.outputStereo->l, length, value);
                fillPortBuffer<type>(ports.outputStereo->r, length, value);
            } else if (ports.multiChannel) {
                for (int i = 0; i < ports.channelCount; ++i) {
                    fillPortBuffer<type>(ports.outputMultiChannel[i], length, value);
                }
            }
        }

        template<typename type>
        void fillPortBuffer(type value) {
            fillPortBuffer(value, ports.samples);
        }

        void allocatePorts(uint32_t channelCount) {
            ports.channelCount = channelCount;
            ports.samples = 0;
            if (ports.channelCount == 0) LOGF("invalid channel count: 0");
            ports.mono = channelCount == 1;
            if (ports.mono) ports.outputMono = new Port();
            else {
                ports.stereo = channelCount == 2;
                if (ports.stereo) {
                    ports.outputStereo = new StereoPorts();
                    ports.outputStereo->l = new Port();
                    ports.outputStereo->r = new Port();
                } else {
                    ports.multiChannel = true;
                    ports.outputMultiChannel = new Port *[channelCount];
                    for (int i = 0; i < channelCount; ++i) {
                        ports.outputMultiChannel[i] = new Port();
                    }
                }
            }
        }

        template<typename type>
        void deallocatePorts(uint32_t channelCount) {
            ports.channelCount = 0;
            ports.samples = 0;
            if (ports.outputMono != nullptr) {
                // buf is never allocated for mono
                delete ports.outputMono;
                ports.outputMono = nullptr;
                ports.mono = false;
            }
            if (ports.outputStereo != nullptr) {
                if (ports.outputStereo->l->buf != nullptr) {
                    delete[] reinterpret_cast<type *>(ports.outputStereo->l->buf);
                    ports.outputStereo->l->buf = nullptr;
                }
                delete ports.outputStereo->l;
                ports.outputStereo->l = nullptr;
                if (ports.outputStereo->r->buf != nullptr) {
                    delete[] reinterpret_cast<type *>(ports.outputStereo->r->buf);
                    ports.outputStereo->r->buf = nullptr;
                }
                delete ports.outputStereo->r;
                ports.outputStereo->r = nullptr;
                delete ports.outputStereo;
                ports.outputStereo = nullptr;
                ports.stereo = false;
            }
            if (ports.outputMultiChannel != nullptr) {
                for (int i = 0; i < channelCount; ++i) {
                    if (ports.outputMultiChannel[i]->buf != nullptr) {
                        delete[] reinterpret_cast<type *>(ports.outputMultiChannel[i]->buf);
                        ports.outputMultiChannel[i]->buf = nullptr;
                    }
                    delete ports.outputMultiChannel[i];
                    ports.outputMultiChannel[i] = nullptr;
                }
                delete[] ports.outputMultiChannel;
                ports.outputMultiChannel = nullptr;
                ports.multiChannel = false;
            }
        }

        uint32_t getSampleCount() {
            return ports.samples;
        }

        uint32_t getChannelCount() {
            return ports.channelCount;
        }

        template<typename type>
        void interleaveFromPortBuffers(
                void *buffer, uint32_t sample_count
        ) {
            if (ports.mono) {
                buffer = ports.outputMono->buf;
                ports.outputMono->buf = nullptr;
            } else if (ports.stereo) {
                uint32_t samples = sample_count / 2;
                interleave_audio_data<type>(
                        reinterpret_cast<type*>(ports.outputStereo->l->buf),
                        reinterpret_cast<type*>(buffer),
                        samples,
                        0,
                        ports.channelCount
                );
                delete[] reinterpret_cast<type *>(ports.outputStereo->l->buf);
                ports.outputStereo->l->buf = nullptr;
                interleave_audio_data<type>(
                        reinterpret_cast<type*>(ports.outputStereo->r->buf),
                        reinterpret_cast<type*>(buffer),
                        samples,
                        1,
                        ports.channelCount
                );
                delete[] reinterpret_cast<type *>(ports.outputStereo->r->buf);
                ports.outputStereo->l->buf = nullptr;
            } else if (ports.multiChannel) {
                uint32_t samples = sample_count / ports.channelCount;
                for (int i = 0; i < ports.channelCount; ++i) {
                    interleave_audio_data<type>(
                            reinterpret_cast<type*>(ports.outputMultiChannel[i]->buf),
                            reinterpret_cast<type*>(buffer),
                            samples,
                            i,
                            ports.channelCount
                    );
                    delete[] reinterpret_cast<type *>(ports.outputMultiChannel[i]->buf);
                    ports.outputMultiChannel[i]->buf = nullptr;
                }
            }
            ports.samples = 0;
        }

        template<typename type>
        void deinterleaveToPortBuffers(
                void *buffer, uint32_t sample_count
        ) {
            if (ports.mono) {
                ports.outputMono->buf = buffer;
                ports.samples = sample_count;
            } else if (ports.stereo) {
                uint32_t samples = sample_count / 2;
                ports.samples = samples;
                // no use setting this to nullptr since it is immediately reallocated
                if (ports.outputStereo->l->buf != nullptr)
                    delete[] reinterpret_cast<type *>(ports.outputStereo->l->buf);
                ports.outputStereo->l->buf = new type[samples];
                deinterleave_audio_data<type>(
                        reinterpret_cast<type*>(buffer),
                        reinterpret_cast<type*>(ports.outputStereo->l->buf),
                        samples,
                        0,
                        ports.channelCount
                );
                // no use setting this to nullptr since it is immediately reallocated
                if (ports.outputStereo->r->buf != nullptr)
                    delete[] reinterpret_cast<type *>(ports.outputStereo->r->buf);
                ports.outputStereo->r->buf = new type[samples];
                deinterleave_audio_data<type>(
                        reinterpret_cast<type*>(buffer),
                        reinterpret_cast<type*>(ports.outputStereo->r->buf),
                        samples,
                        1,
                        ports.channelCount
                );
            } else if (ports.multiChannel) {
                uint32_t samples = sample_count / ports.channelCount;
                ports.samples = samples;
                for (int i = 0; i < ports.channelCount; ++i) {
                    // no use setting this to nullptr since it is immediately reallocated
                    if (ports.outputMultiChannel[i]->buf != nullptr)
                        delete[] reinterpret_cast<type *>(ports.outputMultiChannel[i]->buf);
                    ports.outputMultiChannel[i]->buf = new type[samples];
                    deinterleave_audio_data<type>(
                            reinterpret_cast<type*>(buffer),
                            reinterpret_cast<type*>(ports.outputMultiChannel[i]->buf),
                            samples,
                            i,
                            ports.channelCount
                    );
                }
            }
        }

        template<typename type>
        void interleave_audio_data(type *input,
                                   type *interleaved_output,
                                   uint32_t sample_count,
                                   uint32_t channel,
                                   uint32_t channel_count) {
            type *ptr = interleaved_output + channel;
            while (sample_count-- > 0) {
                *ptr = *input++;
                ptr += channel_count;
            }
        }

        template<typename type>
        void deinterleave_audio_data(const type *interleaved_input,
                                     type *output,
                                     uint32_t sample_count,
                                     uint32_t channel,
                                     uint32_t channel_count) {
            const type *ptr = interleaved_input + channel;
            while (sample_count-- > 0) {
                *output++ = *ptr;
                ptr += channel_count;
            }
        }
    };
}

#endif //AAUDIOTRACK_PORTUTILS_H
