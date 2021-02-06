package smallville7123.aaudiotrack2;

import java.util.ArrayList;

/**
 * a Pattern Group is a list of Pattern Lists
 * <br>
 * <br>
 * this is the actual Pattern List
 * @see PatternList
 * @see Pattern
 */
public class PatternGroup<P extends PatternList<? extends Pattern>> {
    public ArrayList<P> patternListArrayList = new ArrayList<>();
    AAudioTrack2 DAWReference;

    public PatternGroup(AAudioTrack2 instance) {
        DAWReference = instance;
    }

    public P newPatternList(P patternList) {
        patternList.DAWReference = DAWReference;
        if (DAWReference != null) {
            patternList.nativePatternList = DAWReference.createPatternList();
        }
        patternListArrayList.add(patternList);
        return patternList;
    }

    public void removePatternList(P patternList) {
        if (DAWReference != null) {
            DAWReference.deletePatternList(patternList.nativePatternList);
        }
        patternListArrayList.remove(patternList);
    }
}
