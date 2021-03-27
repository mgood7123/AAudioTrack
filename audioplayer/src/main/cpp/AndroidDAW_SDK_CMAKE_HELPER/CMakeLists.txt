cmake_minimum_required(VERSION 3.4.1)

# empty macro, for checking import of cmake file
macro(AndroidDAW_SDK_CMAKE_HELPER)
endmacro()

macro(set_project_root_AndroidDAW_SDK project_root)
    set(PROJECT_ROOT_AndroidDAW_SDK ${project_root})
    set(PREFIX_PARENT_AndroidDAW_SDK EXPORTED_LIBRARIES)
    set(PREFIX_AndroidDAW_SDK ${PREFIX_PARENT_AndroidDAW_SDK}/${CMAKE_ANDROID_ARCH_ABI})
endmacro()

macro(AndroidDAW_SDK_CMAKE_HELPER_import_header SDK )
    set(MODULE_ROOT_AndroidDAW_SDK ${PROJECT_ROOT_AndroidDAW_SDK}/AndroidDAW_SDK__${SDK})
    include_directories(${MODULE_ROOT_AndroidDAW_SDK}/src/main/cpp/include)
endmacro()

macro(AndroidDAW_SDK_CMAKE_HELPER_import_library SDK )
    set(MODULE_ROOT_AndroidDAW_SDK ${PROJECT_ROOT_AndroidDAW_SDK}/AndroidDAW_SDK__${SDK})
    add_library(AndroidDAW_${SDK} SHARED IMPORTED)
    set_target_properties(AndroidDAW_${SDK}
            PROPERTIES IMPORTED_LOCATION
            ${MODULE_ROOT_AndroidDAW_SDK}/${PREFIX_AndroidDAW_SDK}/libAndroidDAW_${SDK}.so
            )
endmacro()

macro(import_library__AndroidDAW_MIDI)
    AndroidDAW_SDK_CMAKE_HELPER_import_header(MIDI)
    AndroidDAW_SDK_CMAKE_HELPER_import_library(MIDI)
endmacro()

macro(import_headers__AndroidDAW_SDK__RingBuffer)
    AndroidDAW_SDK_CMAKE_HELPER_import_header(RingBuffer)
endmacro()

macro(import_headers__AndroidDAW_SDK__Log)
    AndroidDAW_SDK_CMAKE_HELPER_import_header(Log)
endmacro()

macro(import_library__AndroidDAW_Plugin)
    import_headers__AndroidDAW_SDK__Log()
    import_headers__AndroidDAW_SDK__RingBuffer()
    import_library__AndroidDAW_MIDI()
    AndroidDAW_SDK_CMAKE_HELPER_import_header(Plugin)
    AndroidDAW_SDK_CMAKE_HELPER_import_library(Plugin)
endmacro()

macro(export_library_AndroidDAW_SDK SDK)
    add_custom_command(
            TARGET AndroidDAW_${SDK}
            POST_BUILD
            COMMAND mkdir ${PROJECT_ROOT_AndroidDAW_SDK}/${PREFIX_PARENT_AndroidDAW_SDK} || true
    )

    add_custom_command(
            TARGET AndroidDAW_${SDK}
            POST_BUILD
            COMMAND mkdir ${PROJECT_ROOT_AndroidDAW_SDK}/${PREFIX_AndroidDAW_SDK} || true
    )
    add_custom_command(
            TARGET AndroidDAW_${SDK}
            POST_BUILD
            COMMAND cp -v $<TARGET_FILE:AndroidDAW_${SDK}> ${PROJECT_ROOT_AndroidDAW_SDK}/${PREFIX_AndroidDAW_SDK}
    )
endmacro()