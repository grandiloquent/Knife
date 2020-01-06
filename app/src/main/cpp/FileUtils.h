
#ifndef KNIFE_FILEUTILS_H
#define KNIFE_FILEUTILS_H

#include <string>

#ifdef __cplusplus
extern "C" {
#endif

int DeleteDirectory(const char *path);

int DeleteFileSystem(const char *path);

int DirectoryExists(const char *path);

#ifdef __cplusplus
}
#endif

#endif //KNIFE_FILEUTILS_H
