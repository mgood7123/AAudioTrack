package smallville7123.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GridView extends RecyclerView {
    private static final String TAG = "GridView";
    public boolean autoSizeRow;
    public boolean autoSizeColumn;
    public int rowHeight;
    public int columnWidth;
    public Context mContext;
    GridViewAdapter adapter;
    OnClickListener onClickListener;
    int rowCount;
    int columnCount;
    ArrayList<View> data;
    static int VERTICAL = RecyclerView.VERTICAL;
    static int HORIZONTAL = RecyclerView.HORIZONTAL;
    int mOrientation;


    ItemClickListener onItemClickListener;

    public interface ItemClickListener {
        void onClick(Object data);

    }

    public void setOnItemClick(ItemClickListener listener) {
        onItemClickListener = listener;
    }


    public GridView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GridView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GridView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        data = new ArrayList<>();
        rowCount = 1;
        columnCount = 1;
        adapter = new GridViewAdapter(this);
        setOrientation(VERTICAL);
        setAdapter(adapter);
    }

    public void setOrientation(int orientation) {
        if (mOrientation == orientation) return;
        mOrientation = orientation;
        adapter.manager.setOrientation(orientation);
        setRows(rowCount);
        setColumns(columnCount);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
        super.setOnClickListener(l);
    }

    public void setRows(int count) {
        if (mOrientation == RecyclerView.VERTICAL) {
            rowCount = count;
        } else {
            columnCount = count;
            adapter.manager.setSpanCount(count);
        }
    }

    public void setColumns(int count) {
        if (mOrientation == RecyclerView.VERTICAL) {
            columnCount = count;
            adapter.manager.setSpanCount(count);
        } else {
            rowCount = count;
        }
    }

    public void clear() {
        // remove all items from this RecycleView so they can be garbage collected if needed
        data.clear();
        adapter.notifyDataSetChanged();
    }
}