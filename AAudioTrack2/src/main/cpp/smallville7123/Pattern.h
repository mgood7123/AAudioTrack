//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_PATTERN_H
#define AAUDIOTRACK_PATTERN_H


#include "PianoRoll.h"
#include "Rack.h"
#include "Channel_Generator.h"
#include <AndroidDAW_SDK/midifile/MidiEventList.h>
#include <cstdint>

//02:16 vampifrog: so, make a table of each tick
//02:17 vampifrog: let's say you have two clips, from 2 to 4 and from 4 to 6
//02:17 vampifrog: make a table of ticks from 0 to 7
//02:17 vampifrog: and which clip would be active
//02:17 vampifrog: I'd say at no point should 2 clips be active
//02:18 AndroidDAW: ok :)
//02:19 vampifrog: so, probably the first clip would be active at tick 2 and 3, and the second clip would be active at ticks 4 and 5, and at 6 there should be none active
//02:19 AndroidDAW: ok
//02:20 vampifrog: and when you move between tick 3 and 4, you will detect that the first clip ended
//02:20 vampifrog: because at tick 3 it was active, and at tick 4 it is no longer active
//02:21 AndroidDAW: so, assuming the timeline was for example, written in a plugin format, it would first check if it has any clips, and then obtain the clip list from the host and process that accordingly right?
//02:25 vampifrog has left IRC (Ping timeout: 268 seconds)
//02:26 vampirefrog has joined (~vampirefr@unaffiliated/vampirefrog)
//02:27 AndroidDAW: for example, something like, for (Track track : tracks) { if (track.hasEvent(tick); for (Clip clip : host.clips) { for (MIDI midi : clip.midi) { /* process MIDI note data */ } }
//02:27 AndroidDAW: for (Track track : tracks) { if (track.hasEvent(tick)) { for (Clip clip : host.clips) { for (MIDI midi : clip.midi) { /* process MIDI note data */ } } }    *
//02:28 AndroidDAW: wait no
//02:28 AndroidDAW: for (Track track : tracks) { if (track.hasEvent(tick)) { for (Clip clip : getClips(tick)) { for (MIDI midi : clip.midi) { /* process MIDI note data */ } } }
//02:29 AndroidDAW: in which getClips would return a list of Clip's within the current tick
//02:29 AndroidDAW: anyway, something like that right?
//02:29 vampirefrog: you can go through each clip but that will be very inefficient when you have many clips
//02:30 vampirefrog: so you'll probably want some sort of index to make clip lookup faster
//02:30 AndroidDAW: hmm ok
//02:30 vampirefrog: you can also make it like an iterator so you don't need to allocate a list
//02:31 vampirefrog: so in other words, you'd need a function like Track::getNextClipAtTick(int tick, int clip = -1);
//02:31 vampirefrog: and when clip == -1, it would return the first available clip, then you'd give it its index to find the next one
//02:31 vampirefrog: because you don't really need a list, you just need to iterate through the clips
//02:33 vampirefrog: or you can even create a clip iterator class that would hold the state
//02:33 vampirefrog: initially you can just loop through all the clips but at some point you'll want to have some sort of clip index to make lookup a lot faster
//02:33 vampirefrog: you can test by generating hundreds of random clips and see how it moves
//02:35 vampirefrog: I have to go, good luck

class Pattern {
public:
    PianoRoll pianoRoll;
    Channel_Generator * channelReference = nullptr;

    static Pattern * cast(void * pointer) {
        return static_cast<Pattern *>(pointer);
    }
};


#endif //AAUDIOTRACK_PATTERN_H
