#include <android/log.h>
#include <jni.h>
#include <fcntl.h>
#include <errno.h>
#include <zip.h>
#include <stdio.h>
#include "file.h"
#include "str.h"

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "main::", __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, "main::", __VA_ARGS__))

JNIEXPORT jlong JNICALL
Java_euphoria_psycho_knife_DocumentUtils_calculateDirectory(JNIEnv *env, jclass type,
                                                            jstring dir_) {
    const char *dir = (*env)->GetStringUTFChars(env, dir_, 0);
    int dirfd = open(dir, O_DIRECTORY, O_RDONLY);
    if (dirfd < 0) {
        (*env)->ReleaseStringUTFChars(env, dir_, dir);
        return -1;
    } else {
        int64_t res = calculate_dir_size(dirfd);
        (*env)->ReleaseStringUTFChars(env, dir_, dir);
        return res;
    }
}

JNIEXPORT jint JNICALL
Java_euphoria_psycho_knife_DocumentUtils_deleteDirectories(JNIEnv *env, jclass type,
                                                           jobjectArray directories) {
    int count = (*env)->GetArrayLength(env, directories);
    int res = 0;
    for (int i = 0; i < count; i++) {
        jstring dir_ = (jstring) (*env)->GetObjectArrayElement(env, directories, i);
        const char *dir = (*env)->GetStringUTFChars(env, dir_, 0);
        //LOGI("directory %s", dir);
        int r = unlink_recursive(dir);
        if (r) {
            LOGE("Failed delete directory:%s. %d\n", dir, errno);
        }
        if (!res)res = r;
        (*env)->ReleaseStringUTFChars(env, dir_, dir);
    }
    return res;
}

JNIEXPORT void JNICALL
Java_euphoria_psycho_knife_DocumentUtils_extractToDirectory(JNIEnv *env, jclass type,
                                                            jstring filename_, jstring directory_) {
    const char *filename = (*env)->GetStringUTFChars(env, filename_, 0);
    const char *directory = (*env)->GetStringUTFChars(env, directory_, 0);
    zip_extract(filename, directory, NULL, NULL);
    (*env)->ReleaseStringUTFChars(env, filename_, filename);
    (*env)->ReleaseStringUTFChars(env, directory_, directory);
}

JNIEXPORT void JNICALL
Java_euphoria_psycho_knife_DocumentUtils_moveFilesByExtension(JNIEnv *env, jclass type,
                                                              jstring dirPath_,
                                                              jstring destDirName_) {
    const char *dirPath = (*env)->GetStringUTFChars(env, dirPath_, 0);
    const char *destDirName = (*env)->GetStringUTFChars(env, destDirName_, 0);
    int result = move_files(dirPath, destDirName);
    LOGE("moveFilesByExtension:%s. %s %d\n", dirPath, destDirName, result);

    (*env)->ReleaseStringUTFChars(env, dirPath_, dirPath);
    (*env)->ReleaseStringUTFChars(env, destDirName_, destDirName);
}

JNIEXPORT void JNICALL
Java_euphoria_psycho_knife_DocumentUtils_deleteLessFiles(JNIEnv *env, jclass type,
                                                         jstring fileName_) {
    const char *fileName = (*env)->GetStringUTFChars(env, fileName_, 0);

    const char *p = fileName + strlen(fileName);
    int n = 0;
    while (*(--p)) {
        if (*p == '/')
            break;
        n++;
    }
    char tmp[FILENAME_MAX];
    n = strlen(fileName) - n - 1;
    int i = 0;
    while (i < n) {
        tmp[i++] = fileName[i];
    }
    tmp[n] = 0;
    char *fn = strrchr(fileName, '/');
    if (fn)fn++;

    char *argz = 0;
    size_t argz_len = 0;
    if (list_files_by_dir(fileName, &argz, &argz_len) == 0) {
        char *f = 0;
        while ((f = argz_next(argz, argz_len, f))) {
            if (strcmp(f, fn) != 0)
                LOGE(f);
        }
    }
    (*env)->ReleaseStringUTFChars(env, fileName_, fileName);
}