package smallville7123.liboverview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Overview extends RecyclerView {
    private static final String TAG = "Overview";
    public int type = Types.Samsung_GoodLock_TaskChanger_Grid;

    ItemClickListener onItemClickListener;

    public interface ItemClickListener {
        void onClick(Object data);
    }

    public void setOnItemClick(ItemClickListener listener) {
        onItemClickListener = listener;
    }

    public static class Types {
        static int Samsung_GoodLock_TaskChanger_Grid = 0;
        static int AndroidPie = 1;
        static int Zen_X_OS_AndroidPie = 2;
    }

    public Context mContext;
    Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid adapter;
    OnClickListener onClickListener;
    int rowCount = 2;
    int columnCount = 2;
    ArrayList<DataSet> data = new ArrayList<>();

    public Overview(@NonNull Context context) {
        super(context);
        init(context);
    }

    public Overview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Overview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        adapter = new Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid(this);
        setAdapter(adapter);
        adapter.setManager(this);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
        super.setOnClickListener(l);
    }

    public void setRows(int count) {
        rowCount = count;
    }

    public void setColumns(int count) {
        columnCount = count;
        adapter.manager.setSpanCount(count);
    }

    public static class DataSet {
        Drawable icon;
        CharSequence title;
        Bitmap content;
        Object additionalData;

        public DataSet(Drawable icon, CharSequence title, Bitmap content, Object additionalData) {
            this.icon = icon;
            this.title = title;
            this.content = content;
            this.additionalData = additionalData;
        }
    }

    public void addItem(Drawable icon, CharSequence title, Bitmap content) {
        addItem(icon, title, content, null);
    }

    public void addItem(Drawable icon, CharSequence title, Bitmap content, Object additionalData) {
        data.add(new DataSet(icon, title, content, additionalData));
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void clear() {
        // remove all items from this RecycleView so they can be garbage collected if needed
        data.clear();
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
