//
// Created by matthew good on 20/11/20.
//

#ifndef AAUDIOTRACK_LOG_H
#define AAUDIOTRACK_LOG_H

#ifdef __ANDROID__
#include <android/log.h> // output
#else
#include <stdio.h>
#define __android_log_print(priority, tag, ...) printf("[%s] %s: ", #priority, tag); printf(__VA_ARGS__); printf("\n")
#endif

#include <cstdlib> // abort

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "AudioEngine", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "AudioEngine", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, "AudioEngine", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AudioEngine", __VA_ARGS__)
#define LOGF(...) { \
__android_log_print(ANDROID_LOG_FATAL, "AudioEngine", __VA_ARGS__); \
abort(); \
}

#define LOGA_NO_REASON "no reason given"

#define LOGA_NO_ARGS(bool_condition, fmt) { \
if (!(bool_condition)) \
LOGF(\
"%s:%d: %s: Assertion '%s' failed. Additional Assertion information: " fmt, \
__FILE__, __LINE__, __FUNCTION__, \
#bool_condition \
); \
}

#define LOGA_WITH_ARGS(bool_condition, fmt, ...) { \
if (!(bool_condition)) \
LOGF(\
"%s:%d: %s: Assertion '%s' failed. Additional Assertion information: " fmt, \
__FILE__, __LINE__, __FUNCTION__, \
#bool_condition, __VA_ARGS__ \
); \
}

#endif //AAUDIOTRACK_LOG_H
