/* gpx-import/src/main.c
 *
 * GPX file importer
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
#include <stdbool.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <inttypes.h>
#include <errno.h>

#include "gpx.h"
#include "db.h"
#include "image.h"
#include "filename.h"
#include "interpolate.h"

static bool needs_quit = false;
static bool needs_logfile_rotate = false;

static void
unlink_pidfile(void)
{
  if (getenv("GPX_PID_FILE") == NULL) {
    return;
  }
  
  unlink(getenv("GPX_PID_FILE"));
}
  
static void
do_pidfile(void)
{
  int childpid;
  FILE *f;
  
  if (getenv("GPX_PID_FILE") == NULL) {
    INFO("No pidfile specified, not daemonising");
    log_reopen();
    return;
  }
  
  f = fopen(getenv("GPX_PID_FILE"), "w");
  if (f == NULL) {
    ERROR("Unable to open pidfile %s", getenv("GPX_PID_FILE"));
    exit(1);
  }
  
  childpid = fork();
  switch(childpid) {
  case -1:
    ERROR("Unable to fork() for child: %s (%d)", strerror(errno), errno);
    exit(1);
    break;
  case 0:
    /* Child */
    setsid();
    setpgid(0, 0);
    log_reopen();
    fclose(stderr);
    break;
  default:
    /* Parent */
    fprintf(f, "%d\n", childpid);
    fclose(f);
    exit(0);
  }
}

static void
do_quit(int ignored)
{
  (void)ignored;
  
  if (needs_quit == true) {
    ERROR("Hard quit!");
    exit(1);
  }
  needs_quit = true;
}

static void
do_logfile(int ignored)
{
  (void)ignored;
  needs_logfile_rotate = true;
}

int
main(int argc, char **argv)
{
  DBJob *job;
  GPX *g;
  int64_t gpxnr;
  int sleep_time = atoi(getenv("GPX_SLEEP_TIME") ? getenv("GPX_SLEEP_TIME") : "0");
  bool did_work = true;
  clock_t cstart, cend;
  
  if (sleep_time == 0) {
    sleep_time = 30;
    WARN("Defaulted sleep time to %d seconds\n", sleep_time);
  }
  
  INFO("Connecting to DB");
  
  if (db_connect() == false) {
    ERROR("Unable to connect to DB");
    return 1;
  }
  
  signal(SIGHUP, do_logfile);
  signal(SIGINT, do_quit);
  signal(SIGTERM, do_quit);
  
  do_pidfile(); /* Note: this will also reopen the logfile nicely */
  
  do {
    if (needs_logfile_rotate == true) {
      log_reopen();
      needs_logfile_rotate = false;
    }
    DEBUG("Looking for work");
    cstart = clock();
    job = db_find_work(sleep_time);
    
    if (job == NULL) {
      /* Found no work, can we find an invisible trace to delete? */
      gpxnr = db_find_invisible();
      if (gpxnr != -1) {
        INFO("Found invisible trace");
        did_work = true;
        db_destroy_trace(gpxnr);
      }
    }
    
    if (job == NULL) {
      if (did_work == true)
        INFO("No work to do, sleeping");
      did_work = false;
      sleep(sleep_time);
      continue;
    }
    did_work = true;
    
    if (job->error == NULL) {
      gpxnr = job->gpx_id;
      INFO("Found job %"PRId64", reading in...", gpxnr);
      g = job->gpx = gpx_parse_file(make_filename("GPX_PATH_TRACES", job->gpx_id, ".gpx"), &(job->error));
      
      if (g != NULL && job->error == NULL) {
        INFO("GPX contained %d good point(s) and %d bad point(s)", g->goodpoints, g->badpoints);
        if (g->badpoints > 0) {
          INFO("%d missed <time>, %d had bad latitude, %d had bad longitude",
               g->missed_time, g->bad_lat, g->bad_long);
        }
        INFO("Creating icon and animation");
        image_generate_icon(g, make_filename("GPX_PATH_IMAGES", gpxnr, "_icon.gif"), 50, 50);
        image_generate_animation(g, make_filename("GPX_PATH_IMAGES", gpxnr, ".gif"), 250, 250, 10);
        
        if (db_insert_gpx(job) == false) {
          db_error(job, "Issue while inserting job into database");
          ERROR("Failure inserting into DB");
        }
      } else {
        if (job->error == NULL)
          job->error = strdup("XML failure while parsing GPX data");
        ERROR("Failure while parsing GPX");
      }
    }
    
    if (job->error == NULL) {
      /* Report success */
      interpolate(job, "import-ok.eml");
    } else {
      /* Report failure */
      interpolate(job, "import-bad.eml");
      
      /* Destroy this item */
      db_destroy_trace(job->gpx_id);
    }
    
    DEBUG("Cleaning up");
    db_free_job(job);
    cend = clock();
    INFO("Import consumed %g CPU seconds", (double)(cend - cstart) / CLOCKS_PER_SEC);
  } while (needs_quit == false);
  
  INFO("Disconnecting from DB");
  
  db_disconnect();
  
  DEBUG("Bye");
  
  log_close();
  
  unlink_pidfile();
  
  return 0;
}
