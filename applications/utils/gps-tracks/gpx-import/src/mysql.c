/* gpx-import/src/mysql.c
 *
 * GPS point insertion into MySQL database
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

#include <stdlib.h>
#include <mysql.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <inttypes.h>

#include "filename.h"
#include "db.h"
#include "quadtile.h"

#define STMT_BUFLEN (1024 * 256)

static MYSQL *handle;
static char statement_buffer[STMT_BUFLEN];
static char escape_buffer[STMT_BUFLEN];

#define STMT(V...)                                                         \
  do {                                                                     \
    int stmt_len = snprintf(statement_buffer, STMT_BUFLEN, V);             \
    if (mysql_real_query(handle, statement_buffer, stmt_len) != 0) {       \
      ERROR("Failure executing MySQL statement: %s", mysql_error(handle)); \
      return false;                                                        \
    }                                                                      \
  } while (0)

#define BLANKOR(S) strdup(((S) ? (S) : ("")))

bool
db_destroy_trace(int64_t jobnr)
{
  INFO("Destroying job %"PRId64"", jobnr);
  STMT("DELETE FROM gpx_file_tags WHERE gpx_id=%"PRId64"", jobnr);
  STMT("DELETE FROM gps_points WHERE gpx_id=%"PRId64"", jobnr);
  STMT("DELETE FROM gpx_files WHERE id=%"PRId64"", jobnr);
  unlink(make_filename("GPX_PATH_TRACES", jobnr, ".gpx"));
  unlink(make_filename("GPX_PATH_IMAGES", jobnr, "_icon.gif"));
  unlink(make_filename("GPX_PATH_IMAGES", jobnr, ".gif"));
  return true;
}


bool
db_insert_gpx(DBJob *job)
{
  MYSQL_RES *res;
  MYSQL_ROW row;
  bool do_delete = false;
  GPXTrackPoint *pt;
  int64_t gpxnr = job->gpx_id;
  GPX *gpx = job->gpx;
  
  STMT("SELECT COUNT(*) FROM gps_points WHERE gpx_id=%"PRId64"", gpxnr);
  res = mysql_store_result(handle);
  row = mysql_fetch_row(res);
  if (atoi(row[0]) != 0) {
    do_delete = true;
  }
  mysql_free_result(res);
  
  if (do_delete == true) {
    WARN("Old rows detected, deleting");
    STMT("DELETE FROM gps_points WHERE gpx_id=%"PRId64"", gpxnr);
  }
  
  INFO("Inserting %d points", gpx->goodpoints);
  
  /* Iterate the points, inserting them into the DB */
  for (pt = gpx->points; pt != NULL; pt = pt->next) {
    mysql_real_escape_string(handle, escape_buffer, pt->timestamp, strlen(pt->timestamp));
    STMT("INSERT INTO gps_points (gpx_id, trackid, latitude, longitude, timestamp, altitude, tile) " \
         "VALUES (%"PRId64", %d, %"PRId64", %"PRId64", '%s', %f, %u)",
         gpxnr, pt->segment, pt->latitude / 100, pt->longitude / 100, escape_buffer, pt->elevation,
         quadtile_for_coords(pt->latitude, pt->longitude));
  }

  /* Last up, update the GPX with our lat/long/numpoints etc */
  STMT("UPDATE gpx_files SET inserted=1, size=%d, latitude=%g, longitude=%g WHERE id=%"PRId64"\n",
       gpx->goodpoints, (double)gpx->firstlatitude / 1000000000.0, (double)gpx->firstlongitude / 1000000000.0, gpxnr);
  
  return true;
}

int64_t
db_find_invisible(void)
{
  int64_t ret = -1;
  MYSQL_RES *res;
  MYSQL_ROW row;
  
  STMT("SELECT id FROM gpx_files WHERE visible=0 LIMIT 1");
  
  res = mysql_store_result(handle);
  if (res != NULL) {
    row = mysql_fetch_row(res);
    if (row != NULL) {
      ret = strtol(row[0], NULL, 0);
    }
    mysql_free_result(res);
  }
  return ret;
}

DBJob *
db_find_work(int minage)
{
  DBJob *ret = NULL;
  MYSQL_RES *res;
  MYSQL_ROW row;
  int64_t user;
  
  STMT("SELECT id, name, description, user_id FROM gpx_files WHERE visible=1 AND inserted=0 AND timestamp <= now() - %d ORDER BY timestamp ASC LIMIT 1", minage);
  res = mysql_store_result(handle);
  if (res != NULL) {
    row = mysql_fetch_row(res);
    if (row != NULL) {
      ret = calloc(1, sizeof(DBJob));
      ret->gpx_id = strtol(row[0], NULL, 0);
      ret->title = BLANKOR(row[1]);
      ret->description = BLANKOR(row[2]);
      user = strtol(row[3], NULL, 0);
    }
    mysql_free_result(res);
  }
  
  if (ret != NULL) {
    /* Attempt to retrieve the email address */
    STMT("SELECT display_name, email FROM users WHERE id=%"PRId64"", user);
    res = mysql_store_result(handle);
    if (res != NULL) {
      row = mysql_fetch_row(res);
      if (row != NULL) {
        int tlen = strlen(row[0]) + strlen(row[1]) + 4; /* space '<' '>' NULL */
        ret->email = malloc(tlen);
        snprintf(ret->email, tlen, "%s <%s>", row[0], row[1]);
      } else {
        db_error(ret, "Unable to find user information for user %"PRId64"", user);
      }
      mysql_free_result(res);
    } else {
      db_error(ret, "Database error while retrieving user information for user %"PRId64"", user);
    }
  }
  
  if (ret != NULL && ret->error == NULL) {
    /* Attempt to retrieve the tags */
    STMT("SELECT COALESCE(GROUP_CONCAT(tag), '') AS tags FROM gpx_file_tags WHERE gpx_id=%"PRId64"", ret->gpx_id);
    res = mysql_store_result(handle);
    if (res != NULL) {
      row = mysql_fetch_row(res);
      if (row != NULL) {
        ret->tags = BLANKOR(row[0]);
      } else {
        db_error(ret, "Unable to retrieve GPX tags for file %"PRId64"\n", ret->gpx_id);
      }
      mysql_free_result(res);
    } else {
      db_error(ret, "Database error while retrieving GPX tags for file %"PRId64"\n", ret->gpx_id);
    }
  }
  
  return ret;
}

bool
db_connect(void)
{
  char *host, *user, *pass, *db;
  int port;
  /* Establish connection to MySQL using environment */
  mysql_library_init(0, NULL, NULL);
  handle = mysql_init(NULL);
  if (handle == NULL)
    return false;
  
  host = getenv("GPX_MYSQL_HOST");
  user = getenv("GPX_MYSQL_USER");
  pass = getenv("GPX_MYSQL_PASS");
  db = getenv("GPX_MYSQL_DB");
  port = (getenv("GPX_MYSQL_PORT") ? atoi(getenv("GPX_MYSQL_PORT")) : 0);
  
  if (mysql_real_connect(handle, host, user, pass, db, port, NULL, 0) == NULL) {
    ERROR("Failure connecting to MySQL server: %s", mysql_error(handle));
    mysql_close(handle);
    mysql_library_end();
    return false;
  }
  
  return true;
}

void
db_disconnect(void)
{
  mysql_close(handle);
  mysql_library_end();
}
