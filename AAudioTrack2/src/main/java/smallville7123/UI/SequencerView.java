package smallville7123.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import smallville7123.aaudiotrack2.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

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
        addRow("1");
        addRow("2");
        addRow("3");
        addRow("4");
    }

    void addRow(String label) {
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

        LinearLayout pattern = new LinearLayout(mContext);
        pattern.addView(new ToggleRadioButton(mContext), Layout.wrapContent);
        pattern.addView(new ToggleRadioButton(mContext), Layout.wrapContent);
        pattern.addView(new ToggleRadioButton(mContext), Layout.wrapContent);
        pattern.addView(new ToggleRadioButton(mContext), Layout.wrapContent);
        pattern.addView(new ToggleRadioButton(mContext), Layout.wrapContent);
        pattern.addView(new ToggleRadioButton(mContext), Layout.wrapContent);
        pattern.addView(new ToggleRadioButton(mContext), Layout.wrapContent);
        pattern.addView(new ToggleRadioButton(mContext), Layout.wrapContent);
        row.addView(pattern, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));

        if (rowWidth == Float.NaN) {
            rows.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        } else {
            rows.addView(row, new LinearLayout.LayoutParams(MATCH_PARENT, (int) rowWidth));
        }
    }
}
