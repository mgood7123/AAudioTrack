package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import smallville7123.aaudiotrack2.R;

import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * PACET - Preview Auto Complete EditText
 * an EditText that auto adjusts text size to fit on a single line within the view.
 */
@SuppressLint("AppCompatCustomView")
public class PACET extends AutoCompleteTextView {

    private static final String TAG = "PACET";

    private String preview = "";
    private String realText = "";
    private boolean inRefresh = false;

    public class OnPreviewListener {
        /**
         * @param active if the preview is active or not
         */
        void onPreview(boolean active) {
            // do nothing
        }

        void refreshBegin() {
            if (isFocused()) setText(realText);
            else realText = getText().toString();
        }

        /**
         * process your preview string here
         * @param realText a copy of the current real text
         * @param preview a copy of the current preview text
         * @return a string to become the preview
         */
        String refreshProcess(String realText, String preview) {
            return "A";
        }

        void refreshEnd() {
            if (!isFocused()) setText(preview);
            needsRefresh = false;
        }

        public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // do nothing
        }
    }

    private OnPreviewListener mOnPreviewListener = new OnPreviewListener();

    public void setOnPreviewListener(OnPreviewListener mOnPreviewListener) {
        this.mOnPreviewListener = mOnPreviewListener;
    }

    public void refresh() {
        inRefresh = true;
        mOnPreviewListener.refreshBegin();
        String r = new String(realText);
        String p = new String(preview);
        preview = mOnPreviewListener.refreshProcess(r, p);
        mOnPreviewListener.refreshEnd();
        inRefresh = false;
    }

    // Default constructor override
    public PACET(Context context) {
        this(context, null);
    }

    // Default constructor when inflating from XML file
    public PACET(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    // Default constructor override
    public PACET(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public PACET(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    InputMethodManager inputMethodManager;

    // clear focus on keyboard back
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_BACK) clearFocus();
        return super.onKeyPreIme(keyCode, event);
    }

    public void dismissSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        clearFocus();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        Log.d(TAG, "onFocusChanged() called with: focused = [" + focused + "], direction = [" + direction + "], previouslyFocusedRect = [" + previouslyFocusedRect + "]");
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        inRefresh = true;
        if (focused) setText(realText);
        else {

            needsRefresh = true;
        }
        mOnPreviewListener.onPreview(focused);
        inRefresh = false;
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
            inRefresh = true;
            mOnPreviewListener.onLayout(changed, left, top, right, bottom);
            refresh();
            inRefresh = false;
        }
        super.onLayout(changed, left, top, right, bottom);
    }
}