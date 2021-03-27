cmake_minimum_required(VERSION 3.10.2)
project("audio_player")

add_subdirectory(AndroidDAW_SDK_CMAKE_HELPER)
SET_PROJECT_ROOT_AndroidDAW_SDK(${CMAKE_CURRENT_SOURCE_DIR}/../../../..)
import_library__AndroidDAW_Plugin()

add_library(audio_player SHARED native-lib.cpp)
target_link_libraries(audio_player AndroidDAW_Plugin)