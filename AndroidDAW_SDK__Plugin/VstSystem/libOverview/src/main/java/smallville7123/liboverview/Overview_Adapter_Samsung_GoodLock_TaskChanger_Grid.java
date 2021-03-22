package smallville7123.liboverview;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.inflate;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid extends RecyclerView.Adapter<Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid.ViewHolder> {
    private static final String TAG = "Overview_Adapter_Samsun";
    public GridLayoutManager manager;
    Overview overview;
    int global_padding = 75;

    public Overview_Adapter_Samsung_GoodLock_TaskChanger_Grid(Overview overview) {
        this.overview = overview;
    }

    public void setManager(Overview overview) {
        manager = new GridLayoutManager(
            overview.mContext,            // context
            1,                            // spanCount (column count)
            LinearLayoutManager.VERTICAL, // orientation
        // if reverse layout is true, items stick to bottom instead of top
        // eg normal: row 1 is top, row 2 is bottom
        // reversed: row 1 is bottom, row 2 is top
            true                          // reverseLayout
        ) {
            @Override
            protected boolean isLayoutRTL() {
                // same as above but for columns
                // normal: column 1 is left, column 2 is right
                // RTL:    column 2 is left, column 1 is right
                return true;
            }
        };
        overview.setLayoutManager(manager);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        FrameLayout application;
        FrameLayout applicationFrame;
        ImageView applicationIcon;
        TextView applicationLabel;
        FrameLayout applicationContentBackground;
        ImageView applicationContent;
        Bitmap applicationContentBitmap;
        Object additionalData;

        public ViewHolder(FrameLayout itemView) {
            super(itemView);
            application = itemView;
            applicationFrame = itemView.findViewById(R.id.applicationFrame);
            applicationIcon = itemView.findViewById(R.id.applicationIcon);
            applicationLabel = itemView.findViewById(R.id.applicationLabel);
            applicationContentBackground = itemView.findViewById(R.id.applicationContentBackground);
            applicationContent = itemView.findViewById(R.id.applicationContent);

            applicationIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            applicationContent.setBackgroundColor(Color.BLACK);
        }

        public void adjustHeightByRowCount() {
            float containerHeight = overview.getHeight();
            ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            p.height = Math.round(containerHeight/overview.rowCount);
            application.setLayoutParams(p);
        }

        public void adjustForPadding() {
            application.setPadding(global_padding, global_padding, global_padding, global_padding);
        }

        public void setItem(int position) {
            applicationIcon.setVisibility(View.VISIBLE);
            applicationLabel.setVisibility(View.VISIBLE);
            applicationContent.setVisibility(View.VISIBLE);

            Overview.DataSet dataSet = overview.data.get(position);
            additionalData = dataSet.additionalData;

            applicationIcon.setImageDrawable(dataSet.icon);
            applicationLabel.setText(dataSet.title);
            applicationContentBitmap = dataSet.content;
            applicationContent.setImageBitmap(applicationContentBitmap);
        }

        public void setEmptyItem() {
            applicationIcon.setVisibility(View.INVISIBLE);
            applicationLabel.setVisibility(View.INVISIBLE);
            applicationContent.setVisibility(View.INVISIBLE);
        }

        public void setOnClickListener() {
            if (overview.onItemClickListener != null && additionalData != null) {
                applicationFrame.setOnClickListener(v -> {
                    if (overview.onClickListener != null) {
                        overview.onClickListener.onClick(v);
                    }
                    overview.onItemClickListener.onClick(additionalData);
                });
            }
        }

        public void setEmptyOnClickListener() {
            if (overview.onClickListener != null) {
                application.setOnClickListener(v -> {
                    overview.onClickListener.onClick(v);
                });
            } else {
                application.setOnClickListener(null);
                application.setClickable(false);
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        if (holder.applicationContentBitmap != null) holder.applicationContentBitmap.recycle();
        holder.applicationContent.setImageBitmap(null);
        super.onViewRecycled(holder);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder((FrameLayout) inflate(overview.mContext, R.layout.overview_layout_samsung_goodlock_taskchanger_grid, null));
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.adjustHeightByRowCount();
        holder.adjustForPadding();
        holder.setEmptyOnClickListener();
        if (position < overview.data.size()) {
            holder.setItem(position);
            holder.setOnClickListener();
        } else {
            holder.setEmptyItem();
        }
    }

    @Override
    public int getItemCount() {
        // ensure there is at least rowCount items
        int itemSize = overview.data.size();
        return Math.max(overview.columnCount*overview.rowCount, itemSize + (itemSize % overview.columnCount));
    }
}
