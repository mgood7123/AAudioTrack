package smallville7123.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import smallville7123.aaudiotrack2.AAudioTrack2;
import smallville7123.aaudiotrack2.TrackGroup;
import smallville7123.aaudiotrack2.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

/*
TODO:

implementing the enable/disable channel button

implementing a file picker for the sampler plugin

implementing a scroll bar

implementing a visual sequence playback position indicator

implementing resolution pickers

implementing Solo/Mute buttons (maybe?)

implementing L/R channel audio level meters

implementing a BPM picker

 */

@SuppressLint("AppCompatCustomView")
public class PlaylistView extends FrameLayout {
    public PlaylistView(Context context) {
        super(context);
        init(context, null);
    }

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

    GridView trackGrid;
    Context mContext;
    int channels;
    float channelHeight;
    boolean fitChannelsToView;
    int nativeNoteResolution;
    int UINoteResolution;
    float noteWidth;
    boolean fitNotesToView;
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
        setupTrackContextMenu();
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
            channels = 4;
            channelHeight = Float.NaN;
            fitChannelsToView = true;
            nativeNoteResolution = 8;
            UINoteResolution = 4;
            noteWidth = Float.NaN;
            fitNotesToView = true;
//        }
        trackGrid = new GridView(context);
        trackGrid.setOrientation(VERTICAL);
        trackGrid.setRows(channels);
        addView(trackGrid);
        if (isInEditMode()) {
            TrackList list = newTrackList(null);
            addRow(list, "1");
            addRow(list, "2");
            addRow(list, "3");
            addRow(list, "4");
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
        Track track = trackList.newTrack(mContext, label);
        if (!isInEditMode()) {
            track.setNativeResolution(nativeNoteResolution);
        }
        track.setViewResolution(UINoteResolution);
        track.setMaxLength(nativeNoteResolution);
        return track;
    }

    public class TrackList extends smallville7123.aaudiotrack2.TrackList<Track> {
        Track newTrack(Context context, String label) {
            Track track = newTrack(new Track());

            track.mContext = context;
            track.trackGrid = new GridView(context) {
                @Override
                public void onScrolled(int dx, int dy) {
                    super.onScrolled(dx, dy);
                    if (!track.scrolling) {
                        track.scrolling = true;
                        for (Track track1 : trackArrayList) {
                            if (!track1.scrolling) {
                                track1.scrolling = true;
                                track1.trackGrid.scrollBy(dx, dy);
                                track1.scrolling = false;
                            }
                        }
                        track.scrolling = false;
                    }
                }
            };
            track.trackGrid.setOrientation(GridView.HORIZONTAL);
            track.trackGrid.setRows(1);
            LinearLayout row = new LinearLayout(mContext);
            row.setOrientation(HORIZONTAL);
            row.addView(new ToggleRadioButton(mContext) {
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
            }, Constants.wrapContent);
            row.addView(
                    newChannelButton(label),
                    new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 3f)
            );

            row.addView(track.trackGrid, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));

            if (fitChannelsToView || channelHeight == Float.NaN) {
                trackGrid.autoSizeRow = true;
            } else {
                trackGrid.autoSizeRow = false;
                trackGrid.rowHeight = (int) channelHeight;
            }
            trackGrid.data.add(row);
            trackGrid.adapter.notifyDataSetChanged();
            return track;
        }
    }

    Button newChannelButton(CharSequence label) {
        Button button = new Button(mContext) {
            {
                setText(label);
            }
        };
        button.setOnClickListener(unused -> {
            channelContextMenu.setAnchorView(button);
            channelContextMenu.show();
        });
        return button;
    }

    public class Track extends smallville7123.aaudiotrack2.Track {
        ArrayList<CompoundButton> compoundButtons = new ArrayList<>();
        public boolean scrolling = false;
        GridView trackGrid;
        int maxLength;
        Context mContext;

        @Override
        public boolean[] getData() {
            boolean[] data = new boolean[compoundButtons.size()];
            for (int i = 0; i < compoundButtons.size(); i++) {
                data[i] = compoundButtons.get(i).isChecked();
            }
            return data;
        }

        @Override
        public void setViewResolution(int size) {
            if (currentViewResolution != size) {
                // change this value to set the actual number
                // of displayed notes before the user will need
                // to scroll
                trackGrid.setColumns(size);
                currentViewResolution = size;
            }
        }

        public void setMaxLength(int size) {
            if (size == maxLength) return;
            if (size > maxLength) {
                for (int i = maxLength; i < size; i++) {
                    ToggleButton note = new ToggleButton(mContext);
                    note.setBackgroundResource(R.drawable.toggle);
                    note.setTextOn("");
                    note.setTextOff("");
                    note.setText("");
                    note.setOnLongClickListener(v -> {
                        trackContextMenu.setAnchorView(note);
                        trackContextMenu.show();
                        return true;
                    });
                    note.setOnCheckedChangeListener((b0, b1) -> {
                        setNoteData();
                    });
                    compoundButtons.add(note);
                    if (fitNotesToView || noteWidth == Float.NaN) {
                        trackGrid.autoSizeColumn = true;
                    } else {
                        trackGrid.autoSizeColumn = false;
                        trackGrid.columnWidth = (int) noteWidth;
                    }
                    trackGrid.data.add(note);
                    maxLength++;
                }
            } else {
                for (int i = (maxLength-1); i > (size-1); i--) {
                    compoundButtons.remove(i);
                    trackGrid.data.remove(trackGrid.data.size()-1);
                    maxLength--;
                }
            }
            trackGrid.adapter.notifyDataSetChanged();
        }
    }
}
