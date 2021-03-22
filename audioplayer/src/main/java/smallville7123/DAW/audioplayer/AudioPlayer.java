package smallville7123.DAW.audioplayer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import smallville7123.vstmanager.core.VstActivity;

public class AudioPlayer extends VstActivity {
    static {
        System.loadLibrary("audio_player");
    }

    private native long createNativeInstance();
    private native long play(long nativeInstance);
    private native long pause(long nativeInstance);
    private long nativeInstance;

    @Override
    public long newNativeInstance() {
        nativeInstance = createNativeInstance();
        return nativeInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(new androidx.appcompat.widget.AppCompatCheckBox(this) {
            {
                setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) play(nativeInstance);
                    else pause(nativeInstance);
                });
            }
        });
    }
}
