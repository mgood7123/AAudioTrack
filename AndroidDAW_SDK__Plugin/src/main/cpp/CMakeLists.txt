cmake_minimum_required(VERSION 3.10.2)

project("AndroidDAW_SDK__Plugin")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -O0 -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

# https://developer.android.com/ndk/guides/asan#cmake

include_directories(include)
add_library(AndroidDAW_Plugin SHARED TempoGrid.cpp)

add_subdirectory(AndroidDAW_SDK_CMAKE_HELPER)
SET_PROJECT_ROOT_AndroidDAW_SDK(${CMAKE_CURRENT_SOURCE_DIR}/../../..)

export_library_AndroidDAW_SDK(Plugin)
