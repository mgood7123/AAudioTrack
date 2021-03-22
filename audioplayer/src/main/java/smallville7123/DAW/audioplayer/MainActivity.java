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
        AAudioTrack2.ChannelInterface channelInterface0 = audioPlayer.newChannel();
        channelInterface0.loop(true);
        channelInterface0.connectVST(AudioPlayer);
        FrameLayout frameLayout = new FrameLayout(this);
        ViewGroup.LayoutParams matchParent = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(frameLayout, matchParent);
        AudioPlayer.setup(frameLayout, this);

        AAudioTrack2.ChannelInterface channelInterface1 = audioPlayer.newSamplerChannel();
        channelInterface1.loop(true);
        channelInterface1.load(this, R.raw.audio_1d_drum, "wav");
        AAudioTrack2.ChannelInterface channelInterface2 = audioPlayer.newSamplerChannel();
        channelInterface2.loop(true);
        channelInterface2.load(this, R.raw.audio_4d_synth, "wav");
        channelInterface1.sendEvent(AAudioTrack2.EVENT_NOTE_ON);
        channelInterface2.sendEvent(AAudioTrack2.EVENT_NOTE_ON);
    }
}