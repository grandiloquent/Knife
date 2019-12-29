#ifndef LOG_HEADER__
#define LOG_HEADER__

#include <android/log.h>

#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, "TAG", __VA_ARGS__))
#define LOGD(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, "TAG", __VA_ARGS__))
#endif