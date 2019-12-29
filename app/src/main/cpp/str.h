#ifndef STR_HEADER__
#define STR_HEADER__
#include <assert.h>

#include <string.h>
#include <ctype.h>


char *endswith(const char *s, const char *postfix);
char* between(const char* buf, const char* start, const char* end);
char* trim(char* buf);
int ends_with(const char* buf, const char* suffix);
int indexof(const char* s1, const char* s2);
///////////////////////////
char* between(const char* buf, const char* start, const char* end)
{
    int start_idx = indexof(buf, start);
    if (start_idx == -1)
        return NULL;
    int end_idx = indexof(buf, end);
    if (end_idx == -1)
        return NULL;
    end_idx = end_idx - strlen(end);
    if (end_idx <= start_idx)
        return NULL;
    char* result = malloc(end_idx - start_idx + 1);
    char* tmp = result;
    for (size_t i = start_idx; i < end_idx; i++)
    {
        *tmp++ = buf[i];
    }
    *tmp = 0;
    return result;
}
int ends_with(const char* buf, const char* suffix)
{
    size_t len = strlen(buf);
    size_t suflen = strlen(suffix);
    if (len < suflen || memcmp(buf + (len - suflen), suffix, suflen))
        return 0;
    return 1;
}
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
 int indexof(const char* s1, const char* s2)
{
    if (s1 == NULL || s2 == NULL || *s1 == 0 || *s2 == 0)
    {
        return -1;
    }
    const char* p1 = s1;
    size_t len = strlen(p1);
    for (size_t i = 0; i < len; i++)
    {
        if (p1[i] == *s2)
        {
            const char* p2 = s2;
            while (*p2 && *p2 == p1[i])
            {
                i++;
                p2++;
            }
            if (*p2 == 0)
                return i;
        }
    }
    return -1;
}
char* trim(char* buf)
{
    char* tmp = buf;
    size_t i = 0;
    while (*tmp)
    {
        if (isspace(*tmp))
        {
            i++;
        }
        else
        {
            break;
        }
        tmp++;
    }
    if (i > 0)
        memmove(buf, tmp, i);
    size_t len = strlen(buf);
    while (--len)
    {
        if (isspace(buf[len]))
            buf[len] = 0;
        else
            break;
    }
    return buf;
}
#endif