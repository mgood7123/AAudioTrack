package smallville7123.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Arrays;

import smallville7123.aaudiotrack2.R;

import static android.widget.LinearLayout.VERTICAL;

public class FileView extends FrameLayout {
    public FileView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public FileView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FileView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public FileView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    GridView fileList;
    Context mContext;
    AttributeSet mAttrs;

    LayoutUtils.TextViewSize textSize;
    int textColor;
    Drawable background;
    MatchHeightTextView header;
    int chevronColor;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mAttrs = attrs;
        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FileView, 0, 0);
            textSize = LayoutUtils.getTextSizeAttributesSuitableForTextView(attributes, R.styleable.FileView_android_textSize, 30f);
            textColor = attributes.getColor(R.styleable.FileView_android_textColor, Color.WHITE);
            chevronColor = attributes.getColor(R.styleable.FileView_chevronColor, Color.WHITE);
            background = attributes.getDrawable(R.styleable.FileView_android_background);
            if (background == null) {
                background = new ColorDrawable(Color.DKGRAY);
            }
            attributes.recycle();
        } else {
            textColor = Color.WHITE;
            chevronColor = Color.WHITE;
            textSize = LayoutUtils.new_TextViewSize(30f);
            background = new ColorDrawable(Color.DKGRAY);
        }
        setBackground(background);
        inflate(context, R.layout.fileview_content, this);
        fileList = new GridView(context, attrs);
        fileList.setOrientation(VERTICAL);
        fileList.setColumns(1);
        fileList.setRows(20);
        header = findViewById(R.id.FileView_header_EditText);
        FrameLayout fl = findViewById(R.id.FileView_container);
        fl.addView(fileList);
        if (isInEditMode()) {
            enterDirectory("/");
        }
    }

    private static final int ROTATION_LEFT = 180;
    private static final int ROTATION_DOWN = 90;
    private static final int ROTATION_RIGHT = 0;
    private static final int ROTATION_UP = 270;

    class FileInfo {
        final int index;
        final FileInfo parent;
        final File file;
        final int depth;

        FileInfo(int index, FileInfo parent, File file, int depth) {
            this.index = index;
            this.parent = parent;
            this.file = file;
            this.depth = depth;
        }
    }

    ConstraintLayout add(FileInfo parentFileInfo, FileInfo fileInfo) {
        ConstraintLayout r = (ConstraintLayout) inflate(mContext, R.layout.fileview_row, null);
        ConstraintLayout row = r.findViewById(R.id.FileView_row);
        LinearLayout depth = row.findViewById(R.id.FileView_row_depth);
        for (int i = 0; i < fileInfo.depth; i++) {
            inflate(mContext, R.layout.fileview_depth_content, depth);
        }
        ImageView chevron = row.findViewById(R.id.FileView_row_chevron);
        if (fileInfo.file.isDirectory()) {
            chevron.setColorFilter(chevronColor);
            row.setOnClickListener(unused -> {
                Boolean expanded = (Boolean) row.getTag();
                if (expanded == null || expanded == false) {
                    row.setTag(true);
                    expandDirectory(fileInfo);
                    chevron.setRotation(ROTATION_DOWN);
                } else {
                    row.setTag(false);
                    collapseDirectory(fileInfo);
                    chevron.setRotation(ROTATION_RIGHT);
                }
            });
        } else {
            chevron.setVisibility(INVISIBLE);
        }
        TextView textView = row.findViewById(R.id.FileView_row_text);
        String name = fileInfo.file.getName();
        textView.setText(name.isEmpty() ? fileInfo.file.getAbsolutePath() : name);
        textView.setTextColor(textColor);
        fileList.data.add(fileInfo.index, new Pair<>(r, fileInfo));
        return row;
    }

    void clear() {
        fileList.data.clear();
    }

    void update() {
        fileList.adapter.notifyDataSetChanged();
    }

    private static final LinkOption noLinkOptions = null;

    public boolean enterDirectory(String root) {
        return enterDirectory(new File(root));
    }

    private File[] getFiles(File root) {
        if (root == null) return null;
        if (!root.canRead()) return null;
        if (!root.isDirectory()) return null;
        return root.listFiles();
    }

    private void sortAlphabeticallyIgnoreCase(File[] files) {
        if (files == null) return;
        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
    }

    int indexOf(File file) {
        String abs = file.getAbsolutePath();
        ArrayList<Pair<View, Object>> data = fileList.data;
        for (int i = 0, dataSize = data.size(); i < dataSize; i++) {
            Pair<View, Object> datum = data.get(i);
            String abs_ = ((FileInfo) datum.second).file.getAbsolutePath();
            if (abs_.contentEquals(abs)) return i;
        }
        return -1;
    }

    private boolean expandDirectory(FileInfo root) {
        File[] files = getFiles(root.file);
        if (files == null) return false;
        sortAlphabeticallyIgnoreCase(files);
        int index = indexOf(root.file);
        for (File file : files) {
            add(root, new FileInfo(++index, root, file, root.depth+1));
        }
        update();
        return true;
    }

    private boolean collapseDirectory(FileInfo root) {
        fileList.data.removeIf(viewObjectPair -> {
            FileInfo current = ((FileInfo) viewObjectPair.second);
            while (current != null) {
                FileInfo parent = current.parent;
                if (parent == null) {
                    return false;
                } else {
                    if (parent.file.getAbsolutePath()
                            .contentEquals(root.file.getAbsolutePath())) {
                        return true;
                    } else {
                        current = parent;
                    }
                }
            }
            return false;
        });
        update();
        return true;
    }

    private boolean enterDirectory(File root) {
        File[] files = getFiles(root);
        if (files == null) return false;
        clear();
        header.setText(root.getAbsolutePath());
        FileInfo f = new FileInfo(0, null, root, 0);
        add(null, f).callOnClick();
        return true;
    }
}
