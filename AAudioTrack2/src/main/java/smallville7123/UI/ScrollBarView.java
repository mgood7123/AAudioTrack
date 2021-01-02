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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.function.Consumer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

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
    boolean layout = false;
    int scrollableScrollX;
    int scrollableScrollY;
    float tmpScrollableScrollX;
    float tmpScrollableScrollY;


    public void attachTo(View scrollable) {
        this.scrollable = scrollable;
        invalidate();
//        Consumer<ViewGroup.LayoutParams> a = scrollable::setLayoutParams;
    }

    public void updatePosition(int dx, int dy) {
        clip.setY(clip.getY() + dy);
        scrollableScrollX += dx;
        scrollableScrollY += dy;
        Log.d(TAG, "scrollableScrollX = [" + (scrollableScrollX) + "]");
        Log.d(TAG, "scrollableScrollY = [" + (scrollableScrollY) + "]");
    }

    public void updateScrollPosition(float dx, float dy) {
        tmpScrollableScrollX += dx;
        tmpScrollableScrollY += dy;
        Log.d(TAG, "tmpScrollableScrollX = [" + (tmpScrollableScrollX) + "]");
        Log.d(TAG, "tmpScrollableScrollY = [" + (tmpScrollableScrollY) + "]");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d(TAG, "onLayout() called with: changed = [" + changed + "], l = [" + l + "], t = [" + t + "], r = [" + r + "], b = [" + b + "]");
        if (layout) {
            layout = false;
        } else {
            if (scrollable != null) {
                if (scrollable instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) scrollable;
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager instanceof LinearLayoutManager) {
                        LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
                        int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
                        int firstCompletelyVisibleItemPosition = manager.findFirstCompletelyVisibleItemPosition();
                        int lastVisibleItemPosition = manager.findLastVisibleItemPosition();
                        int lastCompletelyVisibleItemPosition = manager.findLastCompletelyVisibleItemPosition();

                        Log.d(TAG, "firstVisibleItemPosition = [" + (firstVisibleItemPosition) + "]");
                        Log.d(TAG, "firstCompletelyVisibleItemPosition = [" + (firstCompletelyVisibleItemPosition) + "]");
                        Log.d(TAG, "lastVisibleItemPosition = [" + (lastVisibleItemPosition) + "]");
                        Log.d(TAG, "lastCompletelyVisibleItemPosition = [" + (lastCompletelyVisibleItemPosition) + "]");

                        int height = 0;
                        View lastCompletelyVisibleVisibleView;
                        // scenarios:
                        // 1. the first view and last view are partially visible
                        // 2. the first view and last view are completely visible
                        // 3. the first view is completely visible
                        //    while the last view is partially visible
                        // 4. the last view is completely visible
                        //    while the first view is partially visible

                        // for reasons, lets skip everything
                        // and assume each item consumes the entire width:
                        // |ITEM_A|
                        // |ITEM_B|

                        View view = manager.getChildAt(firstVisibleItemPosition);
                        if (view != null) {
                            int itemHeight = view.getHeight();

                            Log.d(TAG, "itemHeight = [" + (itemHeight) + "]");
                            // 2. compute the total height

                            float documentHeight = itemHeight * manager.getItemCount();
                            float windowHeight = scrollable.getHeight();
                            float trackHeight = b;

                            Log.d(TAG, "documentHeight = [" + (documentHeight) + "]");
                            Log.d(TAG, "windowHeight = [" + (windowHeight) + "]");
                            Log.d(TAG, "trackHeight = [" + (trackHeight) + "]");

                            // 3. now that we have our total height...

                            float documentHeightDivWindowHeight = documentHeight / windowHeight;

                            Log.d(TAG, "documentHeightDivWindowHeight = [" + (documentHeightDivWindowHeight) + "]");

                            float thumbHeight = trackHeight / documentHeightDivWindowHeight;

                            Log.d(TAG, "thumbHeight = [" + (thumbHeight) + "]");

                            layout = true;
                            clip.setHeight((int) thumbHeight);
                        }
                    }
                }
            }
        }
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
                // scroll bar thumb
                float y1 = clip.getY();

                // current y location
                float y2 = currentRawY + downDY;

                // dont scroll past start
                if (y2 <= 0) {
                    clip.setY(0);
                } else {
                    // dont scroll past end
                    float clipEnd = y2 + clipOriginalHeight;
                    float viewEnd = getY() + getHeight();
                    if (clipEnd > viewEnd) {
                        clip.setY(y2 - (clipEnd - viewEnd));
                    } else {
                        clip.setY(y2);
                        // scroll our view
                        // TODO: account for views
                        //  that can use absolute positions
                        //  and views that cannot use absolute
                        //  positions
                        //
                        // in this case, our view cannot use absolute position

                        // relative difference
                        float y3 = -(y1 - y2);
                        Log.d(TAG, "y3 = [" + (y3) + "]");
                        updateScrollPosition(0, y3);
                        scrollable.scrollBy(0, (int) y3);
                    }
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