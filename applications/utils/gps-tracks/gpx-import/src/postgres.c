/* gpx-import/src/postgres.c
 *
 * GPS point insertion into PostgreSQL database, based on the 
 * existing MySQL backend.
 *
 * Copyright Daniel Silverstone <dsilvers@digital-scurf.org>
 *           CloudMade Ltd <matt@cloudmade.com>
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
#include <libpq-fe.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <inttypes.h>

#include "filename.h"
#include "db.h"
#include "quadtile.h"

#define STMT_BUFLEN (1024 * 256)

static PGconn *handle;
static char statement_buffer[STMT_BUFLEN];
static char escape_buffer[STMT_BUFLEN];

/* Postgres has slightly different semantics to MySQL when doing queries
 * than when doing statements, returning different types from the result
 * status function. This means that we can't share code quite as easily
 * between these two macros :-(
 *
 * Both macros handle transactions by immediately aborting them if 
 * something goes wrong. This appears to be in-line with the rest of the
 * semantics in the program.
 */
#define QUERY(R,V...)							\
  snprintf(statement_buffer, STMT_BUFLEN, V);				\
  R = PQexec(handle, statement_buffer);					\
  if (PQresultStatus(R) != PGRES_TUPLES_OK) {				\
    ERROR("Failure executing PostgreSQL query: %s",			\
	  PQresultErrorMessage(R));					\
    PQclear(R);								\
    PQexec(handle, "ROLLBACK");						\
    return false;							\
  }

#define STMT(V...)							\
  do {									\
    PGresult *stmt_result;						\
    snprintf(statement_buffer, STMT_BUFLEN, V);				\
    stmt_result = PQexec(handle, statement_buffer);			\
    if (PQresultStatus(stmt_result) != PGRES_COMMAND_OK) {		\
      ERROR("Failure executing PostgreSQL command: %s",			\
	    PQresultErrorMessage(stmt_result));				\
      PQclear(stmt_result);						\
      PQexec(handle, "ROLLBACK");					\
      return false;							\
    }									\
    PQclear(stmt_result);						\
  } while(0)

#define BLANKOR(S) strdup(((S) ? (S) : ("")))

bool
db_destroy_trace(int64_t jobnr)
{
  STMT("START TRANSACTION");
  INFO("Destroying job %"PRId64"", jobnr);
  STMT("DELETE FROM gpx_file_tags WHERE gpx_id=%"PRId64"", jobnr);
  STMT("DELETE FROM gps_points WHERE gpx_id=%"PRId64"", jobnr);
  STMT("DELETE FROM gpx_files WHERE id=%"PRId64"", jobnr);
  // NOTE: Errors aren't checked here - should they be?
  unlink(make_filename("GPX_PATH_TRACES", jobnr, ".gpx"));
  unlink(make_filename("GPX_PATH_IMAGES", jobnr, "_icon.gif"));
  unlink(make_filename("GPX_PATH_IMAGES", jobnr, ".gif"));
  STMT("COMMIT");
  return true;
}


bool
db_insert_gpx(DBJob *job)
{
  PGresult *result;
  bool do_delete = false;
  GPXTrackPoint *pt;
  int64_t gpxnr = job->gpx_id;
  GPX *gpx = job->gpx;
  
  STMT("START TRANSACTION");
  QUERY(result,"SELECT COUNT(*) FROM gps_points WHERE gpx_id=%"PRId64"", gpxnr);
  if (atoi(PQgetvalue(result, 0, 0)) != 0) {
    do_delete = true;
  }
  PQclear(result);
  
  if (do_delete == true) {
    WARN("Old rows detected, deleting");
    STMT("DELETE FROM gps_points WHERE gpx_id=%"PRId64"", gpxnr);
  }
  
  INFO("Inserting %d points", gpx->goodpoints);
  
  /* Iterate the points, inserting them into the DB */
  for (pt = gpx->points; pt != NULL; pt = pt->next) {
    int string_invalid = 0;
    PQescapeStringConn(handle, escape_buffer, pt->timestamp,
		       strlen(pt->timestamp), &string_invalid);
    if (string_invalid != 0) {
      ERROR("Failed to escape string `%s', possibly invalid byte sequence.",
	    pt->timestamp);
    }
    STMT("INSERT INTO gps_points (gpx_id, trackid, latitude, longitude, timestamp, altitude, tile) " \
         "VALUES (%"PRId64", %d, %"PRId64", %"PRId64", '%s', %f, %u)",
         gpxnr, pt->segment, pt->latitude / 100, pt->longitude / 100, escape_buffer, pt->elevation,
         quadtile_for_coords(pt->latitude, pt->longitude));
  }

  /* Last up, update the GPX with our lat/long/numpoints etc */
  STMT("UPDATE gpx_files SET inserted=true, size=%d, latitude=%g, longitude=%g WHERE id=%"PRId64"\n",
       gpx->goodpoints, (double)gpx->firstlatitude / 1000000000.0, (double)gpx->firstlongitude / 1000000000.0, gpxnr);
  
  STMT("COMMIT");

  return true;
}

int64_t
db_find_invisible(void)
{
  int64_t ret = -1;
  PGresult *result;

  STMT("START TRANSACTION");
  QUERY(result, "SELECT id FROM gpx_files WHERE visible=false LIMIT 1");
  
  if ((PQntuples(result) > 0) &&
      (PQnfields(result) == 1)) {
    ret = strtol(PQgetvalue(result, 0,0), NULL, 0);
  }
  STMT("ROLLBACK");

  PQclear(result);

  return ret;
}

DBJob *
db_find_work(int minage)
{
  DBJob *ret = NULL;
  PGresult *result;
  int64_t user;
  
  STMT("START TRANSACTION");
  QUERY(result, "SELECT id, name, description, user_id FROM gpx_files WHERE visible=true AND inserted=false AND timestamp <= now() - '%d second'::interval ORDER BY timestamp ASC LIMIT 1", minage);
  if ((PQntuples(result) > 0) &&
      (PQnfields(result) == 4)) {
    ret = calloc(1, sizeof(DBJob));
    ret->gpx_id = strtol(PQgetvalue(result, 0, 0), NULL, 0);
    ret->title = BLANKOR(PQgetvalue(result, 0, 1));
    ret->description = BLANKOR(PQgetvalue(result, 0, 2));
    user = strtol(PQgetvalue(result, 0, 3), NULL, 0);
  }

  PQclear(result);
  
  if (ret != NULL) {
    /* Attempt to retrieve the email address */
    QUERY(result, "SELECT display_name, email FROM users WHERE id=%"PRId64"", user);
    if ((PQntuples(result) > 0) &&
	(PQnfields(result) == 2)) {
      const char *name = PQgetvalue(result, 0, 0);
      const char *email = PQgetvalue(result, 0, 1);
      int tlen = strlen(name) + strlen(email) + 4; /* space '<' '>' NULL */
      ret->email = malloc(tlen);
      snprintf(ret->email, tlen, "%s <%s>", name, email);
    } else {
      db_error(ret, "Database error while retrieving user information for user %"PRId64"", user);
    }

    PQclear(result);
  }
  
  if (ret != NULL && ret->error == NULL) {
    /* Attempt to retrieve the tags */
    QUERY(result, "select array_to_string(array(select tag from gpx_file_tags where gpx_id=%"PRId64"),',')", ret->gpx_id);
    if ((PQntuples(result) > 0) &&
	(PQnfields(result) == 1)) {
      ret->tags = BLANKOR(PQgetvalue(result, 0, 0));
    } else {
      db_error(ret, "Database error while retrieving GPX tags for file %"PRId64"\n", ret->gpx_id);
    }
  }
  
  STMT("ROLLBACK");

  return ret;
}

bool
db_connect(void)
{
  char *host, *user, *pass, *db, *port, *options;
  
  host = getenv("GPX_PGSQL_HOST");
  user = getenv("GPX_PGSQL_USER");
  pass = getenv("GPX_PGSQL_PASS");
  db = getenv("GPX_PGSQL_DB");
  port = getenv("GPX_PGSQL_PORT");
  options = getenv("GPX_PGSQL_OPTIONS");
  
  handle = PQsetdbLogin(host, port, options, NULL, db, user, pass);

  if (PQstatus(handle) != CONNECTION_OK) {
    ERROR("Failure connecting to PostgreSQL server: %s", 
	  PQerrorMessage(handle));
    PQfinish(handle);
    return false;
  }
  
  return true;
}

void
db_disconnect(void)
{
  PQfinish(handle);
}
