/* gpx-import/src/gpx.h
 *
 * GPX structures and management
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

#ifndef GPX_IMPORT_GPX_H
#define GPX_IMPORT_GPX_H

#include <stdint.h>
#include <sys/time.h>
#include <time.h>

typedef int64_t GPXCoord;

typedef struct _GPXTrackPoint_s {
  GPXCoord longitude, latitude, altitude;
  char *timestamp;
  uint32_t segment;
  float elevation;
  struct _GPXTrackPoint_s *next;
} GPXTrackPoint;

typedef struct {
  GPXCoord firstlatitude, firstlongitude, minlatitude, minlongitude, maxlatitude, maxlongitude;
  uint32_t goodpoints;
  uint32_t badpoints;
  uint32_t missed_time;
  uint32_t bad_lat;
  uint32_t bad_long;
  GPXTrackPoint *points;
} GPX;

extern GPX* gpx_parse_file(const char *gpxfile, char **err);
extern void gpx_free(GPX *gpx);
extern void gpx_print(GPX *gpx);

#endif
