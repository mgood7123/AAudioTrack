package smallville7123.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import smallville7123.UI.ScrollBarView.CanvasDrawer;
import smallville7123.UI.ScrollBarView.CanvasView;
import smallville7123.UI.ScrollBarView.Scroller;

/**
 * FL pattern view appears to map by key (midi number):
 *
 * the FL pattern view has a maximum range of C0 to B10
 * however it can be resized
 *
 * the minimum size is 12 rows - 1 full octave, C to B
 *
 * when a row goes from B0 to C1, its note position changes from row 12 to row 1
 *
 * when resized, the minimum size is still 12
 *
 * its max size supports 132 rows: C0 to B10
 *
 * the row range appears to consist of a min max range
 *
 * the top most row is set to the highest note, and the bottom most row to the bottom most note
 */
public class PatternView extends CanvasView {

    private static final String TAG = "PatternView";

    public PatternView(@NonNull Context context) {
        super(context);
    }

    public PatternView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PatternView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PatternView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    SequencerView.PatternList patternList;

    public void setPatternList(SequencerView.PatternList patternList) {
        this.patternList = patternList;
    }

    @Override
    public void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createCanvas(w, h);
    }

    static final int NO_POS = -1;
    static final int samplesPerPixel = 48*3;
    static final double zoom = samplesPerPixel;

    class NoteData {
        int noteStart = NO_POS;
        int noteStartMidiChannel = -1;
        int noteStartMidiNumber = -1;
        int noteStartMidiVelocity = -1;

        int noteEnd = NO_POS;
        int noteEndMidiChannel = -1;
        int noteEndMidiNumber = -1;
        int noteEndMidiVelocity = -1;
    }

    int getNoteStart(SequencerView.Pattern pattern, long midiEvent) {
        if (pattern.isMidiEventNoteOn(midiEvent)) {
            double sample_position = pattern.getMidiTickAt(midiEvent);
            double pixelDouble = Math.floor(sample_position / zoom);
            return (int) pixelDouble;
        }
        return NO_POS;
    }

    int getNoteMidiChannel(SequencerView.Pattern pattern, long midiEvent) {
        if (pattern.isMidiEventNote(midiEvent)) return pattern.getMidiChannelAt(midiEvent);
        return NO_POS;
    }

    int getNoteMidiNumber(SequencerView.Pattern pattern, long midiEvent) {
        if (pattern.isMidiEventNote(midiEvent)) return pattern.getMidiNumberAt(midiEvent);
        return NO_POS;
    }

    int getNoteMidiVelocity(SequencerView.Pattern pattern, long midiEvent) {
        if (pattern.isMidiEventNote(midiEvent)) return pattern.getMidiVelocityAt(midiEvent);
        return NO_POS;
    }

    int getNoteEnd(SequencerView.Pattern pattern, long midiEvent) {
        if (pattern.isMidiEventNoteOff(midiEvent)) {
            double sample_position = pattern.getMidiTickAt(midiEvent);
            double pixelDouble = Math.floor(sample_position / zoom);
            return (int) pixelDouble;
        }
        return NO_POS;
    }

    @Override
    protected void onDrawCanvas(CanvasDrawer canvas) {
        canvas.clear();
        NoteData noteData = new NoteData();
        ArrayList<NoteData> noteDataArrayList = new ArrayList<>();
        for (SequencerView.Pattern pattern : patternList.patternArrayList) {
            if (pattern != null) {
                Scroller<CanvasDrawer> viewScroller = canvas.getViewScroller();
                int mScrollX = viewScroller.mScrollX;
                for (int i = mScrollX; i < canvas.getWidth() + mScrollX; i++) {
                    long midiEvent = pattern.getMidiEventAt(i);
                    if (midiEvent != 0) {
                        if (pattern.isMidiEventNote(midiEvent)) {
                            if (noteData.noteStart == NO_POS && noteData.noteEnd == NO_POS) {
                                // get note start only when we have no start and end
                                noteData.noteStart = getNoteStart(pattern, midiEvent);
                                noteData.noteStartMidiChannel = getNoteMidiChannel(pattern, midiEvent);
                                noteData.noteStartMidiNumber = getNoteMidiNumber(pattern, midiEvent);
                                noteData.noteStartMidiVelocity = getNoteMidiVelocity(pattern, midiEvent);
                            }
                            if (noteData.noteStart != NO_POS && noteData.noteEnd == NO_POS) {
                                // get note end only when we have a start and no end
                                noteData.noteEnd = getNoteEnd(pattern, midiEvent);
                                noteData.noteEndMidiChannel = getNoteMidiChannel(pattern, midiEvent);
                                noteData.noteEndMidiNumber = getNoteMidiNumber(pattern, midiEvent);
                                noteData.noteEndMidiVelocity = getNoteMidiVelocity(pattern, midiEvent);
                                if (noteData.noteEnd != NO_POS) {
                                    noteDataArrayList.add(noteData);
                                    noteData = new NoteData();
                                }
                            }
                        }
                    }
                }
            }

            if (!noteDataArrayList.isEmpty()) {

                NoteData[] list = noteDataArrayList.toArray(new NoteData[0]);
                canvas.savePaint();
                canvas.setPaint(CanvasDrawer.paintRed);

                Arrays.sort(
                        list,
                        (o1, o2) -> Integer.compare(o1.noteStartMidiNumber, o2.noteStartMidiNumber)
                );

                int windowHeight = canvas.getHeight();

                int min = list[0].noteStartMidiNumber;
                int max = list[list.length - 1].noteStartMidiNumber;

                int rows = (max - min) + 1;

                Log.d(TAG, "rows = [" + (rows) + "]");
                if (rows < 12) rows = 12;

                int rowHeight = windowHeight / rows;
                for (NoteData data : noteDataArrayList) {
                    int top = rowHeight * data.noteStartMidiNumber;
                    canvas.drawRectAbsoluteLocation(data.noteStart, windowHeight - top, data.noteEnd, windowHeight - (top + rowHeight));
                }
                canvas.restorePaint();
            }
        }
        invalidate();
    }
}