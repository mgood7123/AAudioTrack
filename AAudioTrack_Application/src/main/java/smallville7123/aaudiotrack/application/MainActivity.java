package smallville7123.aaudiotrack.application;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import smallville7123.UI.Constants;
import smallville7123.UI.PatternView;
import smallville7123.UI.PianoRollView;
import smallville7123.UI.PlaylistView;
import smallville7123.UI.SequencerView;
import smallville7123.UI.UpdatingImageProgressBar;
import smallville7123.UI.UpdatingTextView;
import smallville7123.aaudiotrack2.AAudioTrack2;
import smallville7123.vstmanager.VstManager;

public class MainActivity extends AppCompatActivity {
    AAudioTrack2 audioTrack = new AAudioTrack2();

    void u(UpdatingTextView updatingTextView) {
        updatingTextView.setText(
                "Underruns:     " + audioTrack.getUnderrunCount() + "\n" +
                        "Sample rate:   " + audioTrack.getSampleRate() + "\n" +
                        "Channel count: " + audioTrack.getChannelCount() + "\n"
        );
    }

    void u2(UpdatingImageProgressBar updatingImageProgressBar) {
        updatingImageProgressBar.setProgress(audioTrack.getDSPLoad());
    }

    UpdatingImageProgressBar CPU_LOAD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.main_activity, null, false);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        setContentView(linearLayout, Constants.matchParent);
        CPU_LOAD = findViewById(R.id.CPU_LOAD);
        CPU_LOAD.addOnFirstDrawAction(() -> u2(CPU_LOAD));
        CPU_LOAD.addOnDrawAction(() -> u2(CPU_LOAD));
        UpdatingTextView updatingTextView = findViewById(R.id.INFO);
        updatingTextView.addOnFirstDrawAction(() -> u(updatingTextView));
        updatingTextView.addOnDrawAction(() -> u(updatingTextView));

        audioTrack.deleteTemporaryFiles(this);

        // configure mode change

        ((RadioGroup)findViewById(R.id.mode)).setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.mode_pattern:
                    audioTrack.changeToPatternMode();
                    break;
                case R.id.mode_song:
                    audioTrack.changeToSongMode();
                    break;
                default:
                    break;
            }
        });

        // configure the Playlist
//        PlaylistView playlistView = findViewById(R.id.playlist);
//        PlaylistView.TrackList playlist = playlistView.newTrackList(audioTrack);

        // configure the Sequencer
        SequencerView sequencerView = findViewById(R.id.sequencer);
        SequencerView.PatternList list = sequencerView.newPatternList(audioTrack);

        // bind
//        playlistView.addRow(playlist, "Track 1").bindPatternListToTrack(list);

        PatternView patternView = findViewById(R.id.patternView);
        patternView.setPatternList(list);

        audioTrack.load(
                sequencerView.addRow(list, "Kick").newSamplerChannel(),
                this, R.raw.kick, "wav"
        );
        audioTrack.load(
                sequencerView.addRow(list, "Snare").newSamplerChannel(),
                this, R.raw.snare_2, "wav"
        );
//        audioTrack.load(
//                sequencerView.addRow(list, "Loop").newSamplerChannel(),
//                this, R.raw.loop_00001313, "wav"
//        );
//        audioTrack.manager = new VstManager(this, findViewById(R.id.vstView));
//        audioTrack.loadVST(
//                sequencerView.addRow(list, "VST").newChannel(),
//                "smallville7123.DAW.simplesinewave"
//        );
    }
}