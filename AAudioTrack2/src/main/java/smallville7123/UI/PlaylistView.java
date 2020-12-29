package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import smallville7123.aaudiotrack2.AAudioTrack2;
import smallville7123.aaudiotrack2.R;
import smallville7123.aaudiotrack2.TrackGroup;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

@SuppressLint("AppCompatCustomView")
public class PlaylistView extends FrameLayout {

    int FL_GREY;
    int bright_orange;

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

    LinearLayout linearLayoutVertical;
    LinearLayout scrollBarTop;
    LinearLayout picker;
    LinearLayout focusAndColor;
    LinearLayout scrollBarAndTimeLine;
    LinearLayout scrollBarRightTop;
    LinearLayout linearLayoutHorizontal;
    LinearLayout patternView;
    GridView trackGrid;
    LinearLayout scrollBarRightBottom;

    Context mContext;
    AttributeSet mAttr;
    int channels;
    float channelHeight;
    boolean fitChannelsToView;
    WindowsContextMenu channelContextMenu;
    WindowsContextMenu trackContextMenu;

    void setupChannelContextMenu() {
        channelContextMenu = new WindowsContextMenu(mContext);
        channelContextMenu.addSubMenu("Insert").subMenu.addItem("hi");
        channelContextMenu.addSubMenu("Replace").subMenu.addItem("hi again");
        channelContextMenu.addItem("Clone");
        channelContextMenu.addItem("Delete");
    }

    void setupTrackContextMenu() {
        trackContextMenu = new WindowsContextMenu(mContext);
        trackContextMenu.addSubMenu("Insert").subMenu.addItem("hi");
        trackContextMenu.addSubMenu("Replace").subMenu.addItem("hi again");
        trackContextMenu.addItem("Clone");
        trackContextMenu.addItem("Delete");
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mAttr = attrs;
        setupChannelContextMenu();
        setupTrackContextMenu();
//        if (attrs != null) {
//            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.sequencer, 0, 0);
//            channels = attributes.getInteger(R.styleable.sequencer_channels, 4);
//            channelHeight = attributes.getDimension(R.styleable.sequencer_channelHeight, Float.NaN);
//            fitChannelsToView = attributes.getBoolean(R.styleable.sequencer_fitChannelsToView, true);
//            nativeNoteResolution = attributes.getInteger(R.styleable.sequencer_nativeNotes, 8);
//            UINoteResolution = attributes.getInteger(R.styleable.sequencer_viewNotes, 4);
//            noteWidth = attributes.getDimension(R.styleable.sequencer_noteWidth, Float.NaN);
//            fitNotesToView = attributes.getBoolean(R.styleable.sequencer_fitNotesToView, true);
//            attributes.recycle();
//        } else {
            channels = 2;
            channelHeight = Float.NaN;
            fitChannelsToView = true;
//        }
        trackGrid = new GridView(context, attrs);
        trackGrid.setOrientation(VERTICAL);
        trackGrid.setRows(channels);

        scrollBarTop = new LinearLayout(context, attrs);
        picker = new LinearLayout(context, attrs);
        focusAndColor = new LinearLayout(context, attrs);
        scrollBarAndTimeLine = new LinearLayout(context, attrs);

        Resources res = getResources();
        Resources.Theme theme = context.getTheme();

        FL_GREY = res.getColor(R.color.FL_GREY, theme);
        bright_orange = res.getColor(R.color.bright_orange, theme);

        scrollBarTop.setBackgroundColor(Color.DKGRAY);
        picker.setBackgroundColor(Color.GREEN);
        focusAndColor.setBackgroundColor(FL_GREY);
        scrollBarAndTimeLine.setBackgroundColor(bright_orange);


        scrollBarRightTop = new LinearLayout(context, attrs);
        scrollBarRightTop.setBackgroundColor(Color.YELLOW);
        scrollBarRightBottom = new LinearLayout(context, attrs);
        scrollBarRightBottom.setBackgroundColor(Color.YELLOW);

        trackGrid.setBackgroundColor(Color.DKGRAY);

        linearLayoutHorizontal = new LinearLayout(context, attrs);
        linearLayoutHorizontal.setOrientation(HORIZONTAL);
        linearLayoutVertical = new LinearLayout(context, attrs);
        linearLayoutVertical.setOrientation(VERTICAL);

        addView(linearLayoutVertical);

        scrollBarTop.addView(picker, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTop.addView(focusAndColor, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTop.addView(scrollBarAndTimeLine, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTop.addView(scrollBarRightTop, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayoutVertical.addView(scrollBarTop, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayoutVertical.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        patternView = new LinearLayout(context, attrs);
        patternView.setOrientation(VERTICAL);
        patternView.setBackgroundColor(Color.DKGRAY);

        linearLayoutHorizontal.addView(patternView, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayoutHorizontal.addView(trackGrid, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayoutHorizontal.addView(scrollBarRightBottom, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));


        if (isInEditMode()) {
            TrackList list = newTrackList(null);
            addRow(list, "Track 1");
            addRow(list, "Track 2");
            addRow(list, "Track 3");
            addRow(list, "Track 4");
            addRow(list, "Track 5");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        Log.d(TAG, "measuredWidth = [ " + (measuredWidth) + "]");
        Log.d(TAG, "measuredHeight = [ " + (measuredHeight) + "]");
        if (measuredWidth != 0 && measuredHeight != 0) {
            resizeUI(measuredWidth, measuredHeight);
        }
    }

    AAudioTrack2 DAW;

    public void setDAW(AAudioTrack2 DAW) {
        this.DAW = DAW;
    }

    TrackGroup<TrackList> group = null;

    public TrackList newTrackList(AAudioTrack2 audioTrack) {
        if (group == null) {
            group = new TrackGroup<>(audioTrack);
        }
        return group.newTrackList(new TrackList());
    }

    public void removeTrackList(TrackList trackList) {
        group.removeTrackList(trackList);
    }

    public Track addRow(TrackList trackList, String label) {
        return trackList.newTrack(mContext, label);
    }

    private static final String TAG = "PlaylistView";

    class TrackUI {
        LinearLayout channelButton;
        TextView textView;
        ToggleRadioButton toggleRadioButton;
        ClipView clipView;
    }

    ArrayList<TrackUI> trackUIArrayList = new ArrayList<>();

    public class TrackList extends smallville7123.aaudiotrack2.TrackList<Track> {
        Track newTrack(Context context, String label) {
            Track track = newTrack(new Track());

            track.mContext = context;
            track.clipView = new ClipView(context) {
                @Override
                protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
                    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
                    if (!track.scrolling) {
                        track.scrolling = true;
                        for (Track track1 : trackArrayList) {
                            if (!track1.scrolling) {
                                track1.scrolling = true;
                                track1.clipView.scrollTo(scrollX, scrollY);
                                track1.scrolling = false;
                            }
                        }
                        track.scrolling = false;
                    }
                }
            };
            LinearLayout row = new LinearLayout(mContext);
            row.setOrientation(HORIZONTAL);

            TrackUI trackUI = new TrackUI();

            newChannelButton(trackUI, label);
            trackUI.clipView = track.clipView;

            trackUIArrayList.add(trackUI);
            row.addView(
                    trackUI.channelButton,
                    new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            );

            row.addView(trackUI.clipView, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

            if (fitChannelsToView || channelHeight == Float.NaN) {
                trackGrid.autoSizeRow = true;
            } else {
                trackGrid.autoSizeRow = false;
                trackGrid.rowHeight = (int) channelHeight;
            }
            trackGrid.data.add(new Pair(row, trackUI));
            trackGrid.adapter.notifyDataSetChanged();
            return track;
        }
    }

    void newChannelButton(TrackUI trackUI, CharSequence label) {
        trackUI.channelButton = new LinearLayout(mContext, mAttr) {
            {
                setOnClickListener(view -> {
                    if (channelContextMenu != null) {
                        channelContextMenu.setAnchorView(view);
                        channelContextMenu.show();
                    }
                });
            }
        };
        trackUI.channelButton.setOrientation(VERTICAL);

        trackUI.textView = new TextView(mContext, mAttr) {
            {
                setText(label);
            }
        };

        trackUI.toggleRadioButton = new ToggleRadioButton(mContext, mAttr) {
            {
                setChecked(true);
                setTooltipText("Tap to disable this channel");
                setOnCheckedChangeListener((unused, checked) -> {
                    setTooltipText(
                            "Tap to " +
                                    (checked ? "disable" : "enable") +
                                    " this channel"
                    );
                });
            }
        };

        trackUI.channelButton.addView(
                trackUI.textView,
                new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    {
                        gravity = Gravity.LEFT|Gravity.TOP;
                    }
                }
        );

        trackUI.channelButton.addView(
                trackUI.toggleRadioButton,
                new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    {
                        gravity = Gravity.RIGHT|Gravity.BOTTOM;
                    }
                }
        );

        trackUI.channelButton.setBackgroundColor(FL_GREY);
    }

    public class Track extends smallville7123.aaudiotrack2.Track {
        ArrayList<CompoundButton> compoundButtons = new ArrayList<>();
        public boolean scrolling = false;
        ClipView clipView;
        int maxLength;
        Context mContext;
    }

    void resizeUI(int width, int height) {

        ViewGroup.LayoutParams p;

        p = scrollBarTop.getLayoutParams();
        p.height = 160;
        scrollBarTop.setLayoutParams(p);

        p = linearLayoutHorizontal.getLayoutParams();
        int trackGridHeight = height - 160;
        p.height = trackGridHeight;
        linearLayoutHorizontal.setLayoutParams(p);

        p = picker.getLayoutParams();
        p.width = 200;
        picker.setLayoutParams(p);

        p = patternView.getLayoutParams();
        p.width = 200;
        patternView.setLayoutParams(p);

        p = trackGrid.getLayoutParams();
        int trackGridWidth = width - 200 - 80;
        p.width = trackGridWidth;
        trackGrid.setLayoutParams(p);

        p = focusAndColor.getLayoutParams();
        p.width = 300;
        focusAndColor.setLayoutParams(p);

        p = scrollBarAndTimeLine.getLayoutParams();
        p.width = width - 300 - 200 - 80;
        scrollBarAndTimeLine.setLayoutParams(p);

        trackGrid.setResizeUI((trackWidth, trackHeight, data) -> {
            Log.d(TAG, "trackWidth = [ " + (trackWidth) + "]");
            Log.d(TAG, "trackHeight = [ " + (trackHeight) + "]");
            TrackUI trackUI = (TrackUI) data.second;
            ViewGroup.LayoutParams trackUIParams = trackUI.channelButton.getLayoutParams();
            trackUIParams.width = 300;
            trackUI.channelButton.setLayoutParams(trackUIParams);

            trackUIParams = trackUI.textView.getLayoutParams();
            trackUIParams.height = trackHeight - 80;
            trackUI.textView.setLayoutParams(trackUIParams);

            trackUIParams = trackUI.toggleRadioButton.getLayoutParams();
            trackUIParams.height = 80;
            trackUI.toggleRadioButton.setLayoutParams(trackUIParams);

            trackUIParams = trackUI.clipView.getLayoutParams();
            trackUIParams.width = trackWidth - 300;
            trackUI.clipView.setLayoutParams(trackUIParams);
        });

        p = scrollBarRightTop.getLayoutParams();
        p.width = 80;
        scrollBarRightTop.setLayoutParams(p);

        p = scrollBarRightBottom.getLayoutParams();
        p.width = 80;
        scrollBarRightBottom.setLayoutParams(p);
    }
}
