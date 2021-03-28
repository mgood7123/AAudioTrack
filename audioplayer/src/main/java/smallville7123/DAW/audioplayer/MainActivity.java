package smallville7123.DAW.audioplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import smallville7123.UI.FileView;
import smallville7123.aaudiotrack2.AAudioTrack2;
import smallville7123.aaudiotrack2.PermissionRequester;
import smallville7123.vstmanager.VstManager;

public class MainActivity extends Activity {
    AAudioTrack2 audioPlayer = new AAudioTrack2();

    AudioPlayer AudioPlayer = new AudioPlayer();

    PermissionRequester permissionRequester = new PermissionRequester();

    PermissionRequester.PermissionSet permissionSet = new PermissionRequester.PermissionSet(

            // on first request
            "Storage Permission is required",
            "grant",
            null,
            "deny",
            null,

            // on previously denied
            "Storage Permission is required",
            "grant",
            null,
            "deny",
            null
    );

    private FileView fileView;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioPlayer.deleteTemporaryFiles(this);
        audioPlayer.changeToDirectMode();
        AAudioTrack2.ChannelInterface channelInterface0 = audioPlayer.newChannel();
        channelInterface0.loop(true);

        permissionRequester.requestPermission (
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                1,
                permissionSet,
                null,
                null
        );

        channelInterface0.connectVST(AudioPlayer);
//        AudioPlayer.setupAndAttach(this);

        setContentView(R.layout.activity_main);

        fileView = findViewById(R.id.fileView);
        fileView.enterDirectory("/");
    }
}