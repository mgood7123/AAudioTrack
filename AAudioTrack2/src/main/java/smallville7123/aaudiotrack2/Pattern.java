package smallville7123.aaudiotrack2;

/**
 * a Pattern is a sequence of MIDI Events
 * <br>
 * <br>
 *
 * @see PatternGroup
 * @see PatternList
 */
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

    public final long getMidiEventAt(int index) {
        return DAWReference.getMidiEventAt(nativePattern, index);
    }

    public final int getMidiTickAt(long midiEvent) {
        return DAWReference.getMidiTickAt(nativePattern, midiEvent);
    }

    public final int getMidiChannelAt(long midiEvent) {
        return DAWReference.getMidiChannelAt(nativePattern, midiEvent);
    }

    public final int getMidiNumberAt(long midiEvent) {
        return DAWReference.getMidiNumberAt(nativePattern, midiEvent);
    }

    public final int getMidiVelocityAt(long midiEvent) {
        return DAWReference.getMidiVelocityAt(nativePattern, midiEvent);
    }

    public final boolean isMidiEventNote(long midiEvent) {
        return DAWReference.isMidiEventNote(nativePattern, midiEvent);
    }

    public final boolean isMidiEventNoteOn(long midiEvent) {
        return DAWReference.isMidiEventNoteOn(nativePattern, midiEvent);
    }

    public final boolean isMidiEventNoteOff(long midiEvent) {
        return DAWReference.isMidiEventNoteOff(nativePattern, midiEvent);
    }
}
