package smallville7123.aaudiotrack2;

public class Track {
    AAudioTrack2 DAWReference = null;
    public long nativeChannel = 0;
    public long nativeTrack = 0;
    public int currentNativeResolution = 0;
    public int currentViewResolution = 0;

    public boolean[] getData() {
        return new boolean[1];
    }

    public long newChannel() {
        nativeChannel = DAWReference.newChannel();
        DAWReference.bindChannelToTrack(nativeChannel, nativeTrack);
        return nativeChannel;
    }

    public long newSamplerChannel() {
        nativeChannel = DAWReference.newSamplerChannel();
        DAWReference.bindChannelToTrack(nativeChannel, nativeTrack);
        return nativeChannel;
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
}
