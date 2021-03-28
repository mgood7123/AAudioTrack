package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.text.LineBreaker;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.Nullable;

import smallville7123.aaudiotrack2.R;

/**
 * Text view that auto adjusts text size to fit on a single line within the view.
 */
@SuppressLint("AppCompatCustomView")
public class SingleLineAutoSizeTextView extends TextView {

    // Default constructor override
    public SingleLineAutoSizeTextView(Context context) {
        this(context, null);
    }

    // Default constructor when inflating from XML file
    public SingleLineAutoSizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Default constructor override
    public SingleLineAutoSizeTextView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, 0, 0);
    }

    // Default constructor override
    public SingleLineAutoSizeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // set to single line
        super.setSingleLine(true);

        // make sure we do not break text at all
        super.setLines(1);
        super.setMaxLines(1);
        super.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
        super.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);

        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SingleLineAutoSizeTextView, 0, 0);
            int r = attributes.getInt(R.styleable.SingleLineAutoSizeTextView_autoResizeMode, -1);
            switch (r) {
                case -1:
                case 0:
                    resizeMode = ResizeMode.width;
                    break;
                case 1:
                    resizeMode = ResizeMode.height;
                    break;
                case 2:
                    resizeMode = ResizeMode.heightWithEllipsis;
                    break;
            }
            attributes.recycle();
        } else {
            resizeMode = ResizeMode.width;
        }
    }

    public void switchToResizeMode(ResizeMode mode) {
        resizeMode = mode;
    }

    public ResizeMode getResizeMode() {
        return resizeMode;
    }

    // Our ellipsis string
    private static final String mEllipsis = "...";

    // Flag for text and/or size changes to force a resize
    boolean mNeedsResize = false;

    // Text view line spacing multiplier
    private float mSpacingMult = 1.0f;

    // Text view additional line spacing
    private float mSpacingAdd = 0.0f;

    enum ResizeMode {
        width,
        height,
        heightWithEllipsis
    }

    ResizeMode resizeMode;

    // no-op these to prevent accidental wrapping

    @Override
    public void setBreakStrategy(int breakStrategy) {
    }

    @Override
    public void setHyphenationFrequency(int hyphenationFrequency) {
    }

    @Override
    public void setSingleLine() {
    }

    @Override
    public void setSingleLine(boolean singleLine) {
    }

    @Override
    public void setLines(int lines) {
    }

    @Override
    public void setMaxLines(int maxLines) {
    }

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        mNeedsResize = true;
        super.onTextChanged(text, start, before, after);
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            mNeedsResize = true;
        }
        super.onSizeChanged(w, h, oldw, oldh);
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

        // Get the current text height
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

    public int getTextWidth(CharSequence text, TextPaint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int width = bounds.left + bounds.width();
        return width;
    }

    public int getTextHeight(CharSequence text, TextPaint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.bottom + bounds.height();
        return height;
    }

    /**
     * Resize the text size with specified width and height
     * @param width
     * @param height
     */
    public void resizeText(int width, int height) {
        if (getText().length() == 0) return;
        switch (resizeMode) {
            case width:
                // resize to width and height
                CharSequence text = getText();
                TextPaint textPaint = getPaint();

                // Store the current text size
                float oldTextSize = textPaint.getTextSize();

                int size = 1;
                int sizep = 1;

                while (true) {
                    // see if text will fit
                    textPaint.setTextSize(size);
                    int textWidth = getTextWidth(text, textPaint);
                    int textHeight = getTextHeight(text, textPaint, width, size);

                    // ensure text is not larger than height
                    if (textHeight == height || textWidth == width) {
                        break;
                    }
                    if (textHeight > height || textWidth > width) {
                        size = sizep;
                        break;
                    }

                    // the text does not fit, increase size and try again
                    sizep = size++;
                }

                textPaint.setTextSize(oldTextSize);
                setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                mNeedsResize = false;
                break;
            case height:
                // resize to height
                CharSequence text_ = getText();
                TextPaint textPaint_ = getPaint();

                // Store the current text size
                float oldTextSize_ = textPaint_.getTextSize();

                textPaint_.setTextSize(oldTextSize_);
                ResizeData textDataMain = new ResizeData();
                String newText = text_.toString();

                while (true) {
                    // see if text will fit
                    textDataMain.process(width, height, newText, textPaint_, false);
                    if (textDataMain.resizable) {
                        // the text itself fits, exit loop
                        if (textDataMain.textWidth <= width) break;
                    }
                    // the text does not fit, trim text and try again
                    newText = newText.substring(0, newText.length() - 1);
                }

                textPaint_.setTextSize(oldTextSize_);
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataMain.textSize);
                mNeedsResize = false;
            case heightWithEllipsis:
                // resize to height, add ellipsis
                CharSequence text__ = getText();
                TextPaint textPaint__ = getPaint();

                // Store the current text size
                float oldTextSize__ = textPaint__.getTextSize();

                // if only the ellipse fits, then exit early
                boolean onlyRoomForEllipsis = false;
                // if the ellipse cannot fit, then exit early
                boolean noSpace = false;

                String newEllipsis = mEllipsis;
                ResizeData textDataEllipsis = new ResizeData();
                while (true) {
                    // see if ellipsis will fit
                    textDataEllipsis.process(width, height, newEllipsis, textPaint__, false);
                    if (textDataEllipsis.resizable) {
                        // the ellipsis itself fits, exit loop
                        if (textDataEllipsis.textWidth <= width) break;

                        // either the ellipsis fits partially or there is no room for it to fit,
                        // exit early
                        onlyRoomForEllipsis = true;
                        newEllipsis = newEllipsis.substring(0, newEllipsis.length() - 1);
                    } else {
                        // the ellipsis was unable to fit
                        noSpace = true;
                        break;
                    }
                }

                textPaint__.setTextSize(oldTextSize__);

                if (noSpace) {
                    setText("");
                    mNeedsResize = false;
                    return;
                }

                if (onlyRoomForEllipsis) {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataEllipsis.textSize);
                    setText(newEllipsis);
                    mNeedsResize = false;
                    return;
                }

                // there is at least room for ellipsis

                // see if the text by itself will fit first
                String newText__ = text__.toString();
                ResizeData textDataText = new ResizeData();
                textDataText.process(width, height, newText__, textPaint__, false);
                if (textDataText.resizable) {
                    // the text itself fits, exit loop
                    if (textDataText.textWidth <= width) {
                        textPaint__.setTextSize(oldTextSize__);
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataText.textSize);
                        mNeedsResize = false;
                        return;
                    }
                }

                // text does not fit
                textPaint__.setTextSize(oldTextSize__);
                ResizeData textDataMain__ = new ResizeData();

                while (true) {
                    // see if text and ellipsis will fit
                    textDataMain__.process(width, height, newText__ + mEllipsis, textPaint__, false);
                    if (textDataMain__.resizable) {
                        // the text + ellipsis itself fits, exit loop
                        if (textDataMain__.textWidth <= width) break;
                    }
                    // the text + ellipsis does not fit, trim text and try again
                    newText__ = newText__.substring(0, newText__.length() - 1);
                }

                textPaint__.setTextSize(oldTextSize__);
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataMain__.textSize);
                newText__ += mEllipsis;
                setText(newText__);
                mNeedsResize = false;
                break;
        }
    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
        paint.setTextSize(textSize);
        StaticLayout layout = new StaticLayout(source, paint, width, Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, true);
        return layout.getHeight();
    }

}