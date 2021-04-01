package smallville7123.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Rfc822Tokenizer;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

import smallville7123.AndroidDAW.SDK.jni_cpp_api.JNI_CPP_API;
import smallville7123.aaudiotrack2.R;

import static android.widget.LinearLayout.VERTICAL;

public class FileView extends FrameLayout {
    private static final String TAG = "FileView";

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
    SLACASET header;
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
        setup_IME_ACTION_handler();
        setup_AutoCompletion();
        if (isInEditMode()) {
            enterDirectory("/");
        }
    }

    void setup_IME_ACTION_handler() {
        header.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                enterDirectory(v.getText().toString());
                header.dismissSoftKeyboard();
                return true;
            }
            return true;
        });
    }

    abstract static class ConvertRunnable<F, T> {
        abstract T convert(F input);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    <F, T> T[] convertArrayType(F[] from, Class to, ConvertRunnable<F, T> convertRunnable) {
        T toArray[] = (T[]) Array.newInstance(to, from.length);
        for (int i = 0; i < toArray.length; i++) {
            toArray[i] = convertRunnable.convert(from[i]);
        }
        return toArray;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (header.isAttachedToWindow()) header.showDropDown();
            else post(this);
        }
    };

    void setup_AutoCompletion() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            String dirName;

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.contains("/")) {

                    dirName = JNI_CPP_API.dirname(str);

                    File[] files = getDirectories(new File(dirName));
                    if (files == null) return;
                    sortAlphabeticallyIgnoreCase(files);

                    String[] names = convertArrayType(files, String.class, new ConvertRunnable<File, String>() {
                        @Override
                        String convert(File input) {
                            return input.getName();
                        }
                    });

                    header.setAdapter(
                            new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item, names)
                    );
                }
                post(runnable);
            }
        };

        header.setTokenizer(new PACET.StringTokenizer('/', '/'));
        header.addTextChangedListener(header.convertToTextWatcher(textWatcher));
        header.setThreshold(1);
        // setValidator causes EditView to hang
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

    ConstraintLayout add(FileInfo fileInfo) {
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

    public boolean enterDirectory(String root) {
        return enterDirectory(new File(root));
    }

    private boolean exists(File root) {
        return root != null && root.exists();
    }

    private boolean isReadable(File root) {
        return exists(root) && root.canRead();
    }

    private boolean isDirectory(File root) {
        return isReadable(root) && root.isDirectory();
    }

    private File[] keepIf(File[] files, Predicate<File> filter) {
        if (files == null) return null;
        ArrayList<File> directories = new ArrayList<>(Arrays.asList(files));
        directories.removeIf(filter.negate());
        return directories.toArray(new File[0]);
    }

    private File[] removeIf(File[] files, Predicate<File> filter) {
        return keepIf(files, filter.negate());
    }

    private File[] getDirectories(File root) {
        if (isDirectory(root)) {
            return keepIf(root.listFiles(), File::isDirectory);
        }
        return null;
    }

    private File[] getFiles(File root) {
        if (isDirectory(root)) {
            return removeIf(root.listFiles(), File::isDirectory);
        }
        return null;
    }

    private File[] getFilesAndDirectories(File root) {
        return isDirectory(root) ? root.listFiles() : null;
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
        File[] files = getFilesAndDirectories(root.file);
        if (files == null) return false;
        sortAlphabeticallyIgnoreCase(files);
        int index = indexOf(root.file);
        for (File file : files) {
            add(new FileInfo(++index, root, file, root.depth+1));
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

    File cwd;

    private boolean enterDirectory(File root) {
        File[] files = getFilesAndDirectories(root);
        if (files == null) return false;
        cwd = root;
        clear();
        header.setText(cwd.getAbsolutePath());
        add(new FileInfo(0, null, cwd, 0)).callOnClick();
        return true;
    }
}
