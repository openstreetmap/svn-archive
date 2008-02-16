#ifndef DIR_UTILS_H
#define DIR_UTILS_H

#include <sys/types.h>

#ifdef __cplusplus
  extern "C" {
#endif


/* Build parent directories for the specified file name
 * Note: the part following the trailing / is ignored
 * e.g. mkdirp("/a/b/foo.png") == shell mkdir -p /a/b
 */
int mkdirp(const char *path);

/* File path hashing. Used by both mod_tile and render daemon
 * The two must both agree on the file layout for meta-tiling
 * to work
 */
const char *xyz_to_path(char *path, size_t len, int x, int y, int z);

int check_xyz(int x, int y, int z);
int path_to_xyz(const char *path, int *px, int *py, int *pz);

/* New meta-tile storage functions */

/* Returns the path to the meta-tile and the offset within the meta-tile */
int xyz_to_meta(char *path, size_t len, int x, int y, int z);


#ifdef __cplusplus
  }
#endif


#endif
