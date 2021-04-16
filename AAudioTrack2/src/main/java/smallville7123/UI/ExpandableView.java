package smallville7123.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import smallville7123.aaudiotrack2.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ExpandableView extends LinearLayout {

    private static final String TAG = "ExpandableView";
    Runnable onHeaderClicked;
    Runnable onContentClicked;
    boolean expanded;
    boolean replaceHeaderWhenExpanded;
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

    boolean shouldExpand;

    public ExpandableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (attrs != null) {
            final TypedArray a = getTypedArray(attrs, defStyleAttr, defStyleRes);
            shouldExpand = a.getBoolean(R.styleable.ExpandableView_expanded, false);
            replaceHeaderWhenExpanded = a.getBoolean(R.styleable.ExpandableView_replaceHeaderWhenExpanded, false);
            a.recycle();
        } else {
            shouldExpand = false;
            replaceHeaderWhenExpanded = false;
        }
        internalCollapse(false);
        setOnClickListeners();

        header = new FrameLayout(context, attrs, defStyleAttr, defStyleRes);
        content = new FrameLayout(context, attrs, defStyleAttr, defStyleRes);

        addView(header, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        addView(content, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        if (shouldExpand) {
            internalExpand(false);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() == 2) {
            // adding header
            header.addView(child, index, params);
        } else if (getChildCount() == 3) {
            // adding content
            content.addView(child, index, params);
        } else {
            // adding internal
            super.addView(child, index, params);
        }
    }

    private void setOnClickListeners() {
        header.setOnClickListener(v -> {
            toggleExpandedState(false);
            if (onHeaderClicked != null) onHeaderClicked.run();
        });

        content.setOnClickListener(v -> {
            if (onContentClicked != null) onContentClicked.run();
        });
    }

    @NonNull
    private TypedArray getTypedArray(@NonNull AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ExpandableView, defStyleAttr, defStyleRes);
        return a;
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
            if (replaceHeaderWhenExpanded) header.setVisibility(GONE);
            content.setVisibility(VISIBLE);
        }
    }

    void internalCollapse(boolean animate) {
        expanded = false;
        if (animate) {

        } else {
            if (replaceHeaderWhenExpanded) header.setVisibility(VISIBLE);
            content.setVisibility(GONE);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
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
