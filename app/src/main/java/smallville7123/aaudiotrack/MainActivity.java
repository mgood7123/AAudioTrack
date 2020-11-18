package smallville7123.aaudiotrack;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    AAudioTrack audioTrack = new AAudioTrack();

    void u(UpdatingTextView updatingTextView) {
            updatingTextView.setText(
                            "Underruns:     " + audioTrack.getUnderrunCount() + "\n" +
                            "Current frame: " + audioTrack.getCurrentFrame() + "\n" +
                            "Total frames:  " + audioTrack.getTotalFrames() + "\n"
            );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UpdatingTextView updatingTextView = findViewById(R.id.updatingTextView);
        updatingTextView.addOnFirstDrawAction(() -> u(updatingTextView));
        updatingTextView.addOnDrawAction(() -> u(updatingTextView));
        audioTrack.load(this, R.raw.loop_00001313, "wav");
    }
}