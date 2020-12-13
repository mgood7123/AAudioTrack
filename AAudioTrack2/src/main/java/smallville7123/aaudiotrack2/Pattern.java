package smallville7123.aaudiotrack2;

public class Pattern {
    AAudioTrack2 DAWReference;
    public long nativeChannel;
    public long nativePattern;

    public boolean[] getData() {
        return new boolean[1];
    }

    public long newSamplerChannel() {
        nativeChannel = DAWReference.newSamplerChannel();
        DAWReference.bindChannelToPattern(nativeChannel, nativePattern);
        return nativeChannel;
    }

    public void setNoteData() {
        DAWReference.setNoteData(nativePattern, getData());
    }
}
