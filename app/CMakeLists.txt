# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)


###############################################################################
# INSTALL DIRECTORIES
###############################################################################

set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../distribution)
add_library(libjpeg STATIC IMPORTED)
set_target_properties(libjpeg PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libjpeg-turbo/lib/${ANDROID_ABI}/libjpeg.a)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}  -Wall -Werror")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++1z -Wall -UNDEBUG")
# -lstdc++fs

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        src/main/cpp/file.c
        src/main/cpp/HttpServer.cpp
        src/main/cpp/Files.cpp
        src/main/cpp/Strings.cpp
        src/main/cpp/FileUtils.cpp
        src/main/cpp/Bridge.cpp
        # Provides a relative path to your source file(s).
        src/main/cpp/native-lib.c)

add_subdirectory(src/main/cpp/images)

include_directories(
        src/main/cpp/zip)

add_library(
        libminzip
        SHARED
        src/main/cpp/zip/zip.c)

add_library(
        libhttplib
        SHARED
        src/main/cpp/httplib.cc)

set_target_properties(libhttplib PROPERTIES LINKER_LANGUAGE CXX)

##　target_compile_definitions(lib_exe1 PRIVATE -DForExe1)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_include_directories(libimages PRIVATE
        ${distribution_DIR}/libjpeg-turbo/include)

target_link_libraries( # Specifies the target library.
        native-lib
        libhttplib
        libminzip
        libjpeg
        libimages
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})