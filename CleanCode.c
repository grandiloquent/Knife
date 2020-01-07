#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>
#include <ctype.h>

int IsNullOrWhiteSpace(const char *s) {
    if (!s)return 1;

    const char *t = strdup(s);
    while (*t) {
        if (!isspace(*t))return 0;
        t++;
    }

    return 1;
}

char *ReadAllText(const char *path) {
    FILE *in = fopen(path, "rb");

    if (in) {
        int c;
        size_t buf_size = 80920;
        size_t i = 0;
        char *buf = malloc(buf_size);
        if (!buf) {
            goto err;
        }
        while ((c = fgetc(in)) != EOF) {
            if (i + 1 == buf_size) {
                buf_size <<= 1;
                buf = realloc(buf, buf_size);
                if (!buf) {
                    goto err;
                }
            }
            buf[i++] = (char) c;
        }
        fclose(in);
        return buf;
    }
    err:
    fclose(in);
    return NULL;
}

char **ReadAllLines(const char *path, size_t *count) {
    FILE *in = fopen(path, "rb");

    if (in) {

        size_t lines_buf_size = 1024;
        char **lines_buf = malloc(lines_buf_size * sizeof(*lines_buf));


        if (!lines_buf) {

            return NULL;
        }

        size_t j = 0;

        int c;
        size_t buf_size = 80920;
        size_t i = 0;
        char *buf = malloc(buf_size);

        if (!buf) {
            return NULL;
        }
        memset(buf, 0, buf_size);
        while ((c = fgetc(in)) != EOF) {
            if (c == '\n') {
                if (j + 1 == lines_buf_size) {

                    lines_buf_size <<= 1;
                    lines_buf = realloc(lines_buf, lines_buf_size * sizeof(*lines_buf));

                    if (!lines_buf) {

                        return NULL;
                    }
                }

                //printf("%d %d %d\n",j,lines_buf_size,sizeof(lines_buf)/sizeof(char *));

                char *tmp = malloc(strlen(buf) + 1);
                memcpy(tmp, buf, strlen(buf) + 1);
                lines_buf[j++] = tmp;
                memset(buf, 0, buf_size);
                i = 0;
                continue;
            }

            if (i + 1 == buf_size) {
                buf_size <<= 1;
                buf = realloc(buf, buf_size);
                if (!buf) {

                    return NULL;
                }
            }
            buf[i++] = (char) c;
        }

        if (strlen(buf)) {
            lines_buf[j++] = strdup(buf);
        }
        *count = j;
        free(buf);
        fclose(in);
        return lines_buf;
    }
    return NULL;
}

char *EndsWith(const char *s, const char *postfix) {
    size_t sl, pl;
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

int ListFiles(const char *path, int (f)(int, const char *)) {
    DIR *dir = opendir(path);
    if (dir == NULL) {
        return -1;
    }
    struct dirent *de;
    struct stat s;
    char buf[PATH_MAX];

    while ((de = readdir(dir))) {
        if (strcmp(de->d_name, ".") == 0 ||
            strcmp(de->d_name, "..") == 0) {
            continue;
        }

        memset(buf, 0, PATH_MAX);

        strcpy(buf, path);
        strcat(buf, "/");
        strcat(buf, de->d_name);

        if (stat(buf, &s) != 0) {
            return -1;
        }
        if (S_ISDIR(s.st_mode)) {
            f(1, buf);
            ListFiles(buf, f);
        } else {
            f(0, buf);
        }
    }


    closedir(dir);

    return 0;
}

int RemoveEmptyLines(const char *path) {
    size_t count = 0;
    char **lines = ReadAllLines(path, &count);

    FILE *out = fopen(path, "w");

    for (size_t i = 0; i < count; i++) {
        if (!IsNullOrWhiteSpace(lines[i])) {
            fputs(lines[i], out);
            fputc('\n', out);
        }
    }
    fclose(out);
    return 0;
}

int f(int t, const char *path) {
    if (!t && (EndsWith(path, ".java") || EndsWith(path, ".c")
               || EndsWith(path, ".h")
               || EndsWith(path, ".cpp"))) {
        RemoveEmptyLines(path);
    }
}

int main() {
    /*size_t count=0;
    char **r=	ReadAllLines("1.txt",&count);
    for(size_t i=0; i<count; i++) {
        printf("%s %d\n",r[i],i);
    }*/
    ListFiles("./app/src/main", f);
    //int r=RemoveEmptyLines("app/src/main");
    //printf("%d",r);
}