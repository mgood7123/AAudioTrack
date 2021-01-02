package smallville7123.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.function.Consumer;

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
        clip = newClip();
        clip.setColor(Color.LTGRAY);
        clip.setY(0);
        clip.setHeight(100);
        content.addView(clip.content);
        setPaint();
    }

    View scrollable;

    public void attachTo(View scrollable) {
        this.scrollable = scrollable;
        invalidate();
//        Consumer<ViewGroup.LayoutParams> a = scrollable::setLayoutParams;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if (scrollable != null) {
//            Log.d(TAG, "scrollable.getScrollY() = [ " + (scrollable.getScrollY()) + "]");
//            Log.d(TAG, "scrollable.getHeight() = [ " + (scrollable.getHeight()) + "]");
//        }
    }

    public void updatePosition(int dx, int dy) {
        clip.setY(clip.getY() + dy);
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
    Clip clip;

    private static final String TAG = "ScrollBarView";

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
                if (clip != null) {
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
        if (clip != null) {
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
                        float y1 = clip.getY();
                        float y2 = currentRawY + downDY;
                        float y3 = y1 - y2;
                        clip.setY(y2);
                        scrollable.scrollBy(0, (int) (-y3));
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
}