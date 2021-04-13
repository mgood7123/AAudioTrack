cmake_minimum_required(VERSION 3.10.2)

project("AAudioTrack2")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -O0 -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

# SDK SETUP

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../../../../AndroidDAW_SDK/CMAKE_HELPER ${CMAKE_CURRENT_BINARY_DIR}/CMAKE_HELPER)
set_AndroidDAW_SDK_ROOT(${CMAKE_CURRENT_SOURCE_DIR}/../../../../AndroidDAW_SDK)

import_library__AndroidDAW_JNI_CPP_API()
import_library__AndroidDAW_JniHelpers()
import_library__AndroidDAW_Midi()
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
        AndroidDAW_JNI_CPP_API
        AndroidDAW_JniHelpers
        AndroidDAW_Midi
        AndroidDAW_Plugin
)

add_library(AAudioTrack2 SHARED AAudioTrack.cpp)

target_link_libraries(AAudioTrack2 engine)