#ifndef EPUB_H__
#define EPUB_H__


#include "str.h"
#include <zip.h>
#include "log.h"

void pretty_name(const char *path);

static inline char *strip(char *buf) {
    char end1 = '-';
    char end2 = '/';

    char cs[] = {'\"', '<', '>', '|', ':', '*', '?', '\\', '/'};
    size_t len = sizeof(cs) / sizeof(cs[0]);

    char *tmp = buf;
    char c;
    while ((c = *tmp)) {
        // 移除副标题
        if (c == end1 || c == end2) {
            *tmp = 0;
            break;
        }
        // 用空格替换在Windows 系统中作为文件名的非法字符
        for (size_t i = 0; i < len; i++) {
            if (c == cs[i]) {
                *tmp = ' ';
            }
        }
        tmp++;
    }

    // 移除首尾空白
    buf = trim(buf);

    return buf;
}

char *move(char *s, char c) {
    size_t len = strlen(s);
    if (len == 0)return s;
    size_t i;
    int j = -1;
    for (i = 0; i < len; i++) {
        if (s[i] == c) {
            j = i;
            break;
        }
    }
    if (j == -1)return s;
    size_t k = len - j - 1;
    for (i = 0; i < k; i++) {
        s[i] = s[++j];
    }
    s[k] = 0;
    return s;
}

int parse_opf(const char *path, const char *buf) {

    // 新路径
    char *name = (char *) malloc(MAX_PATH);
    memset(name, 0, MAX_PATH);

    // 获取 Epub 标题
    char *title = between(buf, "<dc:title", "</dc:title>");

    if (title != NULL) {
        // 格式化 Epub 标题


        move(title, '>');
        // 获取目录路径
        strip(title);

        char *dir = strdup(path);
        size_t dir_len = strlen(path);

        while (dir_len) {
            dir_len--;
            if (dir[dir_len] == '/') {
                dir[dir_len] = 0;
                break;
            }
        }

        // 连接文件名
        strcat(name, dir);
        strcat(name, "/");
        strcat(name, title);

        // 释放
        free(dir);
        free(title);
    } else {
        free(name);
        return -1;
    }

    // 获取 Epub 作者
    char *creator = between(buf, "<dc:creator", "</dc:creator>");

    if (creator != NULL) {
        move(creator, '>');
        strcat(name, " - ");
        strcat(name, creator);
        free(creator);
    }
    strcat(name, ".epub");
    int rc = rename(path, (const char *) name);

    //struct stat s;stat(path, &s),
    // LOGE("%s %s %s\n", strerror(errno), path, name);

    free(name);
    return rc;
}

void pretty_name(const char *path) {
    unsigned char *buf = NULL;

    // 加载 Epub 文件
    struct zip_t *zip = zip_open(path, 0, 'r');
    if (zip == NULL) {
        printf("%s\n", "zip_open failed");
        return;
    }

    // 获取文件数目
    int i, n = zip_total_entries(zip);

    for (i = 0; i < n; ++i) {
        zip_entry_openbyindex(zip, i);
        {
            if (zip_entry_isdir(zip))
                continue;
        }
        const char *name = zip_entry_name(zip);

        // 加载元数据文件
        if (ends_with(name, ".opf")) {

            size_t bufSize = (size_t) zip_entry_size(zip);
            //!!! 分配内存
            buf = (unsigned char *) calloc(sizeof(unsigned char), bufSize);
            zip_entry_noallocread(zip, (void *) buf, bufSize);
            zip_entry_close(zip);
            break;

        } else {
            zip_entry_close(zip);
        }
    }
    zip_close(zip);
    parse_opf(path, (const char *) buf);
    if (buf != NULL)
        free(buf);
}

#endif