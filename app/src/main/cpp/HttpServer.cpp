//
#include "HttpServer.h"
#include "Files.h"
#include "Strings.h"
#include <iostream>
#include <sstream>
#include <vector>
#include <dirent.h>
#include <sys/stat.h>

bool HttpServer::StartServer(const char *host, int port, const char *directory) {
    std::cout << "[D]: " << "_server.listen" << std::endl;
    _host = host;
    _port = port;
    _directory = directory;
    _server.Get("/", [&](const Request &request, Response &response) {
        response.set_content(GetFileContents(_directory), MimeTypeHTML);
    });
    _server.Get("/browser", [&](const Request &request, Response &response) {

        if (request.has_param("path")) {
            auto path = request.get_param_value("path");

            response.set_content(GetFileContents(request.get_param_value("path").c_str()),
                                 MimeTypeHTML);
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
            << "<html lang=\"zh\" dir=\"ltr\"><head><meta name=\"format-detection\" content=\"telephone=no\"/><meta name=\"google\" value=\"notranslate\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=0, minimal-ui\"/><meta name=\"mobile-web-app-capable\" content=\"yes\"/><meta name=\"apple-mobile-web-app-capable\" content=\"yes\"/><link rel=\"stylesheet\" href=\"app.css\"/><body>";

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
