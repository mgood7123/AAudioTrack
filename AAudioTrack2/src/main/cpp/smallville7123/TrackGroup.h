//
// Created by Matthew Good on 9/12/20.
//

#ifndef AAUDIOTRACK_TRACKGROUP_H
#define AAUDIOTRACK_TRACKGROUP_H


#include "TrackList.h"

// Track Groups:
//  track group 1
//   track 1
//    channel 1
//   track 2
//    channel 2
//  track group 2
class TrackGroup {
public:
    Rack<TrackList> rack;

    TrackList *newTrackList() {
        return rack.newType();
    }

    void removeTrackList(TrackList * trackList) {
        rack.removeType(trackList);
    }
};


#endif //AAUDIOTRACK_TRACKGROUP_H
