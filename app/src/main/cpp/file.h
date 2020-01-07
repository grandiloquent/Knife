#ifndef FILE_HEADER__#define FILE_HEADER__#include <stddef.h>#include <sys/stat.h>#include <errno.h>#include <dirent.h>#include <unistd.h>#ifndef __error_t_definedtypedef int error_t;#endifstatic inline int64_t stat_size(struct stat *s) {    return s->st_blocks * 512;}struct files {    char **files_name;    size_t capacity;    size_t index;};int list_directory(char *path, struct files *list);char *argz_next(char *argz, size_t argz_len, const char *entry);error_t argz_append(char **pargz, size_t *pargz_len, const char *buf, size_t buf_len);error_t argz_insert(char **pargz, size_t *pargz_len, char *before, const char *entry);int create_directory(const char *p);int move_files(const char *dfd, const char *dir_name);int64_t calculate_dir_size(int dfd);intlist_files_by_dir(const char *dirnam, char **pargz, size_t *pargz_len);int rename_files(const char *dir, size_t pad_len);static inline char *Join(char *path, const char *fileName) {    strcat(path, "/");    strcat(path, fileName);    return path;}char *GetDirectoryName(char *fullPath);char *GetExtension(char *fileName);char *GetFileName(char *fullPath);char *GetInvalidFileName(char *fileName, char substitute);//int Delete(const char *fullPath);//int IsDirectory(const char *fullPath);int MoveFile(const char *src, const char *fileName);char *SubstringAfterLast(char *s, char delimiter);char *SubstringBeforeLast(char *s, char delimiter);int CopyFile(const char *filename_in, const char *filename_out);int CopyDirectory(const char *fullPath, const char *dst, size_t len);char *ReadAllText(const char *path);char *EndsWith(const char *s, const char *postfix);///////////////////////////#endif
