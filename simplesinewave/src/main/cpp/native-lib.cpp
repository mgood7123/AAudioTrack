#include <jni.h>
#include <string>
#include <AndroidDAW_SDK/plugin/Plugin.h>

class SineGenerator : public Plugin {
public:
    int dataIndex = 0;

    int plugin_type() override {
        return PLUGIN_TYPE_GENERATOR;
    }

    int write(HostInfo *hostInfo, PortUtils2 *in, Plugin *mixer, PortUtils2 *out,
              unsigned int samples) override {

        using namespace AndroidDAW_SDK__Plugin_TYPEDEFS;

        ENGINE_FORMAT * left = reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->l->buf);
        ENGINE_FORMAT * right = reinterpret_cast<ENGINE_FORMAT *>(out->ports.outputStereo->r->buf);

        for (int i = 0; i < out->ports.samplesPerChannel; i++) {
            left[i] = sin (2 * M_PI * dataIndex / 100.0);
            right[i] = sin (2 * M_PI * dataIndex / 100.0);
            dataIndex = (dataIndex + 1) % 100;
        }
        return PLUGIN_STOP;
    }
};

extern "C" JNIEXPORT jlong JNICALL
Java_smallville7123_DAW_simplesinewave_NativeApp_createNativeInstance(
        JNIEnv* env,
        jobject /* this */) {
    return reinterpret_cast<jlong>(new SineGenerator());
}