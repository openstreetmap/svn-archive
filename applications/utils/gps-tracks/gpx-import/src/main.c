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

#include "gpx.h"
#include "db.h"
#include "image.h"
#include "filename.h"
#include "interpolate.h"

static bool needs_quit = false;

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
  
  signal(SIGHUP, do_quit);
  signal(SIGINT, do_quit);
  
  do {
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
  
  return 0;
}
