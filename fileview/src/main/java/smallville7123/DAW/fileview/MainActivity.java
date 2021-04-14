package smallville7123.DAW.fileview;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;

import smallville7123.UI.FileView;
import smallville7123.aaudiotrack2.PermissionRequester;

public class MainActivity extends Activity {
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
        permissionRequester.requestPermission (
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                1,
                permissionSet,
                null,
                null
        );
        setContentView(R.layout.activity_main);
        fileView = findViewById(R.id.fileView);
        fileView.enterDirectory("/");
    }
}