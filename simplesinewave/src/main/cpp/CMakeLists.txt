cmake_minimum_required(VERSION 3.10.2)
project("synth")

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../../../../AndroidDAW_SDK/CMAKE_HELPER ${CMAKE_CURRENT_BINARY_DIR}/CMAKE_HELPER)
set_AndroidDAW_SDK_ROOT(${CMAKE_CURRENT_SOURCE_DIR}/../../../../AndroidDAW_SDK)

import_library__AndroidDAW_Midi()
import_library__AndroidDAW_Plugin()

add_library(synth SHARED native-lib.cpp)
target_link_libraries(synth AndroidDAW_Plugin AndroidDAW_Midi)