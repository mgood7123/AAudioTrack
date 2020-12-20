package smallville7123.aaudiotrack2;

public class Pattern {
    AAudioTrack2 DAWReference = null;
    public long nativeChannel = 0;
    public long nativePattern = 0;
    public int currentNativeResolution = 0;
    public int currentViewResolution = 0;

    public boolean[] getData() {
        return new boolean[1];
    }

    public long newChannel() {
        nativeChannel = DAWReference.newChannel();
        DAWReference.bindChannelToPattern(nativeChannel, nativePattern);
        return nativeChannel;
    }

    public long newSamplerChannel() {
        nativeChannel = DAWReference.newSamplerChannel();
        DAWReference.bindChannelToPattern(nativeChannel, nativePattern);
        return nativeChannel;
    }

    public void setNativeResolution(int size) {
        if (currentNativeResolution != size) {
            DAWReference.setPatternGridResolution(nativePattern, size);
            currentNativeResolution = size;
        }
    }

    public void setViewResolution(int size) {
        if (currentViewResolution != size) {
            currentViewResolution = size;
        }
    }

    public void setNoteData() {
        DAWReference.setNoteData(nativePattern, getData());
    }
}
