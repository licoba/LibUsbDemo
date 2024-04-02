
# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# build script scope).
project("hidapi")

# 这条指令告诉CMake创建一个名为"hidapi"的共享库
# "SHARED"关键字表示这是一个动态链接库，而不是静态库
# "hid.c"则是这个库的源文件。如果有多个源文件，可以在这里列出，用空格隔开。
add_library(hidapi SHARED hid.c)
#这条指令用来设置"hidapi"库的头文件搜索路径。
#"PRIVATE"关键字表示这些路径仅用于这个库自身的构建，不会被链接到这个库的其他目标继承。
#"../libusb"是相对于当前CMakeLists.txt文件位置的路径，指向包含头文件的"libusb"目录。
target_include_directories(hidapi PRIVATE ../libusb)
#这条指令告诉CMake，当链接"hidapi"库时，需要链接到"libusb"库。
#这意味着"hidapi"库依赖"libusb"库。"libusb"必须是在同一个CMake项目中定义的一个目标，或者是在系统中已经安装的库。
target_link_libraries(hidapi libusb)

# 在构建时，除了需要链接之前提到的"libusb"库，还需要链接Android系统库和日志库。
target_link_libraries(hidapi
        android
        log)