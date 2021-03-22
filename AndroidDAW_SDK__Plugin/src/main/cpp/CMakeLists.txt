cmake_minimum_required(VERSION 3.10.2)

project("AndroidDAW_SDK_PLUGIN")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -O0 -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

# https://developer.android.com/ndk/guides/asan#cmake

add_library(AndroidDAW_PLUGIN SHARED
        empty.cpp
)

target_link_libraries(AndroidDAW_PLUGIN AndroidDAW_MIDI)