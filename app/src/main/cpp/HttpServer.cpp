//
#include "HttpServer.h"
#include <iostream>
#include <sstream>
#include <vector>
#include <dirent.h>
#include <sys/stat.h>
bool HttpServer::StartServer(const char *host, int port, const char *directory) {
    std::cout<<"[D]: "<<"_server.listen"<<std::endl;
    _host = host;
    _port = port;
    _directory = directory;
    _server.Get("/", [&](const Request &request, Response &response) {
        response.set_content(GetFileContents(_directory), "text/html");
    });
    bool result = _server.listen(host, port);
    if (!result) {
        std::cout<<"[E]: "<<"_server.listen"<<std::endl;
        return result;
    } else {
        std::cout<<"[D]: "<<"_server.listen"
                 <<"host = "<<host <<" "
                 <<"port = "<<port <<" "<<std::endl;
    }
    return result;
}
std::string HttpServer::GetFileContents(const char *path) {
    DIR *dir;
    dir=opendir(path);
    if(dir==NULL) {
        return nullptr;
    }
    std::vector<std::pair<bool,std::string>> files;
    struct dirent *de;
    struct stat st;
    while((de=readdir(dir))) {
        if(strcmp(de->d_name,".")==0
           ||strcmp(de->d_name,"..")==0)continue;
        std::string fullPath=path;
        fullPath.append("/").append(de->d_name);
        if(stat(fullPath.c_str(),&st)!=0) {
            continue;
        }
        if( S_ISDIR(st.st_mode)) {
            files.push_back(std::make_pair(true,fullPath));
        } else if(S_ISREG(st.st_mode)) {
            files.push_back(std::make_pair(false,fullPath));
        }
    }
    std::sort(files.begin(),files.end(),[](auto &a,auto &b) {
        if(a.first==b.first) {
            return a.second<b.second;
        } else if(a.first) {
            return true;
        }
        return false;
    });
    std::stringstream ss;
    std::for_each(std::begin(files),std::end(files),[&](auto & value) {
        ss<<"<a>"
          <<value.second
          <<"</a>";
    });
    closedir(dir);
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
g++ -c HttpServer.cpp && g++ httplib.o HttpServer.o -lws2_32 -o main.exe && main.exe
*/
//int main() {
//    GetEmbedServer().StartServer("localhost",8081,"C:/");
//    return 0;
//}
