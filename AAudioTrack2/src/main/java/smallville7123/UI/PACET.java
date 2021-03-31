package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.MultiAutoCompleteTextView;

import androidx.annotation.CallSuper;

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
    private boolean isSettingPreview = false;

    public class OnPreviewListener {
        /**
         * @param active if the preview is active or not
         */
        void onPreview(boolean active) {
            // do nothing
        }

        @CallSuper
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

        @CallSuper
        void refreshEnd() {
            if (!isFocused()) {
                isSettingPreview = true;
                setText(preview);
                isSettingPreview = false;
            }
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
        this(context, attrs, R.attr.autoCompleteTextViewStyle);
    }

    // Default constructor override
    public PACET(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public PACET(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    /**
     * a version of TextWatcher that gets called when the main text changes
     */
    private abstract class TextWatcher implements android.text.TextWatcher {
        final boolean shouldCall() {
            return !isSettingPreview;
        }
    }

    /**
     * a version of TextWatcher that gets called when the preview text changes
     */
    private abstract class PreviewTextWatcher implements android.text.TextWatcher {
        final boolean shouldCall() {
            return isSettingPreview;
        }
    }

    /**
     * converts a {@link android.text.TextWatcher} into a {@link PACET.TextWatcher}
     * @param textWatcher the {@link android.text.TextWatcher} to convert
     * @return a new {@link PACET.TextWatcher}
     */
    public PACET.TextWatcher convertToTextWatcher(android.text.TextWatcher textWatcher) {
        return new PACET.TextWatcher() {
            final android.text.TextWatcher textWatcher_ = textWatcher;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (shouldCall()) textWatcher_.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (shouldCall()) textWatcher_.onTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (shouldCall()) textWatcher_.afterTextChanged(s);
            }
        };
    }

    /**
     * converts a {@link android.text.TextWatcher} into a {@link PACET.PreviewTextWatcher}
     * @param textWatcher the {@link android.text.TextWatcher} to convert
     * @return a new {@link PACET.PreviewTextWatcher}
     */
    public PACET.PreviewTextWatcher convertToPreviewTextWatcher(android.text.TextWatcher textWatcher) {
        return new PACET.PreviewTextWatcher() {
            final android.text.TextWatcher textWatcher_ = textWatcher;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (shouldCall()) textWatcher_.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (shouldCall()) textWatcher_.onTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (shouldCall()) textWatcher_.afterTextChanged(s);
            }
        };
    }

    public void addTextChangedListener(PACET.TextWatcher watcher) {
        super.addTextChangedListener(watcher);
    }

    public void addTextChangedListener(PACET.PreviewTextWatcher watcher) {
        super.addTextChangedListener(watcher);
    }

    /**
     * This method is deprecated. <br><br>
     * Please call
     * {@link #addTextChangedListener(PACET.TextWatcher)} or
     * {@link #addTextChangedListener(PACET.PreviewTextWatcher)} instead
     *
     * @see #convertToTextWatcher(android.text.TextWatcher)
     * @see #convertToPreviewTextWatcher(android.text.TextWatcher)
     */
    @Override
    @Deprecated
    final public void addTextChangedListener(android.text.TextWatcher watcher) {
        super.addTextChangedListener(watcher);
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
    public void clearFocus() {
        dismissDropDown();
        super.clearFocus();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        Log.d(TAG, "onFocusChanged() called with: focused = [" + focused + "], direction = [" + direction + "], previouslyFocusedRect = [" + previouslyFocusedRect + "]");
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        inRefresh = true;
        if (focused) setText(realText);
        else needsRefresh = true;
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

    
    
    
    
    
    
    
    
    

    private MultiAutoCompleteTextView.Tokenizer mTokenizer;

    /**
     * Sets the Tokenizer that will be used to determine the relevant
     * range of the text where the user is typing.
     */
    public void setTokenizer(MultiAutoCompleteTextView.Tokenizer t) {
        mTokenizer = t;
    }

    /**
     * Instead of filtering on the entire contents of the edit box,
     * this subclass method filters on the range from
     * {@link MultiAutoCompleteTextView.Tokenizer#findTokenStart} to {@link #getSelectionEnd}
     * if the length of that range meets or exceeds {@link #getThreshold}.
     */
    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mTokenizer == null) {
            super.performFiltering(text, keyCode);
            return;
        }

        if (enoughToFilter()) {
            int end = getSelectionEnd();
            int start = mTokenizer.findTokenStart(text, end);

            performFiltering(text, start, end, keyCode);
        } else {
            dismissDropDown();

            Filter f = getFilter();
            if (f != null) {
                f.filter(null);
            }
        }
    }

    /**
     * Instead of filtering whenever the total length of the text
     * exceeds the threshhold, this subclass filters only when the
     * length of the range from
     * {@link MultiAutoCompleteTextView.Tokenizer#findTokenStart} to {@link #getSelectionEnd}
     * meets or exceeds {@link #getThreshold}.
     */
    @Override
    public boolean enoughToFilter() {
        if (mTokenizer == null) return super.enoughToFilter();

        Editable text = getText();

        int end = getSelectionEnd();
        if (end < 0 || mTokenizer == null) {
            return false;
        }

        int start = mTokenizer.findTokenStart(text, end);

        if (end - start >= getThreshold()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Instead of validating the entire text, this subclass method validates
     * each token of the text individually.  Empty tokens are removed.
     */
    @Override
    public void performValidation() {
        if (mTokenizer == null) {
            super.performValidation();
            return;
        }

        Validator v = getValidator();

        if (v == null || mTokenizer == null) {
            return;
        }

        Editable e = getText();
        int i = getText().length();
        while (i > 0) {
            int start = mTokenizer.findTokenStart(e, i);
            int end = mTokenizer.findTokenEnd(e, start);

            CharSequence sub = e.subSequence(start, end);
            if (TextUtils.isEmpty(sub)) {
                e.replace(start, i, "");
            } else if (!v.isValid(sub)) {
                e.replace(start, i,
                        mTokenizer.terminateToken(v.fixText(sub)));
            }

            i = start;
        }
    }

    /**
     * <p>Starts filtering the content of the drop down list. The filtering
     * pattern is the specified range of text from the edit box. Subclasses may
     * override this method to filter with a different pattern, for
     * instance a smaller substring of <code>text</code>.</p>
     */
    protected void performFiltering(CharSequence text, int start, int end,
                                    int keyCode) {
        getFilter().filter(text.subSequence(start, end), this);
    }

    /**
     * <p>Performs the text completion by replacing the range from
     * {@link MultiAutoCompleteTextView.Tokenizer#findTokenStart} to {@link #getSelectionEnd} by the
     * the result of passing <code>text</code> through
     * {@link MultiAutoCompleteTextView.Tokenizer#terminateToken}.
     * In addition, the replaced region will be marked as an AutoText
     * substition so that if the user immediately presses DEL, the
     * completion will be undone.
     * Subclasses may override this method to do some different
     * insertion of the content into the edit box.</p>
     *
     * @param text the selected suggestion in the drop down list
     */
    @Override
    protected void replaceText(CharSequence text) {
        if (mTokenizer == null) {
            super.replaceText(text);
            return;
        }

        clearComposingText();

        int end = getSelectionEnd();
        int start = mTokenizer.findTokenStart(getText(), end);

        Editable editable = getText();
        String original = TextUtils.substring(editable, start, end);

        QwertyKeyListener.markAsReplaced(editable, start, end, original);
        editable.replace(start, end, mTokenizer.terminateToken(text));
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return PACET.class.getName();
    }
}