package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import smallville7123.UI.ScrollBarView.ScrollBarView;
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
    LinearLayout containerTop;
    LinearLayout picker;
    LinearLayout focus;
    LinearLayout scrollBarAndTimeLine;
    FrameLayout timeline;
    LinearLayout scrollBarContainerTop;
    FrameLayout scrollBarTopScrollLeft;
    ScrollBarView scrollBarTopScrollBar;
    FrameLayout scrollBarTopScrollRight;
    LinearLayout scrollBarContainerTopRight;
    FrameLayout zoom;
    FrameLayout scrollBarRightScrollUp;
    LinearLayout scrollBarContainerRight;
    ScrollBarView scrollBarRightScrollBar;
    FrameLayout scrollBarRightScrollDown;
    LinearLayout linearLayoutHorizontal;
    LinearLayout patternView;
    TwoWayNestedScrollView playlistView;

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

        containerTop = new LinearLayout(context, attrs);
        picker = new LinearLayout(context, attrs);
        focus = new LinearLayout(context, attrs);
        // FL Focus has a / at the start of it that is the width of a button
        // this will requires two views

        scrollBarAndTimeLine = new LinearLayout(context, attrs);
        // scroll bar takes up 2 views
        // the scroll bar, timeline, and Playlist resize themselves in accordance
        // to the window's width
        timeline = new FrameLayout(context, attrs);

        Resources res = getResources();
        Resources.Theme theme = context.getTheme();

        FL_GREY = res.getColor(R.color.FL_GREY, theme);
        bright_orange = res.getColor(R.color.bright_orange, theme);

        containerTop.setBackgroundColor(Color.DKGRAY);
        picker.setBackgroundColor(Color.GREEN);
        focus.setBackgroundColor(FL_GREY);

        scrollBarAndTimeLine.setOrientation(VERTICAL);

        scrollBarAndTimeLine.setBackgroundColor(bright_orange);
        timeline.setBackgroundColor(Color.BLUE);



        scrollBarContainerTop = new LinearLayout(context, attrs);
        scrollBarContainerTop.setOrientation(HORIZONTAL);
        scrollBarContainerTop.setBackgroundColor(Color.YELLOW);
        scrollBarTopScrollLeft = new FrameLayout(context, attrs);
        scrollBarTopScrollBar = new ScrollBarView(context, attrs);
        scrollBarTopScrollBar.setOrientation(ScrollBarView.HORIZONTAL);
        scrollBarTopScrollRight = new FrameLayout(context, attrs);

        scrollBarContainerTopRight = new LinearLayout(context, attrs);
        scrollBarContainerTopRight.setOrientation(VERTICAL);
        scrollBarContainerTopRight.setBackgroundColor(Color.YELLOW);
        zoom = new FrameLayout(context, attrs);
        scrollBarRightScrollUp = new FrameLayout(context, attrs);
        scrollBarContainerRight = new LinearLayout(context, attrs);
        scrollBarContainerRight.setOrientation(VERTICAL);
        scrollBarContainerRight.setBackgroundColor(Color.YELLOW);
        scrollBarRightScrollBar = new ScrollBarView(context, attrs);
        scrollBarRightScrollBar.setOrientation(ScrollBarView.VERTICAL);
        scrollBarRightScrollDown = new FrameLayout(context, attrs);

        linearLayoutHorizontal = new LinearLayout(context, attrs);
        linearLayoutHorizontal.setOrientation(HORIZONTAL);
        linearLayoutVertical = new LinearLayout(context, attrs);
        linearLayoutVertical.setOrientation(VERTICAL);

        addView(linearLayoutVertical);

        scrollBarContainerTop.addView(scrollBarTopScrollLeft, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTopScrollLeft.setBackgroundColor(Color.RED);
        scrollBarContainerTop.addView(scrollBarTopScrollBar, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTopScrollBar.setBackgroundColor(Color.GREEN);
        scrollBarContainerTop.addView(scrollBarTopScrollRight, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarTopScrollRight.setBackgroundColor(Color.MAGENTA);

        scrollBarContainerTopRight.addView(zoom, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        zoom.setBackgroundColor(Color.GRAY);
        scrollBarContainerTopRight.addView(scrollBarRightScrollUp, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        scrollBarRightScrollUp.setBackgroundColor(Color.RED);

        scrollBarContainerRight.addView(scrollBarRightScrollBar, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarRightScrollBar.setBackgroundColor(Color.GREEN);
        scrollBarContainerRight.addView(scrollBarRightScrollDown, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        scrollBarRightScrollDown.setBackgroundColor(Color.MAGENTA);

        scrollBarAndTimeLine.addView(scrollBarContainerTop, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        scrollBarAndTimeLine.addView(timeline, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));

        containerTop.addView(picker, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        containerTop.addView(focus, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        containerTop.addView(scrollBarAndTimeLine, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        containerTop.addView(scrollBarContainerTopRight, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayoutVertical.addView(containerTop, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayoutVertical.addView(linearLayoutHorizontal, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        patternView = new LinearLayout(context, attrs);
        patternView.setOrientation(VERTICAL);
        patternView.setBackgroundColor(Color.DKGRAY);

        linearLayoutHorizontal.addView(patternView, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

//        playlistView = new GridView(context, attrs) {
//            @Override
//            public void onScrolled(int dx, int dy) {
//                super.onScrolled(dx, dy);
////                scrollBarRightScrollBar.updateRelativePosition(dx, dy);
//            }
//        };
//        playlistView.setLayoutParams(new LayoutParams(0,0));
//        playlistView.setOrientation(VERTICAL);
//        playlistView.setRows(channels);
//        playlistView.setColumns(1);

        //
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
        //

        playlistView = new TwoWayNestedScrollView(context, attrs);

        playlistView.setBackgroundColor(Color.DKGRAY);

        scrollBarTopScrollBar.attachTo(playlistView);
        scrollBarRightScrollBar.attachTo(playlistView);

        FrameLayout frame = new FrameLayout(context, attrs);
        Button button = new Button(context, attrs);
        // 1000 width, 1000 height
        frame.addView(button, new LayoutParams(1000, 1000) {
            {
                leftMargin = 1000;
                topMargin = 1000;
            }
        });
        frame.setLayoutParams(new FrameLayout.LayoutParams(4000, 4000));
        playlistView.addView(frame);
        linearLayoutHorizontal.addView(playlistView, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        linearLayoutHorizontal.addView(scrollBarContainerRight, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));


        if (isInEditMode()) {
            TrackList list = newTrackList(null);
            addRow(list, "Track 1");
            addRow(list, "Track 2");
            addRow(list, "Track 3");
            addRow(list, "Track 4");
            addRow(list, "Track 5");
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
                    Log.d(TAG, "onOverScrolled() called with: scrollX = [" + scrollX + "], scrollY = [" + scrollY + "], clampedX = [" + clampedX + "], clampedY = [" + clampedY + "]");
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
//            if (scrollBarTopScrollBar.document == null) {
//                scrollBarTopScrollBar.attachTo(track.clipView);
//            }
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
//                playlistView.autoSizeRow = true;
            } else {
//                playlistView.autoSizeRow = false;
//                playlistView.rowSize = (int) channelHeight;
            }
//            playlistView.data.add(new Pair(row, trackUI));
//            playlistView.adapter.notifyDataSetChanged();
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth != 0 && measuredHeight != 0) {
            resizeUI(measuredWidth, measuredHeight);
        }
    }

    void resizeUI(int width, int height) {

        LayoutEngine engine = new LayoutEngine();
        LayoutEngine engineA = engine.newHeightRegion(height);
        engineA.height(containerTop, 160);
        LayoutEngine engineA_ = engine.newHeightRegion(engineA.remainingHeight);
        if (engineA.remainingHeight > 80) {
            engineA_.height(scrollBarRightScrollDown, 80);
            engineA_.height(scrollBarRightScrollBar, engineA_.remainingHeight);
        } else if (engineA.remainingHeight == 80) {
            engineA_.height(scrollBarRightScrollDown, 80);
            engineA_.height(scrollBarRightScrollBar, 0);
        } else if (engineA.remainingHeight < 80) {
            engineA_.height(scrollBarRightScrollDown, engineA.remainingHeight);
            engineA_.height(scrollBarRightScrollBar, 0);
        }
        engineA.height(linearLayoutHorizontal, engineA.remainingHeight);

        LayoutEngine engineB = engine.newWidthRegion(width);
        if (width > 280) {
            if (width < 580) {
                engineB.width(picker, 200);
                if (engineB.remainingWidth < 80) {
                    engineB.width(scrollBarContainerTopRight, engineB.remainingWidth);
                    engineB.width(focus, 0);
                    engineB.width(scrollBarAndTimeLine, 0);
                } else {
                    engineB.width(scrollBarContainerTopRight, 80);
                    engineB.width(focus, engineB.remainingWidth);
                    engineB.width(scrollBarAndTimeLine, 0);
                }
            } else if (width == 580) {
                engineB.width(picker, 200);
                engineB.width(focus, 300);
                engineB.width(scrollBarAndTimeLine, 0);
                engineB.width(scrollBarContainerTopRight, 80);
            } else if (width > 580) {
                engineB.width(picker, 200);
                engineB.width(focus, 300);
                engineB.width(scrollBarContainerTopRight, 80);
                LayoutEngine engineB_ = engine.newWidthRegion(engineB.remainingWidth);
                if (engineB.remainingWidth > 160) {
                    engineB_.width(scrollBarTopScrollLeft, 80);
                    engineB_.width(scrollBarTopScrollRight, 80);
                    engineB_.width(scrollBarTopScrollBar, engineB_.remainingWidth);
                } else if (engineB.remainingWidth == 160) {
                    engineB_.width(scrollBarTopScrollLeft, 80);
                    engineB_.width(scrollBarTopScrollRight, 80);
                    engineB_.width(scrollBarTopScrollBar, 0);
                } else if (engineB.remainingWidth < 160) {
                    if (engineB.remainingWidth < 80) {
                        engineB_.width(scrollBarTopScrollLeft, engineB.remainingWidth);
                        engineB_.width(scrollBarTopScrollRight, 0);
                        engineB_.width(scrollBarTopScrollBar, 0);
                    } else {
                        engineB_.width(scrollBarTopScrollLeft, 80);
                        engineB_.width(scrollBarTopScrollRight, engineB.remainingWidth);
                        engineB_.width(scrollBarTopScrollBar, 0);
                    }
                }
                engineB.width(scrollBarAndTimeLine, engineB.remainingWidth);
            }
        } else if (width == 280) {
            engineB.width(scrollBarContainerTopRight, 80);
            engineB.width(picker, 200);
            engineB.width(focus, 0);
            engineB.width(scrollBarAndTimeLine, 0);
        } else if (width < 280) {
            engineB.width(scrollBarContainerTopRight, 80);
            engineB.width(picker, engineB.remainingWidth);
            engineB.width(focus, 0);
            engineB.width(scrollBarAndTimeLine, 0);
        }

        LayoutEngine engineC = engine.newWidthRegion(width);
        if (width > 280) {
            if (width < 580) {
                engineC.width(patternView, 200);
                if (engineC.remainingWidth < 80) {
                    engineC.width(scrollBarContainerRight, engineC.remainingWidth);
                    engineC.width(playlistView, 0);
                } else {
                    engineC.width(scrollBarContainerRight, 80);
                    engineC.width(playlistView, engineC.remainingWidth);
                }
            } else if (width == 580) {
                engineC.width(patternView, 200);
                engineC.width(playlistView, 300);
                engineC.width(scrollBarContainerRight, 80);
            } else if (width > 580) {
                engineC.width(patternView, 200);
                engineC.width(scrollBarContainerRight, 80);
                engineC.width(playlistView, engineC.remainingWidth);
            }
        } else if (width == 280) {
            engineC.width(scrollBarContainerRight, 80);
            engineC.width(patternView, 200);
            engineC.width(playlistView, 0);
        } else if (width < 280) {
            engineC.width(scrollBarContainerRight, 80);
            engineC.width(patternView, engineC.remainingWidth);
            engineC.width(playlistView, 0);
        }

        engine.execute();

//        playlistView.setResizeUI((trackWidth, trackHeight, data) -> {
//            Log.d(TAG, "trackWidth = [" + (trackWidth) + "]");
//            Log.d(TAG, "trackHeight = [" + (trackHeight) + "]");
//            TrackUI trackUI = (TrackUI) data.second;
//
//            LayoutEngine engine_ = new LayoutEngine();
//            LayoutEngine engine_A = engine_.newWidthRegion(trackWidth);
//            LayoutEngine engine_B = engine_.newHeightRegion(trackHeight);
//            engine_A.width(trackUI.channelButton, 300);
//            engine_A.width(trackUI.clipView, engine_A.remainingWidth);
//            engine_B.height(trackUI.toggleRadioButton, 80);
//            engine_B.height(trackUI.textView, engine_B.remainingHeight);
//            engine_.execute();
//        });
    }
}
