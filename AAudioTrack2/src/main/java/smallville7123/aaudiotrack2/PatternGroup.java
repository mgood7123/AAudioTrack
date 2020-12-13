package smallville7123.aaudiotrack2;

import java.util.ArrayList;

public class PatternGroup<P extends PatternList<? extends Pattern>> {
    public ArrayList<P> patternListArrayList = new ArrayList<>();
    AAudioTrack2 DAWReference;
    long nativePatternList;

    public PatternGroup(AAudioTrack2 instance) {
        DAWReference = instance;
    }

    public P newPatternList(P patternList) {
        patternList.DAWReference = DAWReference;
        patternList.nativePatternList = DAWReference.createPatternList();
        patternListArrayList.add(patternList);
        return patternList;
    }

    public void removePatternList(P patternList) {
        DAWReference.deletePatternList(patternList.nativePatternList);
        patternListArrayList.remove(patternList);
    }

    public void setDAW(AAudioTrack2 audioTrack) {
        DAWReference = audioTrack;
    }
}
