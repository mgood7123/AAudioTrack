//
// Created by matthew good on 22/11/20.
//

#include "track_processor.h"
#include "../../ardour/ardour.h"

Track *
track_processor_get_track (
        TrackProcessor * self)
{
    if (!(
            IS_TRACK_PROCESSOR (self) &&
            IS_TRACK (self->track))) return NULL;

    return self->track;

#if 0
    g_return_val_if_fail (
    self &&
    self->track_pos < TRACKLIST->num_tracks, NULL);
  Track * track =
    TRACKLIST->tracks[self->track_pos];
  g_return_val_if_fail (track, NULL);

  return track;
#endif
}

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
        const ARDOUR::frames_t  local_offset,
        const ARDOUR::frames_t  nframes)
{
    Track * tr = track_processor_get_track (self);
    if (!tr) return;

    /*g_message ("processing %s", tr->name);*/

    /* set the audio clip contents to stereo out */
    if (tr->type == TRACK_TYPE_AUDIO)
    {
        // TODO: audio_track_fill_stereo_ports_from_clip
//        audio_track_fill_stereo_ports_from_clip (
//                tr, self->stereo_out,
//                g_start_frames, nframes);
    }
    /* add inputs to outputs */
    switch (tr->in_signal_type)
    {
        case TYPE_AUDIO:
            for (ARDOUR::frames_t l = local_offset;
                 l < nframes; l++)
            {
                ARDOUR::frames_t buffer_size =
                        ARDOUR::AudioEngine::instance()->current_backend()->buffer_size();
                if(!(l < buffer_size)) {
                    LOGF("buffer size mismatch: l: %d, buffer size: %d", l, buffer_size);
                }

                reinterpret_cast<float*>(self->stereo_out->l->buf)[l] +=
                        reinterpret_cast<float*>(self->stereo_in->l->buf)[l] *
                        (self->input_gain ?
                         self->input_gain->control : 1.f);
                if (
                        self->mono //&& control_port_is_toggled (self->mono)
                ) {
                    reinterpret_cast<float*>(self->stereo_out->r->buf)[l] +=
                            reinterpret_cast<float*>(self->stereo_in->l->buf)[l] *
                            (self->input_gain ?
                             self->input_gain->control : 1.f);
                }
                else
                {
                    reinterpret_cast<float*>(self->stereo_out->r->buf)[l] +=
                            reinterpret_cast<float*>(self->stereo_in->r->buf)[l] *
                            (self->input_gain ?
                             self->input_gain->control : 1.f);
                }
            }
            break;
        default:
            break;
    }
}
