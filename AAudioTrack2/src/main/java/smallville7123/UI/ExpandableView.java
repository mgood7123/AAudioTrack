package smallville7123.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import smallville7123.aaudiotrack2.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ExpandableView extends FrameLayout {

    private static final String TAG = "ExpandableView";
    Runnable onHeaderClicked;
    Runnable onContentClicked;
    boolean expanded = false;
    boolean replaceHeaderWhenExpanded = false;
    private FrameLayout header;
    private FrameLayout content;


    public ExpandableView(Context context) {
        this(context, null);
    }

    public ExpandableView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExpandableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        header = new FrameLayout(context, attrs, defStyleAttr, defStyleRes);
        content = new FrameLayout(context, attrs, defStyleAttr, defStyleRes);

        header.setLayoutParams(new MarginLayoutParams(MATCH_PARENT, MATCH_PARENT));
        content.setLayoutParams(new MarginLayoutParams(MATCH_PARENT, MATCH_PARENT));

        addView(header);
        addView(content);

        internalCollapse(false);
        header.setOnClickListener(v -> {
            toggleExpandedState(false);
            if (onHeaderClicked != null) onHeaderClicked.run();
        });

        content.setOnClickListener(v -> {
            if (onContentClicked != null) onContentClicked.run();
        });

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ExpandableView, defStyleAttr, defStyleRes);
        int headerId = a.getResourceId(R.styleable.ExpandableView_headerLayout, 0);
        int contentId = a.getResourceId(R.styleable.ExpandableView_contentLayout, 0);
        a.recycle();

        if (headerId != 0) setHeader(inflate(context, headerId, null));
        if (contentId != 0) setContent(inflate(context, contentId, null));
    }


    public void setOnHeaderClicked(Runnable onHeaderClicked) {
        this.onHeaderClicked = onHeaderClicked;
    }

    public void setOnContentClicked(Runnable onContentClicked) {
        this.onContentClicked = onContentClicked;
    }


    public void toggleExpandedState(boolean animate) {
        if (expanded) {
            internalCollapse(animate);
        } else {
            internalExpand(animate);
        }
    }

    public void expand(boolean animate) {
        if (!expanded) internalExpand(animate);
    }

    public void collapse(boolean animate) {
        if (expanded) internalCollapse(animate);
    }

    public void internalExpand(boolean animate) {
        expanded = true;
        if (animate) {

        } else {
            if (replaceHeaderWhenExpanded) {
                header.setVisibility(GONE);
                content.setVisibility(VISIBLE);
            } else {
                MarginLayoutParams layoutParams = (MarginLayoutParams) content.getLayoutParams();
                layoutParams.topMargin = header.getBottom();
                content.setLayoutParams(layoutParams);
                content.setVisibility(VISIBLE);
            }
        }
    }

    void internalCollapse(boolean animate) {
        expanded = false;
        if (animate) {

        } else {
            if (replaceHeaderWhenExpanded) {
                header.setVisibility(VISIBLE);
                content.setVisibility(GONE);
            } else {
                content.setVisibility(GONE);
                MarginLayoutParams layoutParams = (MarginLayoutParams) content.getLayoutParams();
                layoutParams.topMargin = 0;
                content.setLayoutParams(layoutParams);
            }
        }
    }

    public void setHeader(View view) {
        header.removeAllViewsInLayout();
        header.addView(view, 0, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    public void setContent(View view) {
        content.removeAllViewsInLayout();
        content.addView(view, 0, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    public FrameLayout getHeader() {
        return header;
    }

    public FrameLayout getContent() {
        return content;
    }

    public void setHeaderTag(Object object) {
        header.setTag(object);
    }

    public void setHeaderTag(int key, Object object) {
        header.setTag(key, object);
    }

    public Object getHeaderTag() {
        return header.getTag();
    }

    public Object getHeaderTag(int key) {
        return header.getTag(key);
    }

    public void setContentTag(Object object) {
        content.setTag(object);
    }

    public void setContentTag(int key, Object object) {
        content.setTag(key, object);
    }

    public Object getContentTag() {
        return content.getTag();
    }

    public Object getContentTag(int key) {
        return content.getTag(key);
    }
}
