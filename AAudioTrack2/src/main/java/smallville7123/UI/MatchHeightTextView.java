package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * Text view that auto adjusts text size to fit within the view height.
 */
@SuppressLint("AppCompatCustomView")
public class MatchHeightTextView extends TextView {

    // Our ellipsis string
    private static final String mEllipsis = "...";

    // Flag for text and/or size changes to force a resize
    private boolean mNeedsResize = false;

    // Text view line spacing multiplier
    private float mSpacingMult = 1.0f;

    // Text view additional line spacing
    private float mSpacingAdd = 0.0f;

    // Add ellipsis to text that overflows at the smallest text size
    private boolean mAddEllipsis = true;

    // Default constructor override
    public MatchHeightTextView(Context context) {
        this(context, null);
    }

    // Default constructor when inflating from XML file
    public MatchHeightTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Default constructor override
    public MatchHeightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        mNeedsResize = true;
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            mNeedsResize = true;
        }
    }

    /**
     * Override the set line spacing to update our internal reference values
     */
    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mSpacingMult = mult;
        mSpacingAdd = add;
    }

    /**
     * Set flag to add ellipsis to text that overflows at the smallest text size
     * @param addEllipsis
     */
    public void setAddEllipsis(boolean addEllipsis) {
        mAddEllipsis = addEllipsis;
    }

    /**
     * Return flag to add ellipsis to text that overflows at the smallest text size
     * @return
     */
    public boolean getAddEllipsis() {
        return mAddEllipsis;
    }

    /**
     * Resize text after measuring
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed || mNeedsResize) {
            int widthLimit = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
            int heightLimit = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
            resizeText(widthLimit, heightLimit);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    float resizeTextInternal(int width, int height, CharSequence text, TextPaint textPaint, float oldTextSize) {
        float targetTextSize = oldTextSize;

        // Get the required text height
        int textHeight = getTextHeight(text, textPaint, width, oldTextSize);

        // if we are larger than our text view, incrementally try smaller sizes until we fit
        while (textHeight > height) {
            textHeight = getTextHeight(text, textPaint, width, --targetTextSize);
        }
        // if we are smaller than our text view, incrementally try larger sizes until we fit
        while (textHeight < height) {
            textHeight = getTextHeight(text, textPaint, width, ++targetTextSize);
        }

        return targetTextSize;
    }

    /**
     * tests to resize the text size with specified width and height
     * @param width
     * @param height
     */
    public Float resize(int width, int height, CharSequence text, TextPaint textPaint) {
        // Do not resize if the view does not have dimensions or there is no text
        if(text == null || text.length() == 0 || height <= 0 || width <= 0) {
            return null;
        }
        return resizeTextInternal(width, height, text, textPaint, textPaint.getTextSize());
    }

    class ResizeData {
        boolean resizable;
        StaticLayout layout;
        float textWidth;
        float originalTextSize;
        float textSize;
        public void process(int width, int height, CharSequence text, TextPaint textPaint, boolean includepad) {
            Float ts = resize(width, height, text, textPaint);
            if (ts == null) {
                resizable = false;
            } else {
                resizable = true;
                textSize = ts;
                originalTextSize = textPaint.getTextSize();
                textPaint.setTextSize(textSize);
                layout = new StaticLayout(text, textPaint, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, includepad);
                textWidth = textPaint.measureText(text.toString());
                textPaint.setTextSize(originalTextSize);
            }
        }
    }


    /**
     * Resize the text size with specified width and height
     * @param width
     * @param height
     */
    public void resizeText(int width, int height) {
        CharSequence text = getText();
        TextPaint textPaint = getPaint();

        // Store the current text size
        float oldTextSize = textPaint.getTextSize();

        // if only the ellipse fits, then exit early
        boolean onlyRoomForEllipsis = false;
        // if the ellipse cannot fit, then exit early
        boolean noSpace = false;

        String newEllipsis = mEllipsis;
        ResizeData textDataEllipsis = new ResizeData();
        while (true) {
            // see if ellipsis will fit
            textDataEllipsis.process(width, height, newEllipsis, textPaint, false);
            if (textDataEllipsis.resizable) {
                // the ellipsis itself fits, exit loop
                if (textDataEllipsis.textWidth < width) break;

                // either the ellipsis fits partially or there is no room for it to fit,
                // exit early
                onlyRoomForEllipsis = true;
                newEllipsis = newEllipsis.substring(0, newEllipsis.length()-1);
            } else {
                // the ellipsis was unable to fit
                noSpace = true;
                break;
            }
        }

        textPaint.setTextSize(oldTextSize);

        if (noSpace) {
            setText("");
            mNeedsResize = false;
            return;
        }

        if (onlyRoomForEllipsis) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataEllipsis.textSize);
            setLineSpacing(mSpacingAdd, mSpacingMult);
            setText(newEllipsis);
            mNeedsResize = false;
            return;
        }

        // there is at least room for ellipsis

        // see of the text by itself will fit first
        String newText = text.toString();
        ResizeData textDataText = new ResizeData();
        textDataText.process(width, height, newText, textPaint, false);
        if (textDataText.resizable) {
            // the text itself fits, exit loop
            if (textDataText.textWidth < width) {
                textPaint.setTextSize(oldTextSize);
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataText.textSize);
                setLineSpacing(mSpacingAdd, mSpacingMult);
                mNeedsResize = false;
                return;
            }
        }

        // text does not fit
        textPaint.setTextSize(oldTextSize);
        ResizeData textDataMain = new ResizeData();

        while (true) {
            // see if text and ellipsis will fit
            textDataMain.process(width, height, newText + mEllipsis, textPaint, false);
            if (textDataMain.resizable) {
                // the text + ellipsis itself fits, exit loop
                if (textDataMain.textWidth < width) break;
            }
            // the text + ellipsis does not fit, trim text and try again
            newText = newText.substring(0, newText.length()-1);
        }

        textPaint.setTextSize(oldTextSize);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataMain.textSize);
        setText(newText + mEllipsis);

        // Reset force resize flag
        mNeedsResize = false;
    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
        paint.setTextSize(textSize);
        StaticLayout layout = new StaticLayout(source, paint, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, true);
        return layout.getHeight();
    }

}