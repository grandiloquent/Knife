#ifndef FILE_HEADER__
#define FILE_HEADER__

#include <stddef.h>
#include <sys/stat.h>


#ifndef __error_t_defined
typedef int error_t;
#endif

struct files
{
    char** files_name;
    size_t capacity;
    size_t index;
};
int list_directory(char* path, struct files* list);

char *argz_next(char *argz, size_t argz_len, const char *entry);

error_t argz_append(char **pargz, size_t *pargz_len, const char *buf, size_t buf_len);

error_t argz_insert(char **pargz, size_t *pargz_len, char *before, const char *entry);

int create_directory(const char *p);

int move_files(const char *dfd, const char *dir_name);

int64_t calculate_dir_size(int dfd);

int64_t stat_size(struct stat *s);

int unlink_recursive(const char *name);
int
list_files_by_dir(const char *dirnam, char **pargz, size_t *pargz_len);

int rename_files(const char* dir,size_t pad_len);

#endif