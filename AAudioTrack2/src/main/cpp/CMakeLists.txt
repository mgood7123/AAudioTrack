cmake_minimum_required(VERSION 3.10.2)

project("AAudioTrack2")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -Ofast -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

# https://developer.android.com/ndk/guides/asan#cmake

add_library(engine SHARED
        ardour/AudioEngine/AudioBackend.cpp
        ardour/AudioEngine/AudioEngine.cpp
        ardour/Backends/AAudio.cpp
        zrythm/audio/port_identifier.cpp
        zrythm/audio/port.cpp
        zrythm/audio/track_processor.cpp
        smallville7123/TempoGrid.cpp
)

target_link_libraries(engine log aaudio)

add_library(AAudioTrack2 SHARED
        AAudioTrack.cpp other/JniHelpers.cpp
)

target_link_libraries(AAudioTrack2 engine)