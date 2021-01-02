package smallville7123.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ScrollBarView extends ScrollView {
    public ScrollBarView(Context context) {
        super(context);
        init(context, null);
    }

    public ScrollBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScrollBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    Context mContext;
    AttributeSet mAttrs;
    FrameLayout content;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mAttrs = attrs;

        // make scroll view match parent height
        setFillViewport(true);

        FrameLayout frame = new FrameLayout(context, attrs);
        content = frame;
        frame.setLayoutParams(
                new ViewGroup.LayoutParams(
                        MATCH_PARENT,
                        WRAP_CONTENT
                )
        );
        frame.setTag(Internal);
        addView(frame);
        Clip A = newClip();
        A.setColor(Color.LTGRAY);
        A.setY(0);
        A.setHeight(100);
        addClip(A);
        setPaint();
    }

    class Clip {
        View content;

        Clip(Context context) {
            content = new FrameLayout(context);
            setHeight(100);
        }

        Clip(Context context, AttributeSet attrs) {
            content = new FrameLayout(context, attrs);
            setHeight(100);
        }

        public Clip(View content) {
            content = content;
        }

        public void setColor(@ColorInt int color) {
            content.setBackgroundColor(color);
        }

        public void setY(float y) {
            ViewGroup.LayoutParams p = content.getLayoutParams();
            if (p != null) {
                if (p instanceof MarginLayoutParams) {
                    ((MarginLayoutParams) p).topMargin = (int) y;
                    content.setLayoutParams(p);
                } else {
                    throw new RuntimeException("layout is not an instance of MarginLayoutParams");
                }
            } else {
                content.setLayoutParams(
                        new MarginLayoutParams(
                                MATCH_PARENT,
                                MATCH_PARENT
                        ) {
                            {
                                topMargin = (int) y;
                            }
                        }
                );
            }
        }

        public float getY() {
            return content.getY();
        }

        public void setHeight(int height) {
            ViewGroup.LayoutParams p = content.getLayoutParams();
            if (p != null) {
                p.height = height;
                content.setLayoutParams(p);
            } else {
                content.setLayoutParams(
                        new MarginLayoutParams(
                                MATCH_PARENT,
                                height
                        )
                );
            }
        }

        public int getHeight() {
            return content.getHeight();
        }

        public ViewPropertyAnimator animate() {
            return content.animate();
        }
    }

    Clip newClip() {
        return new Clip(mContext, mAttrs);
    };

    private static class Internal {}
    Internal Internal = new Internal();
    ArrayList<Clip> clips = new ArrayList<>();

    public void addClip(Clip clip) {
        clips.add(clip);
        content.addView(clip.content);
    }

    private static final String TAG = "ClipView";

    private float relativeToViewY;

    boolean scrolling = false;
    boolean clipTouch = false;
    Clip touchedClip;
    float downDY;
    float downRawY;
    float currentRawY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (Clip clip : clips) {
                    boolean ret = onClipTouchEvent(clip, event);
                    if (ret) {
                        clipTouch = true;
                        touchedClip = clip;
                        return ret;
                    }
                }
                scrolling = true;
                return super.onTouchEvent(event);
            case MotionEvent.ACTION_MOVE:
                return clipTouch ? onClipTouchEvent(touchedClip, event) : super.onTouchEvent(event);
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (clipTouch) {
                    boolean ret = onClipTouchEvent(touchedClip, event);
                    clipTouch = false;
                    return ret;
                }
                scrolling = false;
                return super.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public float touchZoneHeightTop = 80.0f;
    public float touchZoneHeightTopOffset = 80.0f;
    public float touchZoneHeightBottom = 80.0f;
    public float touchZoneHeightBottomOffset = 80.0f;

    Paint highlightPaint;
    Paint touchZonePaint;

    private void setPaint() {
        highlightPaint = new Paint();
        touchZonePaint = new Paint();

        highlightPaint.setARGB(200, 0, 0, 255);
        touchZonePaint.setARGB(160, 0, 90, 0);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        int height = getHeight();
        int width = getWidth();
        if (isResizing) {
            drawHighlight(canvas, width, height, highlightPaint);
        }
//        drawTouchZones(canvas, width, height, touchZonePaint);
    }

    void drawHighlight(Canvas canvas, int width, int height, Paint paint) {
        float clipStart = touchedClip.getY();
        float clipHeight = touchedClip.getHeight();
        float clipEnd = clipStart + clipHeight;
        canvas.drawRect(0, clipStart, width, clipEnd, paint);
    }

    void drawTouchZones(Canvas canvas, int width, int height, Paint paint) {
        for (Clip clip : clips) {
            float clipStart = clip.getY();
            float clipHeight = clip.getHeight();
            float clipEnd = clipStart + clipHeight;
            // top
            canvas.drawRect(0, clipStart - touchZoneHeightTopOffset, width, (clipStart + touchZoneHeightTop) - touchZoneHeightTopOffset, paint);
            // bottom
            canvas.drawRect(0, (clipEnd - touchZoneHeightBottom) + touchZoneHeightBottomOffset, width, clipEnd + touchZoneHeightBottomOffset, paint);
        }
    }

    boolean isResizing;
    boolean isDragging;
    float clipOriginalStart;
    float clipOriginalHeight;
    float clipOriginalEnd;
    boolean resizingTop;
    boolean resizingBottom;

    public boolean onClipTouchEvent(Clip clip, MotionEvent event) {
        currentRawY = event.getRawY();
        relativeToViewY = event.getY() + getScrollY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isResizing && isDragging) {
                    isDragging = false;
                    return true;
                } else if (isResizing && !isDragging) {
                    isResizing = false;
                    invalidate();
                    return true;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!isResizing && isDragging) {
                    if (currentRawY + downDY >= 0) {
                        clip.setY(currentRawY + downDY);
                    } else {
                        clip.setY(0);
                    }
                    return true;
                } else if (isResizing && !isDragging) {
                    MarginLayoutParams layoutParams = (MarginLayoutParams) clip.content.getLayoutParams();
                    if (resizingTop) {
                        float bounds = currentRawY + downDY;
                        if (layoutParams.height > 0) {
                            if (bounds > clipOriginalEnd) bounds = clipOriginalEnd;
                            float newHeight = clipOriginalHeight - (bounds - clipOriginalStart);
                            if (newHeight < 1.0f) newHeight = 1.0f;
                            clip.setY(bounds);
                            clip.setHeight((int) newHeight);
                        }
                    } else if (resizingBottom) {
                        float bounds = currentRawY + downDY;
                        if (layoutParams.height > 0) {
                            float newHeight = clipOriginalHeight + (bounds - clipOriginalStart);
                            if (newHeight < 1.0f) newHeight = 1.0f;
                            clip.setHeight((int) newHeight);
                        }
                    }
                    return true;
                }
                return false;
            case MotionEvent.ACTION_DOWN:
                isDragging = false;
                isResizing = false;
                clipOriginalStart = clip.getY();
                clipOriginalHeight = clip.getHeight();
                clipOriginalEnd = clipOriginalStart + clipOriginalHeight;
                downRawY = currentRawY;
                resizingTop = false;
                resizingBottom = false;
                float topStart = clipOriginalStart - touchZoneHeightTopOffset;
                float topEnd = (clipOriginalStart + touchZoneHeightTop) - touchZoneHeightTopOffset;
                float bottomStart = (clipOriginalEnd - touchZoneHeightBottom) + touchZoneHeightBottomOffset;
                float bottomEnd = clipOriginalEnd + touchZoneHeightBottomOffset;
                if (within(relativeToViewY, topStart, topEnd)) {
                    resizingTop = true;
                    isResizing = true;
                } else if (within(relativeToViewY, bottomStart, bottomEnd)) {
                    resizingBottom = true;
                    isResizing = true;
                } else if (within(relativeToViewY, clipOriginalStart, clipOriginalEnd)) {
                    isDragging = true;
                }
                if (isResizing || isDragging) {
                    invalidate();
                    downDY = clipOriginalStart - downRawY;
                    return true;
                }
            default:
                return false;
        }
    }

    boolean within(float point, float start, float end) {
        return point >= start && point <= end;
    }








    /**
     * <p>Adds a child view. If no layout parameters are already set on the child, the
     * default parameters for this ViewGroup are set on the child.</p>
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(Canvas)}, {@link #onDraw(Canvas)},
     * {@link #dispatchDraw(Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     *
     * @see #generateDefaultLayoutParams()
     */
    @Override
    public void addView(View child) {
        addView(child, -1);
    }

    /**
     * Adds a child view. If no layout parameters are already set on the child, the
     * default parameters for this ViewGroup are set on the child.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(Canvas)}, {@link #onDraw(Canvas)},
     * {@link #dispatchDraw(Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param indey the position at which to add the child
     *
     * @see #generateDefaultLayoutParams()
     */
    @Override
    public void addView(View child, int indey) {
        if (child == null) {
            throw new IllegalArgumentException("Cannot add a null child view to a ViewGroup");
        }
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = generateDefaultLayoutParams();
            if (params == null) {
                throw new IllegalArgumentException("generateDefaultLayoutParams() cannot return null");
            }
        }
        addView(child, indey, params);
    }

    /**
     * Adds a child view with this ViewGroup's default layout parameters and the
     * specified height and height.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(Canvas)}, {@link #onDraw(Canvas)},
     * {@link #dispatchDraw(Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     */
    @Override
    public void addView(View child, int width, int height) {
        final ViewGroup.LayoutParams params = generateDefaultLayoutParams();
        params.height = height;
        params.height = height;
        addView(child, -1, params);
    }

    /**
     * Adds a child view with the specified layout parameters.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(Canvas)}, {@link #onDraw(Canvas)},
     * {@link #dispatchDraw(Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param params the layout parameters to set on the child
     */
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addView(child, -1, params);
    }

    /**
     * Adds a child view with the specified layout parameters.
     *
     * <p><strong>Note:</strong> do not invoke this method from
     * {@link #draw(Canvas)}, {@link #onDraw(Canvas)},
     * {@link #dispatchDraw(Canvas)} or any related method.</p>
     *
     * @param child the child view to add
     * @param indey the position at which to add the child or -1 to add last
     * @param params the layout parameters to set on the child
     */
    @Override
    public void addView(View child, int indey, ViewGroup.LayoutParams params) {
        Object tag = child.getTag();
        if (tag instanceof Internal) super.addView(child, indey, params);
        else addClip(new Clip(child));
    }
}