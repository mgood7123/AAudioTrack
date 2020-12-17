package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import smallville7123.aaudiotrack2.AAudioTrack2;
import smallville7123.aaudiotrack2.PatternGroup;
import smallville7123.aaudiotrack2.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

@SuppressLint("AppCompatCustomView")
public class SequencerView extends FrameLayout {
    public SequencerView(Context context) {
        super(context);
        init(context, null);
    }

    public SequencerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SequencerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SequencerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    GridView channelGrid;
    Context mContext;
    int channels;
    float channelHeight;
    boolean fitChannelsToView;
    int notes;
    float noteWidth;
    boolean fitNotesToView;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.sequencer, 0, 0);
            channels = attributes.getInteger(R.styleable.sequencer_channels, 4);
            channelHeight = attributes.getDimension(R.styleable.sequencer_channelHeight, Float.NaN);
            fitChannelsToView = attributes.getBoolean(R.styleable.sequencer_fitChannelsToView, true);
            notes = attributes.getInteger(R.styleable.sequencer_notes, 8);
            noteWidth = attributes.getDimension(R.styleable.sequencer_noteWidth, Float.NaN);
            fitNotesToView = attributes.getBoolean(R.styleable.sequencer_fitNotesToView, true);
            attributes.recycle();
        } else {
            channels = 4;
            channelHeight = Float.NaN;
            fitChannelsToView = true;
            notes = 8;
            noteWidth = Float.NaN;
            fitNotesToView = true;
        }
        channelGrid = new GridView(context);
        channelGrid.setOrientation(VERTICAL);
        channelGrid.setRows(channels);
        addView(channelGrid);
        if (isInEditMode()) {
            PatternList list = newPatternList(null);
            addRow(list, "1");
            addRow(list, "2");
            addRow(list, "3");
            addRow(list, "4");
        }
    }

    AAudioTrack2 DAW;

    public void setDAW(AAudioTrack2 DAW) {
        this.DAW = DAW;
    }

    PatternGroup<PatternList> group = null;

    public PatternList newPatternList(AAudioTrack2 audioTrack) {
        if (group == null) {
            group = new PatternGroup<>(audioTrack);
        }
        return group.newPatternList(new PatternList());
    }

    public void removePatternList(PatternList patternList) {
        group.removePatternList(patternList);
    }

    public Pattern addRow(PatternList patternList, String label) {
        Pattern pattern = patternList.newPattern(mContext, label);
        pattern.setResolution(notes);
        pattern.setMaxLength(notes);
        return pattern;
    }

    public class PatternList extends smallville7123.aaudiotrack2.PatternList<Pattern> {
        Pattern newPattern(Context context, String label) {
            Pattern pattern = newPattern(new Pattern());

            pattern.mContext = context;
            pattern.noteGrid = new GridView(context) {
                @Override
                public void onScrolled(int dx, int dy) {
                    super.onScrolled(dx, dy);
                    if (!pattern.scrolling) {
                        pattern.scrolling = true;
                        for (Pattern pattern1 : patternArrayList) {
                            if (!pattern1.scrolling) {
                                pattern1.scrolling = true;
                                pattern1.noteGrid.scrollBy(dx, dy);
                                pattern1.scrolling = false;
                            }
                        }
                        pattern.scrolling = false;
                    }
                }
            };
            pattern.noteGrid.setOrientation(GridView.HORIZONTAL);
            pattern.noteGrid.setRows(1);
            pattern.length = 0;
            LinearLayout row = new LinearLayout(mContext);
            row.setOrientation(HORIZONTAL);
            row.addView(new ToggleRadioButton(mContext) {
                {
                    setChecked(true);
                    setTooltipText("Tap to disable this channel");
                    setOnCheckedChangeListener((unused, checked) -> {
                        setTooltipText(
                                "Tap to " +
                                        (checked ? "disable" : "enable") +
                                        " this channel"
                        );
                    });
                }
            }, Constants.wrapContent);
            row.addView(new Button(mContext) {
                {
                    setText(label);
                }
            }, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 3f));

            row.addView(pattern.noteGrid, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));

            if (fitChannelsToView || channelHeight == Float.NaN) {
                channelGrid.autoSizeRow = true;
            } else {
                channelGrid.autoSizeRow = false;
                channelGrid.rowHeight = (int) channelHeight;
            }
            channelGrid.data.add(row);
            channelGrid.adapter.notifyDataSetChanged();
            return pattern;
        }
    }

    public class Pattern extends smallville7123.aaudiotrack2.Pattern {
        ArrayList<CompoundButton> compoundButtons = new ArrayList<>();
        public boolean scrolling = false;
        GridView noteGrid;
        int maxLength;
        int length;
        Context mContext;

        @Override
        public boolean[] getData() {
            boolean[] data = new boolean[compoundButtons.size()];
            for (int i = 0; i < compoundButtons.size(); i++) {
                data[i] = compoundButtons.get(i).isChecked();
            }
            return data;
        }

        @Override
        public void setResolution(int size) {
            if (size == length) return;
            super.setResolution(size);

            // change this value to set the actual number
            // of displayed notes before the user will need
            // to scroll
            noteGrid.setColumns(size);
            length = size;
        }

        public void setMaxLength(int size) {
            if (size == maxLength) return;
            if (size > maxLength) {
                for (int i = maxLength; i < size; i++) {
                    ToggleButton note = new ToggleButton(mContext);
                    note.setBackgroundResource(R.drawable.toggle);
                    note.setTextOn("");
                    note.setTextOff("");
                    note.setText("");
                    note.setOnCheckedChangeListener((b0, b1) -> {
                        setNoteData();
                    });
                    compoundButtons.add(note);
                    if (fitNotesToView || noteWidth == Float.NaN) {
                        noteGrid.autoSizeColumn = true;
                    } else {
                        noteGrid.autoSizeColumn = false;
                        noteGrid.columnWidth = (int) noteWidth;
                    }
                    noteGrid.data.add(note);
                    maxLength++;
                }
            } else {
                for (int i = (maxLength-1); i > (size-1); i--) {
                    compoundButtons.remove(i);
                    noteGrid.data.remove(noteGrid.data.size()-1);
                    maxLength--;
                }
            }
            noteGrid.adapter.notifyDataSetChanged();
        }
    }
}
