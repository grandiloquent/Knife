
//

#ifndef KNIFE_FILES_H
#define KNIFE_FILES_H

#include <vector>
#include <string>

bool IsDirectory(const char *path);
std::vector<std::pair<bool,std::string>> ListFiles(const char *path);
std::vector<std::string> ReadAllLines(const char *path);
std::vector<char> ReadAllBytes(const char *path);
std::string ReadAllText(const char * path);
bool WriteAllLines(const char * path,std::vector<std::string> &contents);
#endif
