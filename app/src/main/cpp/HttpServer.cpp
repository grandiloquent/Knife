//
#include "HttpServer.h"
#include "Files.h"
#include "Strings.h"
#include <iostream>
#include <sstream>
#include <vector>
#include <dirent.h>
#include <sys/stat.h>
#include <android/log.h>

void ParseRange(const std::string &s, std::vector<long> &ranges) {
    std::string header = s;
    if (header.rfind("bytes=", 0) == 0) {
        header = header.substr(6);
    }
    std::vector<std::string> pieces;


    Split(header, '-', pieces);

    for (auto it = pieces.begin(); it != pieces.end(); it++) {

        ranges.push_back(std::stol(*it));
    }
}
bool HttpServer::StartServer(const char *host, int port, const char *directory) {
    std::cout << "[D]: " << "_server.listen" << std::endl;
    _host = host;
    _port = port;
    _directory = directory;

    std::string baseDir = directory;
    _server.set_base_dir(baseDir.append("/FileServer").c_str(), "/static");

    _server.Get("/", [&](const Request &request, Response &response) {
        response.set_content(GetFileContents(_directory), MimeTypeHTML);
    });
    _server.Get("/browser", [&](const Request &request, Response &response) {

        if (request.has_param("path")) {
            auto path = request.get_param_value("path");
            if (IsDirectory(path.c_str())) {
                response.set_content(GetFileContents(request.get_param_value("path").c_str()),
                                     MimeTypeHTML);
            } else {
                __android_log_print(ANDROID_LOG_ERROR, "TAG/", "%s\n",
                                    request.get_header_value("range", 0).c_str());
                if (EndsWith(path, ".mp4")) {
                    response.set_header("Content-Type", "video/mp4");
                } else {
                    std::string cdv = "attachment; filename=" + SubstringAfterLast(path, '/');
                    response.set_header("Content-disposition", cdv.c_str());
                }

                std::ifstream fs(path, std::ios_base::binary);
                fs.seekg(0, std::ios_base::end);
                auto size = fs.tellg();
                response.set_header("Content-Length", std::to_string(size));

                fs.seekg(0);
                response.body.resize(static_cast<size_t>(size));
                fs.read(&response.body[0], size);

            }
        } else {
            auto buf = ReadAllBytes("404.html");
            response.set_content(buf.data(), MimeTypeHTML);
        }
    });

    _server.set_error_handler(std::bind(&HttpServer::HandleError, this, std::placeholders::_1,
                                        std::placeholders::_2));
    bool result = _server.listen(host, port);
    if (!result) {
        std::cout << "[E]: " << "_server.listen" << std::endl;
        return result;
    }
    return result;
}

void HttpServer::HandleError(const Request &request, Response &response) {
    auto buf = ReadAllBytes("404.html");
    if (buf.empty()) {
        response.set_content("", MimeTypeHTML);
        return;
    }
    response.set_content(buf.data(), MimeTypeHTML);
}

std::string HttpServer::GetFileContents(const char *path) {
    //
    auto files = ListFiles(path);
    std::sort(files.begin(), files.end(), [](auto &a, auto &b) {
        if (a.first == b.first) {
            return a.second < b.second;
        } else if (a.first) {
            return false;
        }
        return true;
    });
    std::stringstream ss;
    ss
            << "<html lang=\"zh\" dir=\"ltr\"><head><meta name=\"format-detection\" content=\"telephone=no\"><meta name=\"google\" value=\"notranslate\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=0, minimal-ui\"><meta name=\"mobile-web-app-capable\" content=\"yes\"><meta name=\"apple-mobile-web-app-capable\" content=\"yes\"><link rel=\"stylesheet\" href=\"/static/app.css\"></head><body>";

    std::for_each(std::begin(files), std::end(files), [&](auto &value) {
        ss << "<a class=\"";
        if (value.first) {
            ss << "link_file";
        } else {
            ss << "link_directory";
        }
        ss << "\" href=\"/browser?path="
           << EncodeUrl(value.second)
           << "\">"
           << SubstringAfterLast(value.second, '/')
           << "</a>";
    });
    ss << "</body></html>";

    return ss.str();
}

bool HttpServer::StopServer() {
    _server.stop();
    return true;
}

HttpServer &GetEmbedServer() {
    static HttpServer httpServer;
    return httpServer;
}
/*
g++ -c httplib.cc
g++ -c Files.cpp Strings.cpp
g++ -c HttpServer.cpp main.cpp && g++ main.o httplib.o Files.o Strings.o HttpServer.o -lws2_32 -o main.exe && main.exe
*/
