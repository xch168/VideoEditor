cmake_minimum_required(VERSION 3.4.1)


add_library( ffmpeg-cmd
             SHARED
             src/main/cpp/ffmpeg-cmd.cpp )

find_library( log-lib
              log )

target_link_libraries( ffmpeg-cmd
                       ${log-lib} )