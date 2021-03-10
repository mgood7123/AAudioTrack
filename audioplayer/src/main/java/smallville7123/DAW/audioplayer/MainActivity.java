package smallville7123.DAW.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import smallville7123.aaudiotrack2.AAudioTrack2;

public class MainActivity extends AppCompatActivity {
    AAudioTrack2 audioPlayer = new AAudioTrack2();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioPlayer.deleteTemporaryFiles(this);
        audioPlayer.changeToDirectMode();
        AAudioTrack2.ChannelInterface channelInterface1 = audioPlayer.newSamplerChannel();
        channelInterface1.loop(true);
        channelInterface1.load(this, R.raw.audio_1d_drum, "wav");
        AAudioTrack2.ChannelInterface channelInterface2 = audioPlayer.newSamplerChannel();
        channelInterface2.loop(true);
        channelInterface2.load(this, R.raw.audio_4d_synth, "wav");
        AAudioTrack2.ChannelInterface channelInterface3 = audioPlayer.newSamplerChannel();
        channelInterface3.loop(true);
        channelInterface3.load(this, R.raw.loop_00001313, "wav");
        channelInterface1.sendEvent(AAudioTrack2.EVENT_NOTE_ON);
        channelInterface2.sendEvent(AAudioTrack2.EVENT_NOTE_ON);
        channelInterface3.sendEvent(AAudioTrack2.EVENT_NOTE_OFF);
    }
}