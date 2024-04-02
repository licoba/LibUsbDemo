
# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# build script scope).
project("hidapi")

# 创建一个名为hidapi的共享库，并指定hid.c作为它的源文件。
add_library(hidapi SHARED hid.c)
target_include_directories(hidapi PRIVATE ../libusb)

target_link_libraries(hidapi libusb)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(hidapi
        android
        log)