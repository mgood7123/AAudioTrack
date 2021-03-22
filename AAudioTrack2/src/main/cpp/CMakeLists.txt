cmake_minimum_required(VERSION 3.10.2)

project("AAudioTrack2")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -O0 -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

# https://developer.android.com/ndk/guides/asan#cmake

# SDK SETUP

if(NOT COMMAND AndroidDAW_SDK_CMAKE_HELPER)
    add_subdirectory(AndroidDAW_SDK_CMAKE_HELPER)
endif()

SET_PROJECT_ROOT_AndroidDAW_SDK(${CMAKE_CURRENT_SOURCE_DIR}/../../../..)

import_library__AndroidDAW_Plugin()

# BUILD

add_library(engine SHARED
        ardour/AudioEngine/AudioBackend.cpp
        ardour/AudioEngine/AudioEngine.cpp
        ardour/Backends/AAudio.cpp
        smallville7123/MidiMap.cpp
)

target_link_libraries(
        engine
        aaudio
        log
        AndroidDAW_MIDI
        AndroidDAW_Plugin
)

add_library(AAudioTrack2 SHARED
        AAudioTrack.cpp other/JniHelpers.cpp
)

target_link_libraries(AAudioTrack2 engine)