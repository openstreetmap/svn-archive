/* gpx-import/src/db.h
 *
 * Database interface layer
 *
 * Copyright 2008 Daniel Silverstone <dsilvers@digital-scurf.org>
 *
 * Written for OpenStreetMap
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

#include <stdlib.h>
#include <stdarg.h>
#include <stdio.h>

#include "db.h"

void
db_free_job(DBJob *job)
{
  if (job == NULL)
    return;
  
  if (job->gpx != NULL) {
    gpx_free(job->gpx);
  }
  
  free(job->title);
  free(job->description);
  free(job->tags);
  free(job->email);
  free(job->error);
  
  free(job);
}

void
db_error(DBJob *job, const char *fmt, ...)
{
  va_list va;
  int sz;
  
  va_start(va, fmt);
  sz = vsnprintf(NULL, 0, fmt, va);
  job->error = malloc(sz + 1);
  vsnprintf(job->error, sz + 1, fmt, va);
  va_end(va);
}

