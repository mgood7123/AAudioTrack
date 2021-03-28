package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import smallville7123.aaudiotrack2.R;

@SuppressLint("AppCompatCustomView")
public class example extends EditText {

    String preview;
    String realText;

    OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) setText(realText);
            else needsRefresh = true;
        }
    };

    public void refresh() {
        if (isFocused()) setText(realText);
        else realText = getText().toString();
        preview = "A";
        if (!isFocused()) setText(preview);
        needsRefresh = false;
    }

    // Default constructor override
    public example(Context context) {
        this(context, null);
    }

    // Default constructor when inflating from XML file
    public example(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    // Default constructor override
    public example(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public example(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setSingleLine(true);
        setImeOptions(EditorInfo.IME_ACTION_GO);
        setOnFocusChangeListener(onFocusChangeListener);
    }

    boolean needsRefresh = false;

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        needsRefresh = true;
        super.onTextChanged(text, start, before, after);
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            needsRefresh = true;
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Resize text after measuring
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed || needsRefresh) {
            refresh();
        }
        super.onLayout(changed, left, top, right, bottom);
    }
}