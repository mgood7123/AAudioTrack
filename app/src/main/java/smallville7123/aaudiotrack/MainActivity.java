package smallville7123.aaudiotrack;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends AppCompatActivity {
    AAudioTrack audioTrack = new AAudioTrack();

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

    void addRow(LinearLayout linearLayout, LinearLayout ID) {
        Pair<Pair<LinearLayout, ArrayList<ToggleButton>>, Pair<LinearLayout, ArrayList<ToggleButton>>> pair = new Pair<>(
                new Pair(new LinearLayout(this), new ArrayList<>()),
                new Pair(new LinearLayout(this), new ArrayList<>())
        );
        pair.first.first.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(pair.first.first, new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, 1f));
        for (int i = 0; i < 8; i++) {
            addToggleButton(pair.first, R.drawable.toggle);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sequencer);
        initLayout();
        initSequencer();
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

        Instrument [] instruments = {
                new Instrument(rows.get(0), "kick", audioTrack)
        };

        sequencer = new Sequencer(this, instruments);

        sequencer.setIsPlaying(false);
        new Thread(sequencer).start();
    }

    void play() {
        sequencer.setIsPlaying(true);
    }

    void pause() {
        sequencer.setIsPlaying(false);
    }
}