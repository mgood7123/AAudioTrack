package smallville7123.UI;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.ViewHolder> {
    private static final String TAG = "GridViewAdapter";
    public GridLayoutManager manager;
    GridView gridview;

    public GridViewAdapter(GridView gridview) {
        this.gridview = gridview;
        setManager(gridview);
    }

    public void setManager(GridView gridview) {
        manager = new GridLayoutManager(
                gridview.mContext,            // context
                1
        ) {
            @Override
            protected boolean isLayoutRTL() {
                return false;
            }
        };
        gridview.setLayoutManager(manager);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        FrameLayout root;

        public ViewHolder(FrameLayout itemView) {
            super(itemView);
            root = itemView;
        }

        public void adjustDimensions() {
            ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            if (manager.getOrientation() == RecyclerView.VERTICAL) {
                p.height = Math.round(gridview.getHeight() / gridview.rowCount);
            } else {
                p.width = Math.round(gridview.getWidth() / gridview.rowCount);
            }
            root.setLayoutParams(p);
        }

        public void setItem(int position) {
            root.setVisibility(View.VISIBLE);
            root.addView(gridview.data.get(position));
        }

        public void setEmptyItem() {
            root.setVisibility(View.INVISIBLE);
            root.removeAllViews();
        }

        public void setOnClickListener() {
            if (gridview.onItemClickListener != null) {
                root.setOnClickListener(v -> {
                    if (gridview.onClickListener != null) {
                        gridview.onClickListener.onClick(v);
                    }
                    gridview.onItemClickListener.onClick(null);
                });
            }
        }

        public void setEmptyOnClickListener() {
            if (gridview.onClickListener != null) {
                root.setOnClickListener(v -> {
                    gridview.onClickListener.onClick(v);
                });
            } else {
                root.setOnClickListener(null);
                root.setClickable(false);
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.root.removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new FrameLayout(gridview.mContext));
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.adjustDimensions();
        holder.setEmptyOnClickListener();
        if (position < gridview.data.size()) {
            holder.setItem(position);
            holder.setOnClickListener();
        } else {
            holder.setEmptyItem();
        }
    }

    @Override
    public int getItemCount() {
        int itemSize = gridview.data.size();
        return Math.max(gridview.columnCount*gridview.rowCount, itemSize + (itemSize % (manager.getOrientation() == RecyclerView.VERTICAL ? gridview.columnCount : gridview.rowCount)));
    }
}