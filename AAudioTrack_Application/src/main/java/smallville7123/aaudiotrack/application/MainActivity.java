package smallville7123.aaudiotrack.application;

import android.os.Bundle;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import smallville7123.aaudiotrack2.AAudioTrack2;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends AppCompatActivity {
    AAudioTrack2 audioTrack = new AAudioTrack2();

    static ViewGroup.LayoutParams matchParent = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);

    ToggleButton addToggleButton(Pair<LinearLayout, ArrayList<ToggleButton>> pair, @DrawableRes int id, LinearLayout.LayoutParams params) {
        ToggleButton buttonA = new ToggleButton(this);
        buttonA.setBackgroundResource(id);
        buttonA.setTextOn("");
        buttonA.setTextOff("");
        buttonA.setText("");
        pair.second.add(buttonA);
        pair.first.addView(buttonA, params);
        return buttonA;
    }

    ToggleButton addToggleButton(Pair<LinearLayout, ArrayList<ToggleButton>> pair, @DrawableRes int id) {
        return addToggleButton(pair, id, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
    }

    ArrayList<Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>>> rows = new ArrayList<>();

    boolean[] data = new boolean[8];

    void addRow(LinearLayout linearLayout, LinearLayout ID) {
        Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> pair = new Pair<>(
                new Pair(new LinearLayout(this), new ArrayList<>()),
                new Pair(new LinearLayout(this), new ArrayList<>())
        );
        pair.first.first.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(pair.first.first, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        for (int i = 0; i < 8; i++) {
            ToggleButton b = addToggleButton(pair.first, R.drawable.toggle);
            data[i] = false;
            b.setTag(i);
            int finalI = i;
            b.setOnCheckedChangeListener((compoundButton, b1) -> {
                data[finalI] = b1;
                audioTrack.setNoteData(data);
            });
        }
        pair.second.first.setOrientation(LinearLayout.HORIZONTAL);
        ID.addView(pair.second.first, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        for (int i = 0; i < 8; i++) {
            addToggleButton(pair.second, R.drawable.toggle).setClickable(false);
        }
        rows.add(pair);
    }

    ToggleButton playback;

    private void initLayout() {
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.sequencer, null, false);
        playback = linearLayout.findViewById(R.id.playbackButton);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout, matchParent);
        LinearLayout sequencer = linearLayout.findViewById(R.id.sequenceLayout);
        LinearLayout sequencerID = linearLayout.findViewById(R.id.sequenceLayoutID);
        addRow(sequencer, sequencerID);
    }

    void u(UpdatingTextView updatingTextView) {
        updatingTextView.setText(
                "Underruns:     " + audioTrack.getUnderrunCount() + "\n" +
                        "Current frame: " + audioTrack.getCurrentFrame() + "\n" +
                        "Total frames:  " + audioTrack.getTotalFrames() + "\n" +
                        "Sample rate:   " + audioTrack.getSampleRate() + "\n" +
                        "Channel count: " + audioTrack.getChannelCount() + "\n"
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sequencer);
        initLayout();
        initSequencer();
        UpdatingTextView updatingTextView = findViewById(R.id.INFO);
        updatingTextView.addOnFirstDrawAction(() -> u(updatingTextView));
        updatingTextView.addOnDrawAction(() -> u(updatingTextView));
    }

    Sequencer sequencer;

    private void initSequencer() {
        playback.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                play();
            } else {
                pause();
            }
        });

        audioTrack.load(this, R.raw.kick, "wav");
        audioTrack.loop(false);
        new Thread(() -> {
            while (true) {
                if (!rows.isEmpty()) {
                    ArrayList<ToggleButton> toggleButtonArrayList = rows.get(0).second.second;
                    for (int i = 0; i < toggleButtonArrayList.size(); i++) {
                        int finalI = i;
                        MainActivity.this.runOnUiThread(
                                () -> toggleButtonArrayList
                                        .get(finalI)
                                        .setChecked(
                                                audioTrack.isNotePlaying(finalI)
                                        )
                        );
                    }
                }
                try {
                    // sleep 1 nanosecond
                    Thread.sleep(0, 500_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void play() {
        sequencer.setIsPlaying(true);
    }

    void pause() {
        sequencer.setIsPlaying(false);
    }
}