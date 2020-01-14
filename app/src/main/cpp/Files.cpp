#include "Files.h"

#include <dirent.h>
#include <sys/stat.h>
#include <sstream>
#include <iostream>
#include <fstream>
#include <iterator>
#include <algorithm>

template<typename Cont, typename Pred>
Cont filter(const Cont &container, Pred predicate) {
    Cont result;
    std::copy_if(container.begin(), container.end(), std::back_inserter(result), predicate);
    return result;
}

inline bool EndsWith(std::string const &value, std::string const &ending) {
    if (ending.size() > value.size()) return false;
    return std::equal(ending.rbegin(), ending.rend(), value.rbegin());
}

std::string toString(const std::pair<bool, std::string> &data) {
    std::ostringstream str;
    str << data.first << ", " << data.second << "\n";
    return str.str();
}

bool IsDirectory(const char *path) {
    struct stat statBuf = {0};
    return stat(path, &statBuf) == 0 && S_ISDIR(statBuf.st_mode);
}

bool IsFile(const char *path) {
    struct stat statBuf = {0};
    return stat(path, &statBuf) == 0 && S_ISREG(statBuf.st_mode);
}

std::vector<std::pair<bool, std::string>> ListFiles(const char *path) {
    std::vector<std::pair<bool, std::string>> files;
    DIR *dir;
    // http://man7.org/linux/man-pages/man3/opendir.3.html
    dir = opendir(path);
    if (!dir)return files;

    bool suffix = false;
    if (path[strlen(path) - 1] == '/') {
        suffix = true;
    }
    struct stat s;
    struct dirent *de;

    // http://man7.org/linux/man-pages/man3/readdir.3.html
    while ((de = readdir(dir))) {
        if (strcmp(de->d_name, ".") == 0
            || strcmp(de->d_name, "..") == 0)
            continue;
        std::string p = path;
        if (!suffix) {
            p.append("/");
        }
        p.append(de->d_name);
        if (stat(p.c_str(), &s) != 0)continue;
        if (S_ISREG(s.st_mode)) {
            files.push_back(std::make_pair(true, p));
        } else if (S_ISDIR(s.st_mode)) {
            files.push_back(std::make_pair(false, p));
        }
    }

    closedir(dir);
    return files;
}

std::vector<std::string> ReadAllLines(const char *path) {
    std::ifstream ifs{path};
    if (ifs.fail()) {
        return {};
    }
    std::vector<std::string> buf;
    std::string line;
    while (std::getline(ifs, line)) {
        buf.push_back(line);
    }
    ifs.close();

    return buf;
}

std::vector<char> ReadAllBytes(const char *path) {
    std::ifstream file(path, std::ios::binary);

    if (file.fail()) {
        return {};
    }

    std::streampos begin, end;
    begin = file.tellg();
    file.seekg(0, std::ios::end);
    end = file.tellg();

    std::vector<char> result((size_t) (end - begin));
    file.seekg(0, std::ios::beg);
    file.read(&result[0], end - begin);
    file.close();

    return result;
}

std::string ReadAllText(const char *path) {
    const std::vector<char> &binaryContent = ReadAllBytes(path);
    return std::string(binaryContent.begin(), binaryContent.end());
}

bool WriteAllLines(const char *path, std::vector<std::string> &contents) {
    std::ofstream file{path};
    if (file.fail()) {
        return false;
    }
    for (const auto &c:contents) {
        file << c << std::endl;
    }
    file.close();
    return true;
}

/*
int main() {
	std::string path="C:/Users/psycho/Desktop/apache/epub";
	auto files=ListFiles(path.c_str());
	auto textFiles=filter(files,[](auto &file) {
		return EndsWith(file.second,".txt");
	});
	std::sort(textFiles.begin(),textFiles.end(),[](auto &a,auto &b) {
		return a.second<b.second;
	});
	std::ofstream file{(path+".txt").c_str()};
	if(file.fail()) {
		return 1;
	}
	std::for_each(textFiles.begin(),textFiles.end(),[&](auto &f) {
		file<<ReadAllText(f.second.c_str())<<"\n\n\n===∑∑∑∑∑∑∑∑∑===\n\n\n";
	});
	file.close();
	std::transform(textFiles.begin(),textFiles.end(),std::ostream_iterator<std::string>(std::cout," "),toString);
	return 0;
}
*/

std::ifstream::pos_type GetFileSize(const char *filename) {
    std::ifstream in(filename, std::ifstream::ate | std::ifstream::binary);
    return in.tellg();
}