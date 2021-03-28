package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.text.LineBreaker;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;

import smallville7123.aaudiotrack2.R;

/**
 * SLASET - Single Line Auto Size EditText
 * an EditText that auto adjusts text size to fit on a single line within the view.
 */
@SuppressLint("AppCompatCustomView")
public class SLASET extends PreviewEditText {

    private static final String TAG = "SLASET";

    private OnPreviewListener SLASETListener = new OnPreviewListener() {
        boolean isEllipsis = false;
        int w = 0;
        int h = 0;

        @Override
        void onPreview(boolean active) {
            if (active) {
                if (resizeMode == ResizeMode.heightWithEllipsis) {
                    isEllipsis = true;
                    resizeMode = ResizeMode.height;
                }
            } else {
                if (isEllipsis) {
                    resizeMode = ResizeMode.heightWithEllipsis;
                    isEllipsis = false;
                }
            }
        }

        @Override
        public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            w = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
            h = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
        }

        @Override
        String refreshProcess(String realText, String preview_) {
            if (realText.isEmpty()) return "";
            TextPaint textPaint = getPaint();

            // Store the current text size
            float oldTextSize = textPaint.getTextSize();

            switch (resizeMode) {
                case width:
                    // resize to width and height

                    int size = 1;
                    int sizep = 1;

                    while (true) {
                        // see if text will fit
                        textPaint.setTextSize(size);
                        int textWidth = getTextWidth(realText, textPaint);
                        int textHeight = getTextHeight(realText, textPaint, w, size);

                        // ensure text is not larger than height
                        if (textHeight == h || textWidth == w) {
                            break;
                        }
                        if (textHeight > h || textWidth > w) {
                            size = sizep;
                            break;
                        }

                        // the text does not fit, increase size and try again
                        sizep = size++;
                    }

                    textPaint.setTextSize(oldTextSize);
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                    return realText;
                case height:
                    // resize to height

                    textPaint.setTextSize(oldTextSize);
                    ResizeData textDataMain = new ResizeData();
                    while (true) {
                        // see if text will fit
                        textDataMain.process(w, h, realText, textPaint, false);
                        if (textDataMain.resizable) {
                            // the text itself fits, exit loop
                            if (textDataMain.textWidth <= w) break;
                        }
                        // the text does not fit, trim text and try again
                        realText = realText.substring(0, realText.length() - 1);
                    }

                    textPaint.setTextSize(oldTextSize);
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataMain.textSize);
                    return realText;
                case heightWithEllipsis:
                    // resize to height, add ellipsis

                    // if only the ellipse fits, then exit early
                    boolean onlyRoomForEllipsis = false;
                    // if the ellipse cannot fit, then exit early
                    boolean noSpace = false;

                    String newEllipsis = mEllipsis;
                    ResizeData textDataEllipsis = new ResizeData();
                    while (true) {
                        // see if ellipsis will fit
                        textDataEllipsis.process(w, h, newEllipsis, textPaint, false);
                        if (textDataEllipsis.resizable) {
                            // the ellipsis itself fits, exit loop
                            if (textDataEllipsis.textWidth <= w) break;

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

                    textPaint.setTextSize(oldTextSize);

                    if (noSpace) return "";

                    if (onlyRoomForEllipsis) {
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataEllipsis.textSize);
                        return newEllipsis;
                    }

                    // there is at least room for ellipsis

                    // see if the text by itself will fit first
                    ResizeData textDataText = new ResizeData();
                    textDataText.process(w, h, realText, textPaint, false);
                    if (textDataText.resizable) {
                        // the text itself fits, exit loop
                        if (textDataText.textWidth <= w) {
                            textPaint.setTextSize(oldTextSize);
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataText.textSize);
                            return realText;
                        }
                    }

                    // text does not fit
                    textPaint.setTextSize(oldTextSize);
                    ResizeData textDataMain__ = new ResizeData();

                    while (true) {
                        // see if text and ellipsis will fit
                        textDataMain__.process(w, h, realText + mEllipsis, textPaint, false);
                        if (textDataMain__.resizable) {
                            // the text + ellipsis itself fits, exit loop
                            if (textDataMain__.textWidth <= w) break;
                        }
                        // the text + ellipsis does not fit, trim text and try again
                        realText = realText.substring(0, realText.length() - 1);
                    }

                    textPaint.setTextSize(oldTextSize);
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataMain__.textSize);

                    return realText + mEllipsis;
            }
            return realText;
        }
    };

    // Default constructor override
    public SLASET(Context context) {
        this(context, null);
    }

    // Default constructor when inflating from XML file
    public SLASET(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    // Default constructor override
    public SLASET(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public SLASET(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // set to single line
        super.setSingleLine(true);

        // make sure we do not break text at all
        super.setLines(1);
        super.setMaxLines(1);
        super.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
        super.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);

        // display Go instead of Next
        setImeOptions(EditorInfo.IME_ACTION_GO);

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

        setOnPreviewListener(SLASETListener);
    }

    public ResizeMode switchToResizeMode(ResizeMode mode) {
        ResizeMode old = resizeMode;
        resizeMode = mode;
        return old;
    }

    public ResizeMode getResizeMode() {
        return resizeMode;
    }

    // retain cursor position
    @Override
    public void setText(CharSequence text, BufferType type) {
        int position = getSelectionStart();
        super.setText(text, type);
        setSelection(Math.min(text.length(), position));
    }

    // Our ellipsis string
    private static final String mEllipsis = "...";

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
                layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, getLineSpacingMultiplier(), getLineSpacingExtra(), includepad);
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

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
        paint.setTextSize(textSize);
        StaticLayout layout = new StaticLayout(source, paint, width, Layout.Alignment.ALIGN_NORMAL, getLineSpacingMultiplier(), getLineSpacingExtra(), true);
        return layout.getHeight();
    }

}