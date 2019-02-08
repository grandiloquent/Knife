#include <android/log.h>
#include <stdlib.h>
#include <jni.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>

#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, "main::", __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, "main::", __VA_ARGS__))

int64_t stat_size(struct stat *s) {
    return s->st_blocks * 512;

}

int64_t calculate_dir_size(int dfd) {
    int64_t size = 0;
    struct stat s;
    DIR *d;
    struct dirent *de;
    d = fdopendir(dfd);
    if (d == NULL) {
        close(dfd);
        return 0;
    }
    while ((de = readdir(d))) {
        const char *name = de->d_name;
        if (de->d_type == DT_DIR) {
            int subfd;
            if (name[0] == '.') {
                if (name[1] == 0)
                    continue;
                if ((name[1] == '.') && (name[2] == 0))
                    continue;
            }
            if (fstatat(dfd, name, &s, AT_SYMLINK_NOFOLLOW) == 0) {
                size += stat_size(&s);
            }
            subfd = openat(dfd, name, O_RDONLY | O_DIRECTORY);
            if (subfd >= 0) {
                size += calculate_dir_size(subfd);
            }

        } else {
            if (fstatat(dfd, name, &s, AT_SYMLINK_NOFOLLOW) == 0) {
                size += stat_size(&s);
            }
        }
    }
    closedir(d);
    return size;
}

static int unlink_recursive(const char *name) {
    struct stat st;
    DIR *dir;
    struct dirent *de;
    int fail = 0;
    if (lstat(name, &st) < 0) return -1;
    if (!S_ISDIR(st.st_mode))
        return unlink(name);
    dir = opendir(name);
    if (dir == NULL)
        return -1;
    errno = 0;
    while ((de = readdir(dir)) != NULL) {
        char dn[PATH_MAX];
        if (!strcmp(de->d_name, "..") || !strcmp(de->d_name, "."))
            continue;
        sprintf(dn, "%s/%s", name, de->d_name);
        if (unlink_recursive(dn) < 0) {
            fail = 1;
            break;
        }
        errno = 0;
    }
    if (fail || errno < 0) {
        int save = errno;
        closedir(dir);
        errno = save;
        return -1;
    }
    if (closedir(dir) < 0)
        return -1;
    return rmdir(name);

}

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