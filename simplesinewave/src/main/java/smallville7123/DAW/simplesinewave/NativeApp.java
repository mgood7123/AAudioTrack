package smallville7123.DAW.simplesinewave;

import smallville7123.vstmanager.core.VstActivity;

public class NativeApp extends VstActivity {
    static {
        System.loadLibrary("synth");
    }

    private native long createNativeInstance();

    @Override
    public long newNativeInstance() {
        return createNativeInstance();
    }
}
