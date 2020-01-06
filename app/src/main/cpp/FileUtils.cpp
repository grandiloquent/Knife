

#include <vector>
#include "FileUtils.h"

extern "C"
{
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
}

/*
 * http://man7.org/linux/man-pages/man2/unlink.2.html
 * http://man7.org/linux/man-pages/man3/remove.3.html
 * http://man7.org/linux/man-pages/man2/stat.2.html
 */
int DeleteFileSystem(const char *path) {

    if (DirectoryExists(path)) {
        return DeleteDirectory(path);
    } else {
        return unlink(path);
    }


}

int DeleteDirectory(const char *path) {

    // 0 Success
    //
    DIR *dir = opendir(path);
    if (dir == nullptr) {
        return -1;
    }
    struct dirent *de = {};
    while ((de = readdir(dir)) != nullptr) {
        if (strcmp(de->d_name, ".") == 0 ||
            strcmp(de->d_name, "..") == 0)
            continue;
        std::string fileName = path;
        fileName += "/";
        fileName += de->d_name;
        if (DirectoryExists(fileName.c_str())) {
            DeleteDirectory(fileName.c_str());
        } else {
            unlink(fileName.c_str());
        }

    }
    closedir(dir);
    return rmdir(path);
}

std::vector<std::string> ListFiles(const char *path) {
    std::vector<std::string> paths = {};


    return paths;
}

int DirectoryExists(const char *path) {
    struct stat statBuf = {0};
    return lstat(path, &statBuf) == 0 && S_ISDIR(statBuf.st_mode);
}