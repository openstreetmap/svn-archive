#ifndef STORE_H
#define STORE_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdlib.h>
#include "render_config.h"
int tile_read(int x, int y, int z, char *buf, int sz);

#define META_MAGIC "META"
//static const char meta_magic[4] = { 'M', 'E', 'T', 'A' };

struct entry {
    int offset;
    int size;
};

struct meta_layout {
    char magic[4];
    int count; // METATILE ^ 2
    int x, y, z; // lowest x,y of this metatile, plus z
    struct entry index[]; // count entries
    // Followed by the tile data
    // The index offsets are measured from the start of the file
};


int read_from_file(int x, int y, int z, char *buf, size_t sz);

#ifdef METATILE
int read_from_meta(int x, int y, int z, char *buf, size_t sz);
void process_meta(int x, int y, int z);
void process_pack(const char *name);
void process_unpack(const char *name);
#endif

#ifdef __cplusplus
}
#endif
#endif
