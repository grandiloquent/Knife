

#include "StringUtils.h"


std::string SubstringAfter(std::string &s, char delimiter) {
    int index = s.find_first_of(delimiter);
    if (index == -1)return s;
    return s.substr(static_cast<unsigned int>(index));
}

std::string SubstringAfter(std::string &s, std::string &delimiter) {
    int index = s.find_first_of(delimiter);
    if (index == -1)return s;
    return s.substr(static_cast<unsigned int>(index) + delimiter.length());
}

std::string SubstringAfterLast(std::string &s, char delimiter) {
    int index = s.find_last_of(delimiter);
    if (index == -1)return s;
    return s.substr(static_cast<unsigned int>(index));
}

std::string SubstringAfterLast(std::string &s, std::string &delimiter) {
    int index = s.find_last_of(delimiter);
    if (index == -1)return s;
    return s.substr(static_cast<unsigned int>(index) + delimiter.length());
}

std::string Repeat(std::string &s, int count) {
    if (count <= 0)return s;
    const std::string t = s;
    for (int i = 0; i < count; ++i) {
        s.append(t);
    }
    return s;
}

std::string RemovePrefix(std::string &s, std::vector<std::string> &prefixs) {
    auto prefix = std::find_if(std::begin(prefixs), std::end(prefixs),
                               [](std::string &x) -> bool {
                                   return true;
                               });
    if (prefix == std::end(prefixs)) {
        return s;
    } else {
        s.substr(prefix[0].length());
    }
}