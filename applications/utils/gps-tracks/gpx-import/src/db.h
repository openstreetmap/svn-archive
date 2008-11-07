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

#ifndef GPX_IMPORT_DB_H
#define GPX_IMPORT_DB_H

#include <stdbool.h>
#include <stdint.h>

#include "gpx.h"

typedef struct {
  GPX *gpx;
  int64_t gpx_id;
  char *title;
  char *description;
  char *tags;
  char *email;
  
  /* If this is non-NULL then an error has occurred */
  char *error;
} DBJob;

/* All these functions are in the database backend files. */
extern bool db_connect(void);
extern DBJob *db_find_work(int minage);
extern bool db_insert_gpx(DBJob *job);
extern int64_t db_find_invisible(void);
extern bool db_destroy_trace(int64_t jobnr);
extern void db_disconnect(void);

/* These are implemented in db.c rather than in any of the backends.
 */
extern void db_free_job(DBJob *job);
extern void db_error(DBJob *job, const char *fmt, ...);

#endif /* GPX_IMPORT_DB_H */
