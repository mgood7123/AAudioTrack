package smallville7123.aaudiotrack.application;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.widget.ToggleButton;

public class Sequencer implements Runnable {
    private static final String TAG = "Sequencer";

    private int tempo = 140;
    private boolean playing = false;
    public Instrument[] instruments;
    Activity activity;

    public Sequencer(Activity activity, Instrument[] instruments) {
        this.instruments = instruments;
        this.activity = activity;
    }

    public void setIsPlaying(boolean playing) {
        this.playing = playing;
    }

    private void startSequence(Instrument[] instruments) {
        Log.e(TAG, "run: loop sequence start");
        main:
        while (true) {
            for (int i = 0; i < 8; i++) {
                if (!playing) {
                    for (Instrument instrument : instruments) {
                        instrument.sound.pause();
                    }
                    break main;
                }
                for (Instrument instrument : instruments) {

                    Pair<ToggleButton, ToggleButton> buttonP = null;
                    Pair<ToggleButton, ToggleButton> buttonC = null;
                    buttonP = instrument.getButtonAt(i == 0 ? 7 : i-1);
                    buttonC = instrument.getButtonAt(i);
                    Pair<ToggleButton, ToggleButton> finalButtonP = buttonP;
                    Pair<ToggleButton, ToggleButton> finalButtonC = buttonC;
                    activity.runOnUiThread(() -> {
                        if (finalButtonP != null) finalButtonP.second.setChecked(false);
                        finalButtonC.second.setChecked(true);
                    });
                    if (buttonC.first.isChecked()) {
                        instrument.sound.resetPlayHead();
                        instrument.sound.resume();
                    }
                }
                try {
                    Thread.sleep((1000 * 60) / 400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e(TAG, "run: loop sequence end");
    }

    @Override
    public void run() {
        while (true) {
            Log.e(TAG, "run: loop start");
            for (int i = 0; i < 8; i++) {
                for (Instrument instrument : instruments) {
                    instrument.getButtonAt(i).second.setChecked(false);
                }
            }
            while (!playing);
            startSequence(this.instruments);
            Log.e(TAG, "run: loop end");
        }
    }
}