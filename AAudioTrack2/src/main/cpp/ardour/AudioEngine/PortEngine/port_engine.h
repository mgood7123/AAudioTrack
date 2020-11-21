//
// Created by matthew good on 21/11/20.
//

#ifndef AAUDIOTRACK_PORT_ENGINE_H
#define AAUDIOTRACK_PORT_ENGINE_H

namespace ARDOUR {

    class PortManager;

/** PortEngine is an abstract base class that defines the functionality
 * required by Ardour.
 *
 * A Port is basically an endpoint for a datastream (which can either be
 * continuous, like audio, or event-based, like MIDI). Ports have buffers
 * associated with them into which data can be written (if they are output
 * ports) and from which data can be read (if they input ports). Ports can be
 * connected together so that data written to an output port can be read from
 * an input port. These connections can be 1:1, 1:N OR N:1.
 *
 * Ports may be associated with software only, or with hardware.  Hardware
 * related ports are often referred to as physical, and correspond to some
 * relevant physical entity on a hardware device, such as an audio jack or a
 * MIDI connector. Physical ports may be potentially asked to monitor their
 * inputs, though some implementations may not support this.
 *
 * Most physical ports will also be considered "terminal", which means that
 * data delivered there or read from there will go to or comes from a system
 * outside of the PortEngine implementation's control (e.g. the analog domain
 * for audio, or external MIDI devices for MIDI). Non-physical ports can also
 * be considered "terminal". For example, the output port of a software
 * synthesizer is a terminal port, because the data contained in its buffer
 * does not and cannot be considered to come from any other port - it is
 * synthesized by its owner.
 *
 * Ports also have latency associated with them. Each port has a playback
 * latency and a capture latency:
 *
 * <b>capture latency</b>: how long since the data read from the buffer of a
 *                  port arrived at at a terminal port.  The data will have
 *                  come from the "outside world" if the terminal port is also
 *                  physical, or will have been synthesized by the entity that
 *                  owns the terminal port.
 *
 * <b>playback latency</b>: how long until the data written to the buffer of
 *                   port will reach a terminal port.
 *
 *
 * For more detailed questions about the PortEngine API, consult the JACK API
 * documentation, on which this entire object is based.
 */

    class ProtoPort {
        public:
            ProtoPort() {}
            virtual ~ProtoPort () {}
    };

    class PortEngine {
        public:
        /** Opaque handle to use as reference for Ports
         *
         * The handle needs to be lifetime managed (i.e. a shared_ptr type)
         * in order to allow RCU to provide lock-free cross-thread operations
         * on ports and ports containers.
         *
         * We could theoretically use a template (PortEngine\<T\>) and define
         * PortHandle as T, but this complicates the desired inheritance
         * pattern in which FooPortEngine handles things for the Foo API,
         * rather than being a derivative of PortEngine\<Foo\>.
         *
         * We use this to declare return values and members of structures.
         */
        typedef std::shared_ptr<ProtoPort> PortPtr;
    };
}

#endif //AAUDIOTRACK_PORT_ENGINE_H
