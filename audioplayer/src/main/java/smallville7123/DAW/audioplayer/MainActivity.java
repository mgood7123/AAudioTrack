package smallville7123.DAW.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import smallville7123.aaudiotrack2.AAudioTrack2;

public class MainActivity extends AppCompatActivity {
    AAudioTrack2 audioPlayer = new AAudioTrack2();

    AudioPlayer AudioPlayer = new AudioPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioPlayer.deleteTemporaryFiles(this);
        audioPlayer.changeToDirectMode();
        AAudioTrack2.ChannelInterface channelInterface1 = audioPlayer.newChannel();
        channelInterface1.loop(true);
        channelInterface1.connectVST(AudioPlayer);
        FrameLayout frameLayout = new FrameLayout(this);
        ViewGroup.LayoutParams matchParent = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(frameLayout, matchParent);
        AudioPlayer.setup(frameLayout, this);
    }
}