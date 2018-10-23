# VideoEditor

```shell
#!/bin/bash

cd ffmpeg-3.4.4

# ndk环境    
export NDK=/Users/xch/debug/ndk/android-ndk-r17b
export SYSROOT=$NDK/platforms/android-21/arch-arm
export TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64
CPU=armv7-a

ISYSROOT=$NDK/sysroot
ASM=$ISYSROOT/usr/include/arm-linux-androideabi

# 要保存动态库的目录，这里保存在源码根目录下的android/armv7-a
export PREFIX=$(pwd)/android/$CPU
ADDI_CFLAGS="-marm"

function build_android
{
    echo "开始编译ffmpeg"

    ./configure \
        --target-os=linux \
        --prefix=$PREFIX \
        --enable-cross-compile \
        --enable-static \
        --disable-encoders \
        --disable-decoders \
        --disable-shared \
        --disable-muxers \
        --disable-demuxers \
        --disable-parsers \
        --disable-bsfs \
        --disable-protocols \
        --disable-filters \
        --disable-swscale \
        --disable-swresample \
        --disable-doc \
        --enable-ffmpeg \
        --disable-ffplay \
        --disable-ffprobe \
        --enable-avdevice \
        --disable-doc \
        --disable-symver \
        --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
        --arch=arm \
        --sysroot=$SYSROOT \
        --extra-cflags="-I$ASM -isysroot $ISYSROOT -D__ANDROID_API__=21 -U_FILE_OFFSET_BITS -Os -fPIC -DANDROID -Wno-deprecated -mfloat-abi=softfp -marm" \
        --extra-ldflags="$ADDI_LDFLAGS" \
        $ADDITIONAL_CONFIGURE_FLAG

    make clean

    make -j16
    make install

    # 打包
    $TOOLCHAIN/bin/arm-linux-androideabi-ld \
        -rpath-link=$SYSROOT/usr/lib \
        -L$SYSROOT/usr/lib \
        -L$PREFIX/lib \
        -soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
        $PREFIX/libffmpeg.so \
        libavcodec/libavcodec.a \
        libavfilter/libavfilter.a \
        libavformat/libavformat.a \
        libavutil/libavutil.a \
        libswresample/libswresample.a \
        libswscale/libswscale.a \
        -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
        $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a
 
    # strip 精简文件
    $TOOLCHAIN/bin/arm-linux-androideabi-strip  $PREFIX/libffmpeg.so

    echo "编译结束！"
}

build_android


```
