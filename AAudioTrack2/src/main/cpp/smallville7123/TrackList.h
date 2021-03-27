//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_TRACKLIST_H
#define AAUDIOTRACK_TRACKLIST_H


#include "Track.h"

class TrackList {
public:
    Rack<Track> rack;

    Track *newTrack() {
        return rack.newType();
    }

    void removeTrack(Track * track) {
        rack.removeType(track);
    }
};


#endif //AAUDIOTRACK_TRACKLIST_H
