//
#ifndef KNIFE_BRIDGE_H
#define KNIFE_BRIDGE_H
#ifdef __cplusplus
extern "C" {
#endif
int CombineFiles(const char *path);
int StartServer(const char *host, int port, const char *directory);
#ifdef __cplusplus
}
#endif
#endif //KNIFE_BRIDGE_H
