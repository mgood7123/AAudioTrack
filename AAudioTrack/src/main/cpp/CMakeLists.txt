cmake_minimum_required(VERSION 3.10.2)

project("AAudioTrack")

add_library(AAudioTrack SHARED
        AAudioTrack.cpp
        JniHelpers.cpp
        AAudioEngine/AAudioEngine.cpp
        AAudioEngine/AAudioMixer.cpp
)
target_link_libraries(AAudioTrack log aaudio)