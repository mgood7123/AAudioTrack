cmake_minimum_required(VERSION 3.10.2)

project("oboetrack")

add_subdirectory(oboe)
include_directories (oboe/include)

add_library(OboeTrack SHARED
        OboeTrack.cpp
        JniHelpers.cpp
        OboeAudioEngine/OboeAudioEngine.cpp
        OboeAudioEngine/OboeMixer.cpp
)
target_link_libraries(OboeTrack log oboe aaudio)