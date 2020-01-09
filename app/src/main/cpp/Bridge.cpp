#include <fstream>
#include "Bridge.h"
#include "Files.h"
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
int CombineFiles(const char *path) {
    std::string dir = path;
    auto files = ListFiles(dir.c_str());
    auto textFiles = filter(files, [](auto &file) {
        return EndsWith(file.second, ".txt");
    });
    std::sort(textFiles.begin(), textFiles.end(), [](auto &a, auto &b) {
        return a.second < b.second;
    });
    std::ofstream file{(dir + ".txt").c_str()};
    if (file.fail()) {
        return 1;
    }
    std::for_each(textFiles.begin(), textFiles.end(), [&](auto &f) {
        file << ReadAllText(f.second.c_str()) << "\n\n\n=========\n\n\n";
    });
    file.close();
    return 0;
}
