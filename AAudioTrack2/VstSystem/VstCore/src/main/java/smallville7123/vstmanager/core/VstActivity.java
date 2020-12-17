package smallville7123.vstmanager.core;

public abstract class VstActivity extends ReflectionActivity implements VstCallback {
    private static final String TAG = "VstActivity";
    public abstract long newNativeInstance();
}
