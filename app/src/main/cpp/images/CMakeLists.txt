
add_library(
        libimages
        STATIC
        resampler.cpp
        Resize.cc
        Image.cpp
        Rotate.cc
        Jpeg.cc
)
target_compile_definitions(libimages
        PRIVATE
        -DHAVE_JPEG)
