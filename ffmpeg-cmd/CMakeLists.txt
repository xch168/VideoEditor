cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

# 添加头文件路径
include_directories(
        ./src/main/cpp
        ./src/main/cpp/ffmpeg
        ./src/main/cpp/include
)

# 定义源码所在目录
aux_source_directory(./src/main/cpp SRC)
aux_source_directory(./src/main/cpp/ffmpeg SRC_FFMPEG)

# 将 SRC_FFMPEG 添加到 SRC 中
list(APPEND SRC ${SRC_FFMPEG})

# 设置ffmpeg库所在路径的目录
set(distribution_DIR ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})

# 编译一个ffmpeg-cmd库
add_library(  ffmpeg-cmd   # 库名称
        SHARED             # 库类型
        ${SRC})            # 编译进库的源码

# 添加libffmpeg.so库
add_library( ffmpeg
        SHARED
        IMPORTED )
# 指定libffmpeg.so库的位置
set_target_properties( ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libffmpeg.so )

# 查找日志库
find_library(
        log-lib
        log )

# 将其他库链接到目标库ffmpeg-cmd
target_link_libraries( ffmpeg-cmd
        ffmpeg
        -landroid     # native_window
        -ljnigraphics # bitmap
        -lOpenSLES    # openSLES
        ${log-lib} )