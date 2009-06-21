#include <stdio.h>
#include <stdlib.h>
int main(int argc, char **argv) {
    unsigned char buf[4096];
    int count, bufi;
    int shift = atoi(argv[1]);
    bufi = count = fread(&buf, 1, sizeof(buf) - 1, stdin);
    for (; bufi >= 1;  bufi--) {
        buf[bufi]  |= buf[bufi-1] << (8 - shift);
        buf[bufi-1] = buf[bufi-1] >> shift;
    }
    fwrite(&buf, 1, count+1, stdout);

    return 0;
}

