
cmake_minimum_required(VERSION 3.22.1)
project("libusb")
# 将子目录的头文件包含到目标中
include_directories(os .)
# 生成动态库的名称、类型、资源文件
add_library(libusb SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        core.c
        descriptor.c
        hotplug.c
        io.c
        sync.c
        strerror.c
        os/linux_usbfs.c
        os/events_posix.c
        os/threads_posix.c
        os/linux_netlink.c
        jni_libusb.cpp
        )

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(libusb
        # List libraries link to the target library
        android
        log)