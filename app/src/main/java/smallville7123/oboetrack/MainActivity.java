package smallville7123.oboetrack;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    OboeTrack oboeTrack = new OboeTrack();

    void u(UpdatingTextView updatingTextView) {
            updatingTextView.setText(
                            "Underruns:     " + oboeTrack.getUnderrunCount() + "\n" +
                            "Current frame: " + oboeTrack.getCurrentFrame() + "\n" +
                            "Total frames:  " + oboeTrack.getTotalFrames() + "\n"
            );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UpdatingTextView updatingTextView = findViewById(R.id.updatingTextView);
        updatingTextView.addOnFirstDrawAction(() -> u(updatingTextView));
        updatingTextView.addOnDrawAction(() -> u(updatingTextView));
        oboeTrack.load(this, R.raw.loop_00001313, "wav");
    }
}