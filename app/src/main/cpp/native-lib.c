#include <android/log.h>
#include <pthread.h>
#include <jni.h>
#include <fcntl.h>
#include <errno.h>
#include <zip.h>
#include <stdio.h>
#include "file.h"
#include "str.h"
#include "markdown/markdown.h"
#include "markdown/html.h"
#include "epub.h"

#define LOGI(...) \
    ((void)__android_log_print(ANDROID_LOG_INFO, "main::", __VA_ARGS__))
#define LOGE(...) \
    ((void)__android_log_print(ANDROID_LOG_ERROR, "main::", __VA_ARGS__))

/*
static pthread_key_t mThreadKey;
static JavaVM* mJavaVM;

static void Android_JNI_ThreadDestroyed(void* value) {
    */
/* The thread is being destroyed, detach it from the Java VM and set the mThreadKey value to NULL as required */ /*
    JNIEnv *env = (JNIEnv*) value;
    if (env != NULL) {
        (*mJavaVM)->DetachCurrentThread(mJavaVM);
        pthread_setspecific(mThreadKey, NULL);
    }
}
JNIEnv* Android_JNI_GetEnv(void) {
    */
/* From http://developer.android.com/guide/practices/jni.html
     * All threads are Linux threads, scheduled by the kernel.
     * They're usually started from managed code (using Thread.start), but they can also be created elsewhere and then
     * attached to the JavaVM. For example, a thread started with pthread_create can be attached with the
     * JNI AttachCurrentThread or AttachCurrentThreadAsDaemon functions. Until a thread is attached, it has no JNIEnv,
     * and cannot make JNI calls.
     * Attaching a natively-created thread causes a java.lang.Thread object to be constructed and added to the "main"
     * ThreadGroup, making it visible to the debugger. Calling AttachCurrentThread on an already-attached thread
     * is a no-op.
     * Note: You can call this function any number of times for the same thread, there's no harm in it
     */
/*

    JNIEnv *env;
    int status = (*mJavaVM)->AttachCurrentThread(mJavaVM, &env, NULL);
    if(status < 0) {
        LOGE("failed to attach current thread");
        return 0;
    }

    return env;
}
int Android_JNI_SetupThread(void) {
    */
/* From http://developer.android.com/guide/practices/jni.html
     * Threads attached through JNI must call DetachCurrentThread before they exit. If coding this directly is awkward,
     * in Android 2.0 (Eclair) and higher you can use pthread_key_create to define a destructor function that will be
     * called before the thread exits, and call DetachCurrentThread from there. (Use that key with pthread_setspecific
     * to store the JNIEnv in thread-local-storage; that way it'll be passed into your destructor as the argument.)
     * Note: The destructor is not called unless the stored value is != NULL
     * Note: You can call this function any number of times for the same thread, there's no harm in it
     *       (except for some lost CPU cycles)
     */
/*
    JNIEnv *env = Android_JNI_GetEnv();
    pthread_setspecific(mThreadKey, (void*) env);
    return 1;
}
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv *env;
    mJavaVM = vm;
    LOGI("JNI_OnLoad called");
    if ((*mJavaVM)->GetEnv(mJavaVM, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("Failed to get the environment using GetEnv()");
        return -1;
    }
    */
/*
     * Create mThreadKey so we can keep track of the JNIEnv assigned to each thread
     * Refer to http://developer.android.com/guide/practices/design/jni.html for the rationale behind this
     */
/*
    if (pthread_key_create(&mThreadKey, Android_JNI_ThreadDestroyed)) {
        __android_log_print(ANDROID_LOG_ERROR, "SDL", "Error initializing pthread key");
    }
    else {
        Android_JNI_SetupThread();
    }

    return JNI_VERSION_1_4;
}*/

//////////////////////////

// |HTML_ESCAPE|HTML_SAFELINK
#define HTML_RENDER_FLAGS (HTML_USE_XHTML)

JNIEXPORT jstring JNICALL
Java_euphoria_psycho_knife_DocumentUtils_renderMarkdown(JNIEnv *env, jclass type, jstring text_) {
    const char *text = (*env)->GetStringUTFChars(env, text_, 0);
    struct sd_callbacks callbacks;
    struct html_renderopt options;
    sdhtml_renderer(&callbacks, &options, HTML_RENDER_FLAGS);
    struct sd_markdown *markdown = sd_markdown_new(
            MKDEXT_TABLES | MKDEXT_AUTOLINK | MKDEXT_FENCED_CODE, 16, &callbacks,
            &options);
    struct buf *output_buf;
    output_buf = bufnew(128);
    //result = kno_make_string(NULL,output_buf->size,output_buf->data);
    sd_markdown_render(
            output_buf,
            (const uint8_t *) (text),
            strlen(text),
            markdown);
    sd_markdown_free(markdown);

    const char *p = "<!DOCTYPE html>\n"
                    "<html>\n"
                    "<head>\n"
                    "    <meta charset=utf-8>\n"
                    "    <meta http-equiv=X-UA-Compatible content=\"chrome=1\">\n"
                    "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no,shrink-to-fit=no\">\n"

                    "    <link rel=stylesheet href=app.css>\n"
                    "</head>\n"
                    "<body>\n"
                    "%s"
                    "</body>\n"
                    "</html>";
    char *buf = malloc(output_buf->size + strlen(p));

    sprintf(buf, p, output_buf->data);
    bufrelease(output_buf);
    //    FILE *f = fopen(outFile, "w");
    //    fputs(buf, f);
    //    fflush(f);
    //    fclose(f);
    //    free(buf);

    (*env)->ReleaseStringUTFChars(env, text_, text);
    return (*env)->NewStringUTF(env, buf);
}

JNIEXPORT jlong JNICALL
Java_euphoria_psycho_knife_DocumentUtils_calculateDirectory(JNIEnv *env, jclass type,
                                                            jstring dir_) {
    const char *dir = (*env)->GetStringUTFChars(env, dir_, 0);
    int dirfd = open(dir, O_DIRECTORY | O_CLOEXEC, O_RDONLY);
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
        if (!res)
            res = r;
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
Java_euphoria_psycho_knife_DocumentUtils_formatEpubFileName(JNIEnv *env, jclass type,
                                                            jstring path_) {

const char *path=(*env)->GetStringUTFChars(env,path_,0);
pretty_name(path);
    (*env)->ReleaseStringUTFChars(env, path_, path);
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
        tmp[i] = fileName[i];
        i++;
    }
    tmp[n] = 0;
    char *fn = strrchr(fileName, '/');

    char *argz = 0;
    size_t argz_len = 0;
    if (list_files_by_dir(tmp, &argz, &argz_len) == 0) {
        char *f = 0;
        while ((f = argz_next(argz, argz_len, f))) {
            remove(f);
            if (strcmp(strrchr(f, '/'), fn) == 0)
                break;
        }
    }
    (*env)->ReleaseStringUTFChars(env, fileName_, fileName);
}

JNIEXPORT void JNICALL
Java_euphoria_psycho_knife_DocumentUtils_padFileNames(JNIEnv *env, jclass type,
                                                      jstring dir_, jint paddingLeftLength) {
    const char *dir = (*env)->GetStringUTFChars(env, dir_, 0);
    LOGE("%s\n", dir);
    rename_files(dir, (size_t) paddingLeftLength);
    (*env)->ReleaseStringUTFChars(env, dir_, dir);
}

JNIEXPORT void JNICALL
Java_euphoria_psycho_knife_DocumentUtils_createZipFromDirectory(JNIEnv *env, jclass type,
                                                                jstring dir_, jstring filename_) {
    const char *dir = (*env)->GetStringUTFChars(env, dir_, 0);
    const char *filename = (*env)->GetStringUTFChars(env, filename_, 0);
    struct zip_t *zip = zip_open(filename, ZIP_DEFAULT_COMPRESSION_LEVEL, 'w');

    size_t cap = 128;
    struct files list = {
            .files_name = malloc(sizeof(char *) * cap),
            .capacity = cap,
            .index = 0,
    };

    char *p = strdup(dir);

    list_directory(p, &list);
    for (size_t i = 0; i < list.index; i++) {

        zip_entry_open(zip, *(list.files_name + i) + strlen(dir) + 1);
        zip_entry_fwrite(zip, *(list.files_name + i));
        zip_entry_close(zip);
        free(*(list.files_name + i));
    }

    free(list.files_name);
    zip_close(zip);
    (*env)->ReleaseStringUTFChars(env, dir_, dir);
    (*env)->ReleaseStringUTFChars(env, filename_, filename);
}