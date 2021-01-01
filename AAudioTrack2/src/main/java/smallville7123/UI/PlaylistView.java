package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import smallville7123.UI.Style.Android.ToggleRadioButton;
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
    LinearLayout focus;
    LinearLayout scrollBarAndTimeLine;
    LinearLayout scrollBarRightTop;
    LinearLayout linearLayoutHorizontal;
    LinearLayout patternView;
    GridView playlistView;
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
            channels = 3;
            channelHeight = 300.0f;
            fitChannelsToView = false;
//        }
        playlistView = new GridView(context, attrs);
        playlistView.setOrientation(VERTICAL);
        playlistView.setRows(channels);
        playlistView.setColumns(1);

        scrollBarTop = new LinearLayout(context, attrs);
        picker = new LinearLayout(context, attrs);
        focus = new LinearLayout(context, attrs);
        // FL Focus has a / at the start of it that is the width of a button
        // this will requires two views

        scrollBarAndTimeLine = new LinearLayout(context, attrs);
        // scroll bar takes up 2 views
        // the scroll bar, timeline, and Playlist resize themselves in accordance
        // to the window's width

        Resources res = getResources();
        Resources.Theme theme = context.getTheme();

        FL_GREY = res.getColor(R.color.FL_GREY, theme);
        bright_orange = res.getColor(R.color.bright_orange, theme);

        scrollBarTop.setBackgroundColor(Color.DKGRAY);
        picker.setBackgroundColor(Color.GREEN);
        focus.setBackgroundColor(FL_GREY);
        scrollBarAndTimeLine.setBackgroundColor(bright_orange);


        scrollBarRightTop = new LinearLayout(context, attrs);
        scrollBarRightTop.setBackgroundColor(Color.YELLOW);
        scrollBarRightBottom = new LinearLayout(context, attrs);
        scrollBarRightBottom.setBackgroundColor(Color.YELLOW);

        playlistView.setBackgroundColor(Color.DKGRAY);

        linearLayoutHorizontal = new LinearLayout(context, attrs);
        linearLayoutHorizontal.setOrientation(HORIZONTAL);
        linearLayoutVertical = new LinearLayout(context, attrs);
        linearLayoutVertical.setOrientation(VERTICAL);

        addView(linearLayoutVertical);

        scrollBarTop.addView(picker, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTop.addView(focus, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTop.addView(scrollBarAndTimeLine, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTop.addView(scrollBarRightTop, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayoutVertical.addView(scrollBarTop, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayoutVertical.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        patternView = new LinearLayout(context, attrs);
        patternView.setOrientation(VERTICAL);
        patternView.setBackgroundColor(Color.DKGRAY);

        linearLayoutHorizontal.addView(patternView, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        linearLayoutHorizontal.addView(playlistView, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        // by default, the FL playlist is 18 bars long (1 to 17)
        // with the 17th bar being visible, and the 18th being out of view
        //
        // |              WINDOW               |
        // |VISIBLE     VISIBLE     VISIBLE    |INVISIBLE   INVISIBLE
        // |1           ...         17         |18          ...
        // ||  |  |  |  |  |  |  |  |  |  |  | ||  |  |  |  |
        // |                                   |
        //
        // each pattern extends the grid by 18 (11 to 27)
        // this extension is dependant on the position of the final pattern placement

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
                playlistView.autoSizeRow = true;
            } else {
                playlistView.autoSizeRow = false;
                playlistView.rowSize = (int) channelHeight;
            }
            playlistView.data.add(new Pair(row, trackUI));
            playlistView.adapter.notifyDataSetChanged();
            return track;
        }
    }

    void newChannelButton(TrackUI trackUI, CharSequence label) {
//        if (mAttr != null) {
//            trackUI.channelButton = new LinearLayout(mContext, mAttr);
//            trackUI.textView = new TextView(mContext, mAttr);
//            trackUI.toggleRadioButton = new ToggleRadioButton(mContext, mAttr);
//        } else {
            trackUI.channelButton = new LinearLayout(mContext);
            trackUI.textView = new TextView(mContext);
            trackUI.toggleRadioButton = new ToggleRadioButton(mContext);
//        }

        trackUI.channelButton.setOnClickListener(view -> {
            if (channelContextMenu != null) {
                channelContextMenu.setAnchorView(view);
                channelContextMenu.show();
            }
        });
        trackUI.channelButton.setOrientation(VERTICAL);
        trackUI.textView.setText(label);
        trackUI.toggleRadioButton.setChecked(true);
        trackUI.toggleRadioButton.setTooltipText("Tap to disable this channel");
        trackUI.toggleRadioButton.setOnCheckedChangeListener((unused, checked) -> {
            setTooltipText(
                    "Tap to " +
                            (checked ? "disable" : "enable") +
                            " this channel"
            );
        });

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

        LayoutEngine engine = new LayoutEngine();
        LayoutEngine A = engine.newHeightRegion(height);
        A.height(scrollBarTop, 160);
        A.height(linearLayoutHorizontal, A.remainingHeight);

        LayoutEngine B = engine.newWidthRegion(width);
        if (width > 280) {
            if (width < 580) {
                B.width(picker, 200);
                if (B.remainingWidth < 80) {
                    B.width(scrollBarRightTop, B.remainingWidth);
                    B.width(focus, 0);
                    B.width(scrollBarAndTimeLine, 0);
                } else {
                    B.width(scrollBarRightTop, 80);
                    B.width(focus, B.remainingWidth);
                    B.width(scrollBarAndTimeLine, 0);
                }
            } else if (width == 580) {
                B.width(picker, 200);
                B.width(focus, 300);
                B.width(scrollBarAndTimeLine, 0);
                B.width(scrollBarRightTop, 80);
            } else if (width > 580) {
                B.width(picker, 200);
                B.width(focus, 300);
                B.width(scrollBarRightTop, 80);
                B.width(scrollBarAndTimeLine, B.remainingWidth);
            }
        } else if (width == 280) {
            B.width(scrollBarRightTop, 80);
            B.width(picker, 200);
            B.width(focus, 0);
            B.width(scrollBarAndTimeLine, 0);
        } else if (width < 280) {
            B.width(scrollBarRightTop, 80);
            B.width(picker, B.remainingWidth);
            B.width(focus, 0);
            B.width(scrollBarAndTimeLine, 0);
        }

        LayoutEngine C = engine.newWidthRegion(width);
        if (width > 280) {
            if (width < 580) {
                C.width(patternView, 200);
                if (C.remainingWidth < 80) {
                    C.width(scrollBarRightBottom, C.remainingWidth);
                    C.width(playlistView, 0);
                } else {
                    C.width(scrollBarRightBottom, 80);
                    C.width(playlistView, C.remainingWidth);
                }
            } else if (width == 580) {
                C.width(patternView, 200);
                C.width(playlistView, 300);
                C.width(scrollBarRightBottom, 80);
            } else if (width > 580) {
                C.width(patternView, 200);
                C.width(scrollBarRightBottom, 80);
                C.width(playlistView, C.remainingWidth);
            }
        } else if (width == 280) {
            C.width(scrollBarRightBottom, 80);
            C.width(patternView, 200);
            C.width(playlistView, 0);
        } else if (width < 280) {
            C.width(scrollBarRightBottom, 80);
            C.width(patternView, C.remainingWidth);
            C.width(playlistView, 0);
        }

        engine.execute();

        playlistView.setResizeUI((trackWidth, trackHeight, data) -> {
            Log.d(TAG, "trackWidth = [ " + (trackWidth) + "]");
            Log.d(TAG, "trackHeight = [ " + (trackHeight) + "]");
            TrackUI trackUI = (TrackUI) data.second;

            LayoutEngine engine_ = new LayoutEngine();
            LayoutEngine A_ = engine_.newWidthRegion(trackWidth);
            LayoutEngine B_ = engine_.newHeightRegion(trackHeight);
            A_.width(trackUI.channelButton, 300);
            A_.width(trackUI.clipView, A_.remainingWidth);
            B_.height(trackUI.toggleRadioButton, 80);
            B_.height(trackUI.textView, B_.remainingHeight);
            engine_.execute();
        });
    }
}
