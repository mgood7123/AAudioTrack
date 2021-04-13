package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;

import static android.view.KeyEvent.KEYCODE_BACK;

@SuppressLint("AppCompatCustomView")
public class MultiAutoCompleteTextView extends android.widget.MultiAutoCompleteTextView {

    public MultiAutoCompleteTextView(Context context) {
        super(context);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public MultiAutoCompleteTextView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // clear focus on keyboard back
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_BACK) clearFocus();
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public void clearFocus() {
        dismissDropDown();
        super.clearFocus();
    }

    @Override
    public void showDropDown() {
        if (isFocused()) super.showDropDown();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) super.showDropDown();
    }
}
