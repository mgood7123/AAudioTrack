package smallville7123.UI;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import smallville7123.AndroidDAW.SDK.UI.ScrollBar.CanvasDrawer;
import smallville7123.AndroidDAW.SDK.UI.ScrollBar.CanvasView;
import smallville7123.AndroidDAW.SDK.UI.ScrollBar.Scroller;

public class PianoRollView extends CanvasView {
    public PianoRollView(@NonNull Context context) {
        super(context);
    }

    public PianoRollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PianoRollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PianoRollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    SequencerView.Pattern pattern;

    public void setPattern(SequencerView.Pattern pattern) {
        this.pattern = pattern;
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
    static final int rowHeight = 200;
    static final int samplesPerPixel = 48*2;
    static final double zoom = samplesPerPixel;
    int noteStart = NO_POS;
    int noteEnd = NO_POS;

    int getNoteStart(long midiEvent) {
        if (pattern.isMidiEventNoteOn(midiEvent)) {
            double sample_position = pattern.getMidiTickAt(midiEvent);
            double pixelDouble = Math.floor(sample_position / zoom);
            return (int) pixelDouble;
        }
        return NO_POS;
    }

    int getNoteEnd(long midiEvent) {
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
        if (pattern != null) {
            canvas.savePaint();
            canvas.setPaint(CanvasDrawer.paintRed);
            Scroller<CanvasDrawer> viewScroller = canvas.getViewScroller();
            int mScrollX = viewScroller.mScrollX;
            for (int i = mScrollX; i < canvas.getWidth() + mScrollX; i++) {
                long midiEvent = pattern.getMidiEventAt(i);
                if (midiEvent != 0) {
                    if (pattern.isMidiEventNote(midiEvent)) {
                        if (noteStart == NO_POS && noteEnd == NO_POS) {
                            // get note start only when we have no start and end
                            noteStart = getNoteStart(midiEvent);
                        }
                        if (noteStart != NO_POS && noteEnd == NO_POS) {
                            // get note end only when we have a start and no end
                            noteEnd = getNoteEnd(midiEvent);
                        }

                        if (noteStart != NO_POS && noteEnd != NO_POS) {
                            // ignore row index for now
                            // note pitch changing is not implemented
                            canvas.drawRectAbsoluteLocation(noteStart, 0 * rowHeight, noteEnd, rowHeight);
                            noteStart = NO_POS;
                            noteEnd = NO_POS;
                        }
                    }
                }
            }
            canvas.restorePaint();
        }
        noteStart = NO_POS;
        noteEnd = NO_POS;
        invalidate();
    }
}