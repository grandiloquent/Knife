cmake_minimum_required(VERSION 3.4.1)

add_library(markdown
        STATIC
        ${CMAKE_CURRENT_SOURCE_DIR}/autolink.c
        ${CMAKE_CURRENT_SOURCE_DIR}/buffer.c
        ${CMAKE_CURRENT_SOURCE_DIR}/houdini_href_e.c
        ${CMAKE_CURRENT_SOURCE_DIR}/houdini_html_e.c
        ${CMAKE_CURRENT_SOURCE_DIR}/html.c
        ${CMAKE_CURRENT_SOURCE_DIR}/html_smartypants.c
        ${CMAKE_CURRENT_SOURCE_DIR}/markdown.c
        ${CMAKE_CURRENT_SOURCE_DIR}/stack.c
        )
