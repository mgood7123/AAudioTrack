package smallville7123.aaudiotrack2;

import java.util.ArrayList;

public class TrackGroup<P extends TrackList<? extends Track>> {
    public ArrayList<P> trackListArrayList = new ArrayList<>();
    AAudioTrack2 DAWReference;

    public TrackGroup(AAudioTrack2 instance) {
        DAWReference = instance;
    }

    public P newTrackList(P trackList) {
        trackList.DAWReference = DAWReference;
        if (DAWReference != null) {
            trackList.nativeTrackList = DAWReference.createTrackList();
        }
        trackListArrayList.add(trackList);
        return trackList;
    }

    public void removeTrackList(P trackList) {
        if (DAWReference != null) {
            DAWReference.deleteTrackList(trackList.nativeTrackList);
        }
        trackListArrayList.remove(trackList);
    }
}
