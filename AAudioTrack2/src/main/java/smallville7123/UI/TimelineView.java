package smallville7123.UI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import smallville7123.UI.ScrollBarView.CanvasDrawer;
import smallville7123.UI.ScrollBarView.CanvasView;

import static android.graphics.Color.argb;

public class TimelineView extends CanvasView {
    private static final String TAG = "TimelineView";

    public TimelineView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public TimelineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimelineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public TimelineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    Paint paintGrey;
    Paint paintRed;

    void initPaint() {
        paintGrey = new Paint();
        paintRed = new Paint();
        paintGrey.setColor(argb(255, 125, 125, 125));
        paintRed.setColor(argb(255, 255, 0, 0));
    }

    private void init(Context context, AttributeSet attrs) {
        createCanvas(4000, 4000);
        initPaint();
    }

    @Override
    protected void onDrawCanvas(CanvasDrawer canvas) {
        canvas.clear();
        canvas.paint = paintRed;
        canvas.drawRect(500, 500, 500, 500);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent() called with: event = [" + event + "]");
        return super.onTouchEvent(event);
    }
}
