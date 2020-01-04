#include <assert.h>
#include <memory.h>
#include <malloc.h>
#include <asm/errno.h>
#include <sys/stat.h>
#include <dirent.h>
#include <stdio.h>
#include <unistd.h>
#include "file.h"
#include <fcntl.h>
#include <errno.h>
#include "log.h"
#include <stdlib.h>


#  define D_NAMLEN(dirent) (strlen((dirent)->d_name))


error_t argz_append(char **pargz, size_t *pargz_len, const char *buf, size_t buf_len) {
    size_t argz_len;
    char *argz;
    assert(pargz);
    assert(pargz_len);
    assert((*pargz && *pargz_len) || (!*pargz && !*pargz_len));
    if (buf_len == 0)return 0;
    argz_len = *pargz_len + buf_len;
    argz = (char *) realloc(*pargz, argz_len);
    if (!argz)
        return ENOMEM;
    memcpy(argz + *pargz_len, buf, buf_len);
    *pargz = argz;
    *pargz_len = argz_len;
    return 0;
}

error_t argz_insert(char **pargz, size_t *pargz_len, char *before, const char *entry) {
    assert(pargz);
    assert(pargz_len);
    assert(entry && *entry);
    if (!before)
        return argz_append(pargz, pargz_len, entry, 1 + strlen(entry));
    while ((before > *pargz) && (before[-1] != '\0'))
        --before;
    {
        size_t entry_len = 1 + strlen(entry);
        size_t argz_len = *pargz_len + entry_len;
        size_t offset = before - *pargz;
        char *argz = (char *) realloc(*pargz, argz_len);
        if (!argz)return ENOMEM;
        before = argz + offset;
        memmove(before + entry_len, before, *pargz_len - offset);
        memcpy(before, entry, entry_len);
        *pargz = argz;
        *pargz_len = argz_len;
    }
    return 0;
}

static int argz_insertinorder(char **pargz,
                              size_t *pargz_len,
                              const char *entry
) {
    char *before = 0;
    assert(pargz);
    assert(pargz_len);
    assert(entry && *entry);
    if (*pargz)
        while ((before = argz_next(*pargz, *pargz_len, before))) {
            int cmp = strcmp(entry, before);
            if (cmp < 0)break;
            if (cmp == 0)return 0;
        }
    return argz_insert(pargz, pargz_len, before, entry);
}

static int argz_insertdir(char **pargz,
                          size_t *pargz_len,
                          const char *dirnam,
                          struct dirent *dp) {
    char *buf = 0;
    size_t buf_len = 0;
    char *end = 0;
    size_t end_offset = 0;
    size_t dir_len = 0;
    int errors = 0;
    assert(pargz);
    assert(pargz_len);
    assert(dp);
    dir_len = strlen(dirnam);
    end = dp->d_name + D_NAMLEN(dp);
//  {
//    char *p;
//    for (p = end; p - 1 > dp->d_name; --p)
//      if (strchr(".0123456789", p[-1]) == 0)
//        break;
//    if (*p == '.')
//      end = p;
//  }
//  {
//    char *p;
//    for (p = end - 1; p > dp->d_name; --p)
//      if (*p == '.') {
//        end = p;
//        break;
//      }
//  }
    end_offset = end - dp->d_name;
    buf_len = dir_len + 1 + end_offset;
    buf = malloc((1 + buf_len) * sizeof(char));
    if (!buf)
        return ++errors;
    assert(buf);
    strcpy(buf, dirnam);
    strcat(buf, "/");
    strncat(buf, dp->d_name, end_offset);
    buf[buf_len] = '\0';
    if (argz_insertinorder(pargz, pargz_len, buf) != 0)
        ++errors;
    free(buf);
    return errors;
}

char *argz_next(char *argz, size_t argz_len, const char *entry) {
    assert((argz && argz_len) || (!argz && !argz_len));
    if (entry) {
        assert((!argz && !argz_len) || ((argz <= entry) && (entry < (argz + argz_len))));
        entry = 1 + strchr(entry, '\0');
        return (entry >= argz + argz_len) ? 0 : (char *) entry;
    } else {
        if (argz_len > 0)
            return argz;
        else return 0;
    }
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

int create_directory(const char *p) {
    struct stat s;
    if (stat(p, &s) == 0 && S_ISDIR(s.st_mode)) {
        return 0;
    } else {
        return mkdir(p, 0777);
    }
}

int
list_files_by_dir(const char *dirnam, char **pargz, size_t *pargz_len) {
    DIR *dirp = 0;
    int errors = 0;
    assert (dirnam && *dirnam);
    assert (pargz);
    assert (pargz_len);
    assert (dirnam[strlen(dirnam) - 1] != '/');
    dirp = opendir(dirnam);
    if (dirp) {
        struct dirent *dp = 0;
        while ((dp = readdir(dirp))) {
            if (dp->d_name[0] != '.')
                if (argz_insertdir(pargz, pargz_len, dirnam, dp)) {
                    ++errors;
                    break;
                }
        }
        closedir(dirp);
    } else
        ++errors;
    return errors;
}

int list_directory(char *path, struct files *list) {
    DIR *dir = opendir(path);
    if (dir) {
        struct dirent *dp;
        struct stat s;
        while ((dp = readdir(dir))) {
            if (strcmp(dp->d_name, ".") == 0
                || strcmp(dp->d_name, "..") == 0)
                continue;
            size_t len = strlen(path) + 2 + strlen(dp->d_name);
            char *buf = malloc(len);
            memset(buf, 0, len);
            strcat(buf, path);
            strcat(buf, "/");
            strcat(buf, dp->d_name);
            if (stat(buf, &s) == -1) {
                free(buf);
                continue;
            }
            if (S_ISDIR(s.st_mode)) {
                list_directory(buf, list);
            } else {
                if (list->index + 1 >= list->capacity) {
                    list->capacity = list->capacity * 2;
                    list->files_name = realloc(list->files_name, sizeof(char *) * list->capacity);
                }
                //printf("%d %s\n", list->index, buf);
                *(list->files_name + list->index++) = buf;
            }
        }
        closedir(dir);
    }
    free(path);
    return 0;
}

int move_files(const char *dfd, const char *dir_name) {
    struct stat s;
    DIR *d;
    struct dirent *de;
    d = opendir(dfd);
    if (d == NULL) {
        close((int) dfd);
        return 0;
    }
    //=====>>>
    char dir[FILENAME_MAX];
    memset(dir, 0, FILENAME_MAX);
    strcat(dir, dfd);
    strcat(dir, "/");
    strcat(dir, dir_name);
    create_directory(dir);
    //=====>>>
    while ((de = readdir(d))) {
        const char *name = de->d_name;
        //=====>>>
        char p[FILENAME_MAX];
        memset(p, '\0', sizeof(p));
        strcat(p, dfd);
        strcat(p, "/");
        strcat(p, name);
        //=====>>>
        if (name[0] == '.') {
            if (name[1] == 0)continue;
            if ((name[1] == '.' && name[2] == 0))continue;
        }
        stat(p, &s);
        if (S_ISREG(s.st_mode)) {
            char *e = strrchr(p, '.');
            if (!e || strlen(e) <= 1)continue;
            //=====>>>
            char tmp[FILENAME_MAX];
            strcpy(tmp, e + 1);
            if (tmp[0] >= 'a' && tmp[0] <= 'z')
                tmp[0] ^= 32;
            //=====>>>
            //=====>>>
            char n[FILENAME_MAX];
            memset(n, '\0', FILENAME_MAX);
            strcat(n, dir);
            strcat(n, "/");
            strcat(n, tmp);
            //=====>>>
            int ret = create_directory(n);
            LOGD("create_directory: %s === %d.\n", n, ret);
            strcat(n, "/");
            strcat(n, de->d_name);
            rename(p, n);
        }
    }
    return 1;
}

static int pad_num(const char *s, char *buf, size_t pad_len) {
    char c;
    int found = 0;
    char t[PATH_MAX];
    memset(t, 0, PATH_MAX);
    int count = 0;
    int offset = 0;
    int last = 0;
    const char *saved = s;
    while ((c = *s)) {
        offset++;
        if (!found) {
            if (c >= '0' && c <= '9') {
                found = 1;
            }
        }
        if (found) {
            if (c < '0' || c > '9') {
                break;
            } else {
                t[count++] = c;
            }
        }
        s++;
    }
    if (count == 0 || count >= pad_len)
        return -1;
    // if last char is number fix the offset
    if (offset == strlen(saved)) {
        last = 1;
    }
    int saved_offset = offset;
    offset = offset - count - 1;
    if (last) {
        offset++;
    }
    // before numbers
    for (size_t i = 0; i < offset; i++) {
        *(buf + i) = *(saved + i);
    }
    // pad 0
    for (size_t i = 0; i < pad_len - count; i++) {
        *(buf + offset + i) = '0';
    }
    // numbers
    offset = offset + pad_len - count;
    for (size_t i = 0; i < count; i++) {
        *(buf + offset + i) = t[i];
    }
    // after numbers
    if (!last) {
        saved_offset--;
        int dif = strlen(saved) - saved_offset;
        if (dif) {
            offset = offset + count;
            for (size_t i = 0; i < dif; i++) {
                *(buf + offset + i) = *(saved + saved_offset + i);
                // printf("%c\n", *(buf + offset + i));
            }
        }
    }
    return 0;
}

int rename_files(const char *dir, size_t pad_len) {
    DIR *directory;
    struct dirent *ent;
    int len;
    char file[PATH_MAX];
    char target[PATH_MAX];
    char file_name[PATH_MAX];
    int ret;
    struct stat stat_buf;
    directory = opendir(dir);
    if (directory == NULL) {
        return -1;
    }
    while ((ent = readdir(directory)) != NULL) {
        if (!strcmp(ent->d_name, ".") || !strcmp(ent->d_name, ".."))
            continue;
        len = strlen(dir) + 1 + strlen(ent->d_name) + 1;
        if (len > PATH_MAX) {
            ret = -1;
            goto out;
        }
        snprintf(file, PATH_MAX, "%s/%s", dir, ent->d_name);
        if (stat(file, &stat_buf) || !S_ISREG(stat_buf.st_mode))
            continue;
        memset(file_name, 0, PATH_MAX);
        if (pad_num(ent->d_name, file_name, pad_len) == -1)
            continue;
        snprintf(target, PATH_MAX, "%s/%s", dir, file_name);
        rename(file, target);
        LOGD("%s -> %s\n", file, target);
    }
    out:
    closedir(directory);
    return 0;
}


///////////////////////////
int Delete(const char *fullPath) {
    struct stat s;
    if (stat(fullPath, &s) != 0)return -1;
    if (!S_ISDIR(s.st_mode)) {
        return unlink(fullPath);
    } else {
        return DeleteDirectory(fullPath);
    }
}

int DeleteDirectory(const char *name) {
    struct stat st;
    DIR *dir = NULL;
    struct dirent *de;
    int fail = 0;
    if (stat(name, &st) < 0) return -1;
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
        if (DeleteDirectory(dn) < 0) {
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

char *GetDirectoryName(char *fullPath) {
    return SubstringBeforeLast(fullPath, '/');
}

char *GetExtension(char *fileName) {
    return SubstringBeforeLast(fileName, '.');
}

char *GetFileName(char *fullPath) {
    return SubstringAfterLast(fullPath, '/');
}

char *GetInvalidFileName(char *fileName, char substitute) {
    static char invalidFileNameChars[] = {'\"', '<', '>', '|', ':', '*', '?', '\\', '/'};
    char *tmp = fileName;
    char c;
    while ((c = *tmp)) {
        for (size_t i = 0; i < 10; i++) {
            if (c == invalidFileNameChars[i]) {
                *tmp = substitute;
            }
        }
        tmp++;
    }
    return fileName;
}

int IsDirectory(const char *fullPath) {
    struct stat s;
    return stat(fullPath, &s) != -1 && S_ISDIR(s.st_mode);
}


int CopyFile(const char *filename_in, const char *filename_out) {
    FILE *fin, *fout;
    size_t len;
    void *buf;
    fin = fopen(filename_in, "rbe");
    if (fin == NULL) {
        return -1;
    }
    fout = fopen(filename_out, "wbe");
    if (fout == NULL) {
        fclose(fin);
        return -1;
    }
    //We pick a value that is the largest multiple of 4096 that is still smaller than the large object heap threshold (85K).
    // The CopyTo/CopyToAsync buffer is short-lived and is likely to be collected at Gen0, and it offers a significant
    // improvement in Copy performance.
    size_t buf_size = 81920;
    buf = malloc(buf_size);
    while ((len = fread(buf, 1, buf_size, fin)) > 0) {
        fwrite(buf, 1, len, fout);
    }
    free(buf);
    fclose(fin);
    fclose(fout);
}


int CopyDirectory(const char *fullPath, const char *dst, size_t len) {
    struct stat st;
    if (stat(dst, &st) != 0)mkdir(dst, 0777);

    DIR *dir = NULL;
    struct dirent *de;

    dir = opendir(fullPath);
    if (dir == NULL)
        return -1;
    errno = 0;

    int dir_len = strlen(fullPath);
    char buf[PATH_MAX];
    if (len == dir_len)
        buf[0] = 0;
    else {
        for (size_t i = len + 1, j = 0; i < dir_len; i++) {
            buf[j++] = fullPath[i];
            printf("%c\n", fullPath[i]);
        }
        buf[dir_len - len - 1] = '/';
        buf[dir_len - len] = 0;
    }

    while ((de = readdir(dir)) != NULL) {
        char dn[PATH_MAX];
        char fn[PATH_MAX];
        memset(fn, 0, PATH_MAX);
        if (!strcmp(de->d_name, "..") || !strcmp(de->d_name, "."))
            continue;
        sprintf(dn, "%s/%s", fullPath, de->d_name);
        if (stat(dn, &st) < 0) return -1;
        if (S_ISDIR(st.st_mode)) {
            sprintf(fn, "%s/%s%s", dst, buf, de->d_name);
            CopyDirectory(dn, fn, len);
            printf("%s\n", fn);
        } else {


            sprintf(fn, "%s/%s", dst, de->d_name);
            printf("%s\n", fn);
            CopyFile(dn, fn);
        }
        errno = 0;
    }
    closedir(dir);
    return 0;
}


int MoveFile(const char *src, const char *fileName) {
    char t[strlen(src) + 1 + strlen(fileName)];
    strcpy(t, src);
    // 0 Success
    return rename(src, fileName);
}

char *SubstringAfterLast(char *s, char delimiter) {
    size_t j = strlen(s);
    size_t len = j;
    while (--j) {
        if (*(s + j) == delimiter) {
            break;
        }
    }
    if (j + 1 == len)return s;
    j++;
    char *t = s + j;
    memmove(s, t, strlen(t));
    *(s + len - j) = 0;
    return s;
}

char *SubstringBeforeLast(char *s, char delimiter) {
    size_t j = strlen(s);
    while (--j) {
        if (*(s + j) == delimiter) {
            *(s + j) = 0;
            break;
        }
    }
    return s;
}