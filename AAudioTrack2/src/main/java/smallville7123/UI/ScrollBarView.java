package smallville7123.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.InspectableProperty;
import androidx.annotation.IntDef;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ScrollBarView extends FrameLayout {
    public ScrollBarView(Context context) {
        super(context);
        init(context, null);
    }

    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {}

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    static final int DEFAULT_ORIENTATION = VERTICAL;

    @ViewDebug.ExportedProperty(category = "measurement")
    private int mOrientation = DEFAULT_ORIENTATION;

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
//        setFillViewport(true);

        FrameLayout frame = new FrameLayout(context, attrs);
        content = frame;
        frame.setLayoutParams(
                new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        );
        frame.setTag(Internal);
        addView(frame);
        clip = newClip();
        clip.setColor(Color.LTGRAY);
        if (mOrientation == VERTICAL) {
            clip.setY(0);
            clip.setHeight(100);
        } else {
            clip.setX(0);
            clip.setWidth(100);
        }
        content.addView(clip.content);
        setPaint();
    }

    /**
     * Should the scroll bar be horizontal (scrolling left and right)
     * or vertical (scrolling up and down).
     * @param orientation Pass {@link #HORIZONTAL} or {@link #VERTICAL}. Default
     * value is {@link #VERTICAL}.
     */
    public void setOrientation(@OrientationMode int orientation) {
        if (mOrientation != orientation) {
            mOrientation = orientation;
            if (mOrientation == VERTICAL) {
                clip.setY(clip.getX());
                clip.setX(0);
                clip.setHeight(100);
            } else {
                clip.setX(clip.getY());
                clip.setY(0);
                clip.setWidth(100);
            }
            requestLayout();
        }
    }

    /**
     * Returns the current orientation.
     *
     * @return either {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    @OrientationMode
    @InspectableProperty(enumMapping = {
            @InspectableProperty.EnumEntry(value = HORIZONTAL, name = "horizontal"),
            @InspectableProperty.EnumEntry(value = VERTICAL, name = "vertical")
    })
    public int getOrientation() {
        return mOrientation;
    }

    View document;
    boolean layout = false;
    int documentScrollX;
    int documentScrollY;
    float documentWidthDivWindowWidth;
    float documentHeightDivWindowHeight;


    public void attachTo(View document) {
        this.document = document;
        invalidate();
    }

    float documentWidth;
    float windowWidth;
    float documentHeight;
    float windowHeight;
    
    static boolean DEBUG = true;

    void adjustClip(int r, int b) {
        if (mOrientation == VERTICAL) {
            clip.setWidth(r);
        } else {
            clip.setHeight(b);
        }
    }

    void getWindowSize() {
        if (mOrientation == VERTICAL) {
            windowHeight = document.getHeight();
        } else {
            windowWidth = document.getWidth();
        }
    }

    boolean getDocumentSizeTypeRecyclerView() {
        if (document instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) document;
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
                View view = manager.getChildAt(firstVisibleItemPosition);
                if (view != null) {
                    if (mOrientation == VERTICAL) {
                        documentHeight = view.getHeight() * manager.getItemCount();
                    } else {
                        documentWidth = view.getWidth() * manager.getItemCount();
                    }
                }
            }
            return true;
        } else return false;
    }

    boolean getDocumentSizeTypeHorizontalScrollView() {
        if (document instanceof HorizontalScrollView) {
            HorizontalScrollView horizontalScrollView = (HorizontalScrollView) document;
            documentWidth = horizontalScrollView.getChildAt(0).getWidth();
            return true;
        } else return false;
    }

    boolean getDocumentSize() {
        if (getDocumentSizeTypeRecyclerView()) return true;
        if (getDocumentSizeTypeHorizontalScrollView()) return true;
        return false;
    }

    void setThumbSize(int b, int r) {
        if (mOrientation == VERTICAL) {
            documentHeightDivWindowHeight = documentHeight / windowHeight;
            float thumbHeight = b / documentHeightDivWindowHeight;
            clip.setHeight((int) thumbHeight);
        } else {
            documentWidthDivWindowWidth = documentWidth / windowWidth;
            float thumbWidth = r / documentWidthDivWindowWidth;
            clip.setWidth((int) thumbWidth);
        }
    }

    void doScroll(int r, int b) {
        if (document != null) {
            getWindowSize();
            if (getDocumentSize()) {
                setThumbSize(b, r);
                if (!scrolling) scrollDocument();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (DEBUG) Log.d(TAG, "onLayout() called with: changed = [" + changed + "], l = [" + l + "], t = [" + t + "], r = [" + r + "], b = [" + b + "]");
        if (layout) {
            layout = false;
        } else {
            layout = true;
            adjustClip(r, b);
            doScroll(r, b);
        }
    }

    class Clip {
        View content;

        Clip(Context context) {
            content = new FrameLayout(context);
            if (mOrientation == VERTICAL) {
                setHeight(100);
            } else {
                setWidth(100);
            }
        }

        Clip(Context context, AttributeSet attrs) {
            content = new FrameLayout(context, attrs);
            if (mOrientation == VERTICAL) {
                setHeight(100);
            } else {
                setWidth(100);
            }
        }

        public Clip(View content) {
            this.content = content;
            if (mOrientation == VERTICAL) {
                setHeight(100);
            } else {
                setWidth(100);
            }
        }

        public void setColor(@ColorInt int color) {
            content.setBackgroundColor(color);
        }

        public void setX(float x) {
            ViewGroup.LayoutParams p = content.getLayoutParams();
            if (p != null) {
                if (p instanceof MarginLayoutParams) {
                    ((MarginLayoutParams) p).leftMargin = (int) x;
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
                                leftMargin = (int) x;
                            }
                        }
                );
            }
        }

        public float getX() {
            return content.getX();
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

        public void setWidth(int width) {
            ViewGroup.LayoutParams p = content.getLayoutParams();
            if (p != null) {
                p.width = width;
                content.setLayoutParams(p);
            } else {
                content.setLayoutParams(
                        new MarginLayoutParams(
                                width,
                                MATCH_PARENT
                        )
                );
            }
        }

        public int getWidth() {
            return content.getWidth();
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

    private float relativeToViewX;
    private float relativeToViewY;

    boolean clipTouch = false;
    Clip touchedClip;
    float downDX;
    float downDY;
    float downRawX;
    float downRawY;
    float currentRawX;
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

    public float touchZoneWidthLeft = 80.0f;
    public float touchZoneWidthLeftOffset = 80.0f;
    public float touchZoneWidthRight = 80.0f;
    public float touchZoneWidthRightOffset = 80.0f;
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
        if (mOrientation == VERTICAL) {
            float clipStart = touchedClip.getY();
            float clipHeight = touchedClip.getHeight();
            float clipEnd = clipStart + clipHeight;
            canvas.drawRect(0, clipStart, width, clipEnd, paint);
        } else {
            float clipStart = touchedClip.getX();
            float clipWidth = touchedClip.getWidth();
            float clipEnd = clipStart + clipWidth;
            canvas.drawRect(clipStart, 0, clipEnd, height, paint);
        }
    }

    void drawTouchZones(Canvas canvas, int width, int height, Paint paint) {
        if (clip != null) {
            if (mOrientation == VERTICAL) {
                float clipStart = touchedClip.getY();
                float clipHeight = touchedClip.getHeight();
                float clipEnd = clipStart + clipHeight;
                // top
                canvas.drawRect(0, clipStart - touchZoneHeightTopOffset, width, (clipStart + touchZoneHeightTop) - touchZoneHeightTopOffset, paint);
                // bottom
                canvas.drawRect(0, (clipEnd - touchZoneHeightBottom) + touchZoneHeightBottomOffset, width, clipEnd + touchZoneHeightBottomOffset, paint);
            } else {
                float clipStart = touchedClip.getX();
                float clipWidth = touchedClip.getWidth();
                float clipEnd = clipStart + clipWidth;
                // left
                canvas.drawRect(clipStart - touchZoneWidthLeftOffset, 0, (clipStart + touchZoneWidthLeft) - touchZoneWidthLeftOffset, height, paint);
                // right
                canvas.drawRect((clipEnd - touchZoneWidthRight) + touchZoneWidthRightOffset, 0, clipEnd + touchZoneWidthRightOffset, height, paint);
            }
        }
    }

    boolean isResizing;
    boolean isDragging;
    float clipOriginalStartX;
    float clipOriginalStartY;
    float clipOriginalWidth;
    float clipOriginalHeight;
    float clipOriginalEndX;
    float clipOriginalEndY;
    boolean resizingLeft;
    boolean resizingRight;
    boolean resizingTop;
    boolean resizingBottom;

    boolean scrolling = false;

    public void updateRelativePosition(int dx, int dy) {
        documentScrollX += dx;
        documentScrollY += dy;
        if (!scrolling) scrollThumb();
    }

    public void updateAbsolutePosition(int scrollX, int scrollY) {
        documentScrollX = scrollX;
        documentScrollY = scrollY;
        if (!scrolling) scrollThumb();
    }

    void scrollThumb() {
        // the absolute position is only updated when the scrollbar itself
        // gets scrolled by the touch listener
        // so use documentScrollY instead, which is equivilant in that
        // it is maintained by a callback from the view
        // specifying relative scroll direction
        float multiplier;
        float scrollBarPosition;
        if (mOrientation == VERTICAL) {
            multiplier = documentScrollY / (documentHeight - windowHeight);
            scrollBarPosition = multiplier * (getHeight() - clip.getHeight());
            clip.setY(scrollBarPosition);
        } else {
            multiplier = documentScrollX / (documentWidth - windowWidth);
            scrollBarPosition = multiplier * (getWidth() - clip.getWidth());
            clip.setX(scrollBarPosition);
        }
    }

    void scrollDocument() {
        float multiplier;
        float absoluteOffset;
        if (mOrientation == VERTICAL) {
            multiplier = clip.getY() / (getHeight() - clip.getHeight());
            absoluteOffset = multiplier * (documentHeight - windowHeight);
            scrolling = true;
            document.scrollBy(0, -documentScrollY);
            document.scrollBy(0, (int) absoluteOffset);
        } else {
            multiplier = clip.getX() / (getWidth() - clip.getWidth());
            absoluteOffset = multiplier * (documentWidth - windowWidth);
            scrolling = true;
            document.scrollBy(-documentScrollX, 0);
            document.scrollBy((int) absoluteOffset, 0);
        }
        scrolling = false;
    }

    public boolean onClipTouchEvent(Clip clip, MotionEvent event) {
        currentRawX = event.getRawX();
        currentRawY = event.getRawY();
        relativeToViewX = event.getX() + getScrollX();
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
                    if (mOrientation == VERTICAL) {
                        // scroll bar thumb
                        float thumbY = clip.getY();

                        // current y location
                        float scrollBarPosition = currentRawY + downDY;

                        // dont scroll past start
                        if (scrollBarPosition <= 0) {
                            clip.setY(0);
                        } else {
                            // dont scroll past end
                            float clipEnd = scrollBarPosition + clipOriginalHeight;
                            float viewEnd = getHeight();
                            if (clipEnd > viewEnd) {
                                clip.setY(scrollBarPosition - (clipEnd - viewEnd));
                            } else {
                                clip.setY(scrollBarPosition);
                            }
                        }
                    } else {
                        // scroll bar thumb
                        float thumbX = clip.getX();

                        // current x location
                        float scrollBarPosition = currentRawX + downDX;

                        // dont scroll past start
                        if (scrollBarPosition <= 0) {
                            clip.setX(0);
                        } else {
                            // dont scroll past end
                            float clipEnd = scrollBarPosition + clipOriginalWidth;
                            float viewEnd = getWidth();
                            if (clipEnd > viewEnd) {
                                clip.setX(scrollBarPosition - (clipEnd - viewEnd));
                            } else {
                                clip.setX(scrollBarPosition);
                            }
                        }
                    }
                    return true;
                } else if (isResizing && !isDragging) {
                    MarginLayoutParams layoutParams = (MarginLayoutParams) clip.content.getLayoutParams();
                    if (resizingLeft) {
                        float bounds = currentRawX + downDX;
                        if (layoutParams.width > 0) {
                            if (bounds > clipOriginalEndX) bounds = clipOriginalEndX;
                            float newWidth = clipOriginalWidth - (bounds - clipOriginalStartX);
                            if (newWidth < 1.0f) newWidth = 1.0f;
                            clip.setX(bounds);
                            clip.setWidth((int) newWidth);
                        }
                    } else if (resizingRight) {
                        float bounds = currentRawX + downDX;
                        if (layoutParams.width > 0) {
                            float newWidth = clipOriginalWidth + (bounds - clipOriginalStartX);
                            if (newWidth < 1.0f) newWidth = 1.0f;
                            clip.setWidth((int) newWidth);
                        }
                    } else if (resizingTop) {
                        float bounds = currentRawY + downDY;
                        if (layoutParams.height > 0) {
                            if (bounds > clipOriginalEndY) bounds = clipOriginalEndY;
                            float newHeight = clipOriginalHeight - (bounds - clipOriginalStartY);
                            if (newHeight < 1.0f) newHeight = 1.0f;
                            clip.setY(bounds);
                            clip.setHeight((int) newHeight);
                        }
                    } else if (resizingBottom) {
                        float bounds = currentRawY + downDY;
                        if (layoutParams.height > 0) {
                            float newHeight = clipOriginalHeight + (bounds - clipOriginalStartY);
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
                clipOriginalStartX = clip.getX();
                clipOriginalStartY = clip.getY();
                clipOriginalWidth = clip.getWidth();
                clipOriginalHeight = clip.getHeight();
                clipOriginalEndX = clipOriginalStartX + clipOriginalWidth;
                clipOriginalEndY = clipOriginalStartY + clipOriginalHeight;
                downRawX = currentRawX;
                downRawY = currentRawY;
                resizingLeft = false;
                resizingRight = false;
                resizingTop = false;
                resizingBottom = false;
                if (mOrientation == VERTICAL) {
                    float topStart = clipOriginalStartY - touchZoneHeightTopOffset;
                    float topEnd = (clipOriginalStartY + touchZoneHeightTop) - touchZoneHeightTopOffset;
                    float bottomStart = (clipOriginalEndY - touchZoneHeightBottom) + touchZoneHeightBottomOffset;
                    float bottomEnd = clipOriginalEndY + touchZoneHeightBottomOffset;
                    if (within(relativeToViewY, topStart, topEnd)) {
//                    resizingTop = true;
//                    isResizing = true;
                    } else if (within(relativeToViewY, bottomStart, bottomEnd)) {
//                    resizingBottom = true;
//                    isResizing = true;
                    } else if (within(relativeToViewY, clipOriginalStartY, clipOriginalEndY)) {
                        isDragging = true;
                    }
                } else {
                    float leftStart = clipOriginalStartX - touchZoneWidthLeftOffset;
                    float leftEnd = (clipOriginalStartX + touchZoneWidthLeft) - touchZoneWidthLeftOffset;
                    float rightStart = (clipOriginalEndX - touchZoneWidthRight) + touchZoneWidthRightOffset;
                    float rightEnd = clipOriginalEndX + touchZoneWidthRightOffset;
                    if (within(relativeToViewX, leftStart, leftEnd)) {
//                        resizingLeft = true;
//                        isResizing = true;
                    } else if (within(relativeToViewX, rightStart, rightEnd)) {
//                        resizingRight = true;
//                        isResizing = true;
                    } else if (within(relativeToViewX, clipOriginalStartX, clipOriginalEndX)) {
                        isDragging = true;
                    }
                }
                if (isResizing || isDragging) {
                    invalidate();
                    downDX = clipOriginalStartX - downRawX;
                    downDY = clipOriginalStartY - downRawY;
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