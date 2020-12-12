package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import smallville7123.aaudiotrack2.R;

@SuppressLint("AppCompatCustomView")
public class UpdatingImageProgressBar extends ImageProgressBar {
    ArrayList<Runnable> initialDrawQueue = new ArrayList<>();
    boolean initialDrawQueueHasBeenExecuted = false;
    ArrayList<Runnable> drawQueue = new ArrayList<>();

    public void addOnFirstDrawAction(Runnable action) {
        initialDrawQueue.add(action);
    }

    public void addOnDrawAction(Runnable action) {
        drawQueue.add(action);
    }


    /**
     * draws the background onto the canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (initialDrawQueueHasBeenExecuted) {
            for (Runnable action : drawQueue) action.run();
        } else {
            if (!initialDrawQueue.isEmpty()) {
                for (Runnable action : initialDrawQueue) action.run();
                initialDrawQueueHasBeenExecuted = true;
            }
        }
        super.onDraw(canvas);
    }

    public UpdatingImageProgressBar(Context context) {
        super(context);
    }

    public UpdatingImageProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UpdatingImageProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UpdatingImageProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
