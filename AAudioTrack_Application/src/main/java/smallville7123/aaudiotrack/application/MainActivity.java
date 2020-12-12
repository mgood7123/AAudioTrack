package smallville7123.aaudiotrack.application;

import android.os.Bundle;
import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import smallville7123.UI.Layout;
import smallville7123.UI.UpdatingImageProgressBar;
import smallville7123.UI.UpdatingTextView;
import smallville7123.aaudiotrack2.AAudioTrack2;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainActivity extends AppCompatActivity {
    AAudioTrack2 audioTrack = new AAudioTrack2();

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
        setContentView(linearLayout, Layout.matchParent);
        CPU_LOAD = findViewById(R.id.CPU_LOAD);
        CPU_LOAD.addOnFirstDrawAction(() -> u2(CPU_LOAD));
        CPU_LOAD.addOnDrawAction(() -> u2(CPU_LOAD));
        UpdatingTextView updatingTextView = findViewById(R.id.INFO);
        updatingTextView.addOnFirstDrawAction(() -> u(updatingTextView));
        updatingTextView.addOnDrawAction(() -> u(updatingTextView));


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
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}