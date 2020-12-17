cmake_minimum_required(VERSION 3.10.2)
project("synth")
add_library(synth SHARED native-lib.cpp)
target_link_libraries(synth android log)