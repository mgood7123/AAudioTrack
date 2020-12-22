package smallville7123.aaudiotrack2;

import smallville7123.UI.SequencerView;

public class Track {
    AAudioTrack2 DAWReference = null;
    public long nativeTrack = 0;
    public int currentNativeResolution = 0;
    public int currentViewResolution = 0;

    public boolean[] getData() {
        return new boolean[1];
    }

    public void setNativeResolution(int size) {
        if (currentNativeResolution != size) {
            DAWReference.setTrackGridResolution(nativeTrack, size);
            currentNativeResolution = size;
        }
    }

    public void setViewResolution(int size) {
        if (currentViewResolution != size) {
            currentViewResolution = size;
        }
    }

    public void setTrackData() {
        DAWReference.setTrackData(nativeTrack, getData());
    }

    public void bindPatternListToTrack(PatternList patternList) {
        DAWReference.bindPatternListToTrack(patternList.nativePatternList, nativeTrack);
    }
}
