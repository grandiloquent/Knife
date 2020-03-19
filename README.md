# Knife

* [x] 在内部储存和外部储存卡之间移动文件
* [x] 基于目录播放的视频播放器
* [ ] 基于目录播放的音乐播放器
* [x] 基于目录浏览的图片浏览器
* [x] Web 服务器，可用于在设备之间传输文件和播放视频
* [ ] 深度清理储存空间
* [x] 压缩和解压文件
* [x] 格式化 Epub 文件名

[下载](https://github.com/grandiloquent/Knife/blob/master/app/release/app-release.apk?raw=true)

<div>
<img width="360" src="screenshots\Screenshot_2019-02-11-14-30-05.png">
<img width="360" src="screenshots\Screenshot_2019-02-11-14-30-18.png">
<img width="360" src="screenshots\Screenshot_2019-02-11-14-31-50.png">
<img width="360" src="screenshots\Screenshot_2019-02-11-14-32-00.png">
<img width="360" src="screenshots\Screenshot_2019-02-11-14-32-15.png">
</div>

## 第三方类库

- https://github.com/kuba--/zip
- https://github.com/NanoHttpd/nanohttpd
- https://github.com/apache/commons-fileupload
- https://github.com/yhirose/cpp-httplib
- https://github.com/ram-on/SkyTube

## 编译 libjpeg-turbo

https://github.com/libjpeg-turbo/libjpeg-turbo/blob/master/BUILDING.md

1. `C:\msys64\msys2_shell.cmd -mingw64`
2. `cd C:/Codes/Android/Knife/app/src/main/cpp/libjpeg-turbo`
3. `NDK_PATH=C:/Users/psycho/AppData/Local/Android/Sdk/ndk-bundle`
4. `TOOLCHAIN=clang`
5. `ANDROID_VERSION=16`
6. `C:/Users/psycho/AppData/Local/Android/Sdk/cmake/3.6.4111459/bin/cmake.exe -G"Unix Makefiles" -DANDROID_ABI=armeabi-v7a -DANDROID_ARM_MODE=arm -DANDROID_PLATFORM=android-${ANDROID_VERSION} -DANDROID_TOOLCHAIN=${TOOLCHAIN} -DCMAKE_ASM_FLAGS="--target=arm-linux-androideabi${ANDROID_VERSION}" -DCMAKE_TOOLCHAIN_FILE=${NDK_PATH}/build/cmake/android.toolchain.cmake`
7. `make`

