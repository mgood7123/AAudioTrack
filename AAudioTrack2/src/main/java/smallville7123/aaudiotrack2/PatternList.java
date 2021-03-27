package smallville7123.aaudiotrack2;

import java.util.ArrayList;

/**
 * a Pattern List is a list of Patterns, which is a sequence of MIDI Events
 * <br>
 * <br>
 * this is sub Pattern List, and is an abstract list of Patterns
 * <br>
 * <br>
 * this is typically used in Sequencers to display a pattern for each channel,
 * and should not be considered as an actual PatternList, see {@link PatternGroup} instead
 * @see PatternGroup
 * @see Pattern
 */
public class PatternList<P extends Pattern> {
    public ArrayList<P> patternArrayList = new ArrayList<>();
    AAudioTrack2 DAWReference;
    public long nativePatternList;

    public P newPattern(P pattern) {
        pattern.DAWReference = DAWReference;
        if (DAWReference != null) {
            pattern.nativePattern = DAWReference.createPattern(nativePatternList);
        }
        patternArrayList.add(pattern);
        return pattern;
    }

    public void removePattern(P pattern) {
        if (DAWReference != null) {
            DAWReference.deletePattern(nativePatternList, pattern.nativePattern);
        }
        patternArrayList.remove(pattern);
    }

    public boolean[] getData() {
        ArrayList<Boolean> data = new ArrayList<>();
        for (int i = 0; i < patternArrayList.size(); i++) {
            boolean[] patternData = patternArrayList.get(i).getData();
            for (boolean value : patternData) {
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
