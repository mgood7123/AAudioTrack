package smallville7123.aaudiotrack2;

import java.util.ArrayList;

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
