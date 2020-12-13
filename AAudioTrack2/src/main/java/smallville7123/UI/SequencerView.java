package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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

    LinearLayout rows;
    Context mContext;
    float rowWidth;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.sequencer, 0, 0);
            rowWidth = attributes.getDimension(R.styleable.sequencer_rowWidth, Float.NaN);
            attributes.recycle();
        } else {
            rowWidth = Float.NaN;
        }
        rows = new LinearLayout(context);
        rows.setOrientation(VERTICAL);
        addView(rows);
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
        pattern.resize(8);
        return pattern;
    }

    public class PatternList extends smallville7123.aaudiotrack2.PatternList<Pattern> {
        Pattern newPattern(Context context, String label) {
            Pattern pattern = newPattern(new Pattern());

            pattern.mContext = context;
            pattern.row = new LinearLayout(context);
            pattern.row.setOrientation(HORIZONTAL);
            pattern.length = 0;

            patternArrayList.add(pattern);
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
            }, Layout.wrapContent);
            row.addView(new Button(mContext) {
                {
                    setText(label);
                }
            }, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 3f));

            row.addView(pattern.row, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));

            if (rowWidth == Float.NaN) {
                rows.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
            } else {
                rows.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, (int) rowWidth));
            }
            return pattern;
        }
    }

    public class Pattern extends smallville7123.aaudiotrack2.Pattern {
        ArrayList<CompoundButton> compoundButtons = new ArrayList<>();
        LinearLayout row;
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

        void resize(int size) {
            if (size == length) return;
            if (size > length) {
                for (int i = length; i < size; i++) {
                    CompoundButton compoundButton = new ToggleRadioButton(mContext);
                    compoundButton.setOnCheckedChangeListener((b0, b1) -> {
                        setNoteData();
                    });
                    compoundButtons.add(compoundButton);
                    row.addView(compoundButton, Layout.wrapContent);
                    length++;
                }
            } else {
                for (int i = (length-1); i > (size-1); i--) {
                    compoundButtons.remove(i);
                    row.removeViewAt(i);
                    length--;
                }
            }
        }
    }
}
