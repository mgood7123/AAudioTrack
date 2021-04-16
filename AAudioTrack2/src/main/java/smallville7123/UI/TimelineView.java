package smallville7123.UI;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import smallville7123.AndroidDAW.SDK.UI.ScrollBar.CanvasDrawer;
import smallville7123.AndroidDAW.SDK.UI.ScrollBar.CanvasView;

public class TimelineView extends CanvasView {
    public TimelineView(@NonNull Context context) {
        super(context);
    }

    public TimelineView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimelineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimelineView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public class Row {
        class Column {
            int index;

            Column(int index) {
                this.index = index;
            }

            void draw(CanvasDrawer canvas, int width) {
                int x = index * columnWidth;
                int y = rows.indexOf(Row.this) * rowHeight;
                canvas.drawRect(x, y, width, rowHeight);
            }
        }
        ArrayList<Column> columns = new ArrayList<>();

        public void addColumn(int index) {
            columns.add(new Column(index));
        }
    }

    ArrayList<Row> rows = new ArrayList<>();

    public Row addRow() {
        Row row = new Row();
        rows.add(row);
        return row;
    }

    @Override
    public void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Row row;
        row = addRow();
        row.addColumn(0);
        row.addColumn(2);
        row.addColumn(4);
        row.addColumn(6);
        row.addColumn(8);
        row = addRow();
        row.addColumn(1);
        row.addColumn(3);
        row.addColumn(5);
        row.addColumn(7);
        row.addColumn(9);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createCanvas(w, h);
    }

    int columnWidth = 100;
    int rowHeight = 200;

    // a TimelineView draws things scaled
    // 1 pixel = X samples

    int sampleRate = 48000;
    int samplesPerPixel = 2048;

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDrawCanvas(CanvasDrawer canvas) {
        canvas.clear();
        canvas.savePaint();
        canvas.setPaint(CanvasDrawer.paintRed);
        for (Row row : rows) {
            for (Row.Column column : row.columns) {
                column.draw(canvas, columnWidth);
            }
        }
        canvas.restorePaint();
    }
}