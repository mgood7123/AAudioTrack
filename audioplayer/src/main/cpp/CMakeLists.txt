cmake_minimum_required(VERSION 3.10.2)
project("audio_player")
add_library(audio_player SHARED native-lib.cpp)

#find_package(AAudioTrack2 REQUIRED CONFIG)

target_link_libraries(audio_player android log)