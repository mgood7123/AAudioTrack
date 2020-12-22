package smallville7123.aaudiotrack2;

import java.util.ArrayList;

public class TrackList<P extends Track> {
    public ArrayList<P> trackArrayList = new ArrayList<>();
    AAudioTrack2 DAWReference;
    public long nativeTrackList = 0;

    public P newTrack(P track) {
        track.DAWReference = DAWReference;
        if (DAWReference != null) {
            track.nativeTrack = DAWReference.createTrack(nativeTrackList);
        }
        trackArrayList.add(track);
        return track;
    }

    public void removeTrack(P track) {
        if (DAWReference != null) {
            DAWReference.deleteTrack(nativeTrackList, track.nativeTrack);
        }
        trackArrayList.remove(track);
    }

    public boolean[] getData() {
        ArrayList<Boolean> data = new ArrayList<>();
        for (int i = 0; i < trackArrayList.size(); i++) {
            boolean[] trackData = trackArrayList.get(i).getData();
            for (boolean value : trackData) {
                data.add(value);
            }
        }
        boolean[] data_ = new boolean[data.size()];
        for (int i = 0; i < data.size(); i++) {
            data_[i] = data.get(i);
        }
        return data_;
    }
}
