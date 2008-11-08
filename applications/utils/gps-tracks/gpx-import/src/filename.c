/* gpx-import/src/filename.c
 *
 * GPX importer, filename generator
 *
 * Copyright Daniel Silverstone <dsilvers@digital-scurf.org>
 *
 * Written for the OpenStreetMap project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; version 2 of the License
 * only.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */

#include <stdio.h>
#include <limits.h>
#include <stdlib.h>
#include <inttypes.h>

#include "filename.h"

char *
make_filename(const char *base, int64_t nr, const char *suffix)
{
  static char namebuffer[PATH_MAX];
  char *real_base = getenv(base);
  
  if (real_base == NULL) {
    WARN("Unable to find base from: %s", base);
    real_base = ".";
  }
  
  snprintf(namebuffer, PATH_MAX, "%s/%"PRId64"%s", real_base, nr, suffix);
  
  return namebuffer;
}

