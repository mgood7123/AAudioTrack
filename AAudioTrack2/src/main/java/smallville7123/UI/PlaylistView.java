package smallville7123.UI;

// hmmm i wonder if the playlist could be a gigantic advanced piano roll lmao
//
// eg just replace note data with pattern numbers and it should be good lmao
//
// but i suspect it would probably be somehow more complex than that lol

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class PlaylistView extends FrameLayout {
    public PlaylistView(Context context) {
        super(context);
        init(context, null);
    }

    public PlaylistView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlaylistView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PlaylistView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    Context mContext;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
    }
}
