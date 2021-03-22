cmake_minimum_required(VERSION 3.10.2)

project("AndroidDAW_SDK_MIDI")

set(CMAKE_CXX_FLAGS_DEBUG   "${CMAKE_CXX_FLAGS_DEBUG}   --optimize -O0 -g3")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} --optimize -Ofast -g0")

# https://developer.android.com/ndk/guides/asan#cmake

include(CheckIncludeFiles)

include_directories(include)

check_include_files(unistd.h HAVE_UNISTD_H)
check_include_files(humdrum.h HAVE_HUMDRUM_H)
check_include_files(sys/io.h HAVE_SYS_IO_H)

add_library(AndroidDAW_MIDI SHARED
        src/Options.cpp
        src/Binasc.cpp
        src/MidiEvent.cpp
        src/MidiEventList.cpp
        src/MidiFile.cpp
        src/MidiMessage.cpp
)

add_subdirectory(AndroidDAW_SDK_CMAKE_HELPER)
SET_PROJECT_ROOT_AndroidDAW_SDK(${CMAKE_CURRENT_SOURCE_DIR}/../../..)

export_library_AndroidDAW_SDK(MIDI)
