#ifndef STR_HEADER__
#define STR_HEADER__
#include <assert.h>
#include <memory.h>
char *endswith(const char *s, const char *postfix);
char *endswith(const char *s, const char *postfix) {
    size_t sl, pl;
    assert(s);
    assert(postfix);
    sl = strlen(s);
    pl = strlen(postfix);
    if (pl == 0)
        return (char *) s + sl;
    if (sl < pl)
        return NULL;
    if (memcmp(s + sl - pl, postfix, pl) != 0)
        return NULL;
    return (char *) s + sl - pl;
}
#endif