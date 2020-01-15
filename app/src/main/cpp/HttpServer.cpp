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

#define LOG_TAG "TAG/"
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

//void ParseRange(const std::string &s, std::vector<long> &ranges) {
//    std::string header = s;
//    if (header.rfind("bytes=", 0) == 0) {
//        header = header.substr(6);
//    }
//    std::vector<std::string> pieces;
//
//
//    Split(header, '-', pieces);
//
//    for (auto it = pieces.begin(); it != pieces.end(); it++) {
//
//        ranges.push_back(std::stol(*it));
//    }
//}

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
                return true;
            } else if (IsFile(path.c_str())) {
                if (EndsWith(path, ".mp4")) {
                    response.set_header("Content-Type", "video/mp4");
                } else {
                    std::string cdv = "attachment; filename=" + SubstringAfterLast(path, '/');
                    response.set_header("Content-disposition", cdv.c_str());
                }
                HandleDownload(path, request, response);
                return true;

            }
        }

        this->HandleError(request, response);
        return true;
    });
    std::function<void(const Request &, Response &)> uploadHandler = std::bind(
            &HttpServer::HandleUpload, this, std::placeholders::_1,
            std::placeholders::_2);
    _server.Post("/upload", uploadHandler);

    _server.set_error_handler(std::bind(&HttpServer::HandleError, this, std::placeholders::_1,
                                        std::placeholders::_2));
    bool result = _server.listen(host, port);
    if (!result) {
        std::cout << "[E]: " << "_server.listen" << std::endl;
        return result;
    }
    return result;
}

void HttpServer::HandleUpload(const Request &request, Response &response) {
    if (request.files.empty()) {
        HandleError(request, response);
        return;
    }
    for (auto itr = request.files.begin(); itr != request.files.end(); ++itr) {
        LOGE("key = %s file.name = %s file.filename = %s\n", itr->first.c_str(),
             itr->second.name.c_str(),
             itr->second.filename.c_str());
        {
            std::string target = this->_directory;
            target.append("/FileServer/").append(itr->second.filename);
            if (IsFile(target.c_str())) {
                continue;
            }
            LOGE("%s\n", target.c_str());
            std::ofstream o(target);
            o << itr->second.content;
        }

    }


    response.set_content("Ok", MimeTypeHTML);
}

void HttpServer::HandleError(const Request &request, Response &response) {

    response.status = 404;
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
            << "<html lang=\"zh\" dir=\"ltr\"><head><meta name=\"format-detection\" content=\"telephone=no\"/><meta name=\"google\" value=\"notranslate\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=0, minimal-ui\"/><meta name=\"mobile-web-app-capable\" content=\"yes\"/><meta name=\"apple-mobile-web-app-capable\" content=\"yes\"/><link rel=\"stylesheet\" href=\"static/app.css\"/><body><header><div class=\"toolbar\"><div class=\"toolbar-button\"><div class=\"toolbar-ico\"><svg width=\"24px\" height=\"24px\" viewBox=\"0 0 24 24\" fill=\"#000000\"><g><rect fill=\"none\" width=\"24\" height=\"24\"></rect><path d=\"M3,18h18v-2H3V18z M3,13h18v-2H3V13z M3,6v2h18V6H3z\"></path></g></svg></div></div><div class=\"toolbar-title\">我的网络硬盘</div><div class=\"toolbar-end\"><div class=\"toolbar-button\" id=\"uploadButton\"><div class=\"toolbar-ico\"><svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\"><path d=\"M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z\"/></svg></div></div><div class=\"toolbar-button\"><div class=\"toolbar-ico\"><svg x=\"0px\" y=\"0px\" width=\"24px\" height=\"24px\" viewBox=\"0 0 24 24\" fill=\"#000000\"><path d=\"M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z\"></path><path d=\"M0 0h24v24H0z\" fill=\"none\"></path></svg></div></div><div class=\"toolbar-button\"><div class=\"toolbar-ico\"><svg x=\"0px\" y=\"0px\" width=\"24px\" height=\"24px\" viewBox=\"0 0 24 24\" focusable=\"false\" fill=\"#000000\"><path d=\"M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z\"></path></svg></div></div></div></div></header>";

    std::for_each(std::begin(files), std::end(files), [&](auto &value) {
        ss << "<a class=\"";
        if (value.first) {
            ss << "link_file";
        } else {
            ss << "link_directory";
        }
        ss << "\" href=\"/browser?path="
           << EncodeUrl(value.second)
           << "\"><div class=\"link_cover\"></div>"
           << "<div class=\"link_name\">"
           << SubstringAfterLast(value.second, '/')
           << "</div>"
           << "<div class=\"link_size\"></div>"
           << "</a>";
    });
    ss << "<script src=\"static/app.js\"></script></body></html>";

    return ss.str();
}

bool HttpServer::StopServer() {
    _server.stop();
    return true;
}

void HttpServer::HandleDownload(const std::string &path,
                                const Request &request, Response
                                &response) {
    std::shared_ptr<std::ifstream> fs = std::make_shared<std::ifstream>();
    fs->open(path, std::ios_base::binary);
    fs->seekg(0, std::ios_base::end);
    auto end = fs->tellg();
    fs->seekg(0);

    LOGE("fileSize(end) = %d\n", static_cast<size_t>(end));

    response.set_content_provider(static_cast<size_t>(end),
                                  [fs, this, &request, &response](
                                          uint64_t offset,
                                          uint64_t
                                          length,
                                          DataSink &sink
                                  ) {
                                      if (fs->fail()) {
                                          this->HandleError(request, response);
                                          return;
                                      }

                                      fs->seekg(offset, std::ios_base::beg
                                      );

                                      size_t bufSize = 81920;
                                      char buffer[bufSize];

                                      fs->read(buffer, bufSize);

                                      sink.write(buffer, static_cast<size_t>(fs->gcount()));
                                  });
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
