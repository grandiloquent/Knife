
//

#ifndef KNIFE_HTTPSERVER_H
#define KNIFE_HTTPSERVER_H

#include "httplib.h"

using namespace httplib;

class HttpServer {
public:


    bool StartServer(const char *host, int port, const char *directory);

    bool StopServer();

private:
    std::string GetFileContents(const char *path);

    Server _server;
    //bool _isStopped;
    const char *_host;
    const char *_directory;
    int _port;
};

HttpServer &GetEmbedServer();


#endif //KNIFE_HTTPSERVER_H
