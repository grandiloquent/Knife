
//

#ifndef KNIFE_HTTPSERVER_H
#define KNIFE_HTTPSERVER_H

#include "httplib.h"

using namespace httplib;

class HttpServer {
public:
    HttpServer() : MimeTypeHTML("text/html; charset=utf-8") {}

    bool StartServer(const char *host, int port, const char *directory);

    bool StopServer();

private:
    void HandleError(const Request &request, Response &response);

    std::string GetFileContents(const char *path);

    Server _server;
    bool _isStopped;
    const char *_host;
    const char *_directory;
    const char *MimeTypeHTML;

    int _port;
};

HttpServer &GetEmbedServer();


#endif //KNIFE_HTTPSERVER_H
