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
    while (c = *tmp) {
        if (c == end1 || c == end2) {
            *tmp = 0;
            break;
        }
        for (size_t i = 0; i++; i < len) {
            if (c == cs[i]) {
                *tmp = ' ';
            }
        }
        tmp++;
    }

    buf = trim(buf);

    return buf;
}

int parse_opf(const char *path, const char *buf) {
    char *name = (char *) malloc(MAX_PATH);
    memset(name, 0, MAX_PATH);

    char *title = between(buf, "<dc:title", "</dc:title>");
    if (title != NULL) {
        strip(title);

        char *tmp = title;
        tmp++;
        char *dir = strdup(path);
        size_t dir_len = strlen(path);

        while (dir_len) {
            dir_len--;
            if (dir[dir_len] == '/') {
                dir[dir_len] = 0;
                break;
            }
        }
        strcat(name, dir);
        strcat(name, "/");
        strcat(name, tmp);
        free(dir);
        free(title);
    } else {
        free(name);
        return -1;
    }
    char *creator = between(buf, "<dc:creator", "</dc:creator>");

    if (creator != NULL) {
        char *tmp = creator;
        size_t i = 0;
        while (*tmp) {
            if (*tmp == '>') {
                i++;
                tmp++;
                break;
            }
            i++;
            tmp++;
        }
        memmove(creator, tmp, i);
        strcat(name, " - ");
        strcat(name, creator);
        free(creator);
    }
    strcat(name, ".epub");
    int rc = rename(path, (const char *) name);

    //struct stat s;stat(path, &s),
    LOGE("%s %s %s\n", strerror(errno), path, name);
    free(name);
    return 0;
}

void pretty_name(const char *path) {
    unsigned char *buf = NULL;

    struct zip_t *zip = zip_open(path, 0, 'r');
    if (!zip) {
        printf("%s\n", "zip_open failed");
        return;
    }
    int i, n = zip_total_entries(zip);
    int found = 0;
    for (i = 0; i < n; ++i) {
        zip_entry_openbyindex(zip, i);
        {
            if (zip_entry_isdir(zip))
                continue;
        }
        const char *name = zip_entry_name(zip);
        if (!found && ends_with(name, ".opf")) {

            size_t bufSize = zip_entry_size(zip);
            buf = (unsigned char *) calloc(sizeof(unsigned char), bufSize);
            zip_entry_noallocread(zip, (void *) buf, bufSize);

            found = 1;
        }
        zip_entry_close(zip);
        if (found)
            break;
    }
    zip_close(zip);
    parse_opf(path, (const char *) buf);
    if (buf != NULL)
        free(buf);
}

#endif