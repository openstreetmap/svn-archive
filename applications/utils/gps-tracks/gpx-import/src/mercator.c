/* gpx-import/src/mercator.c
 *
 * GPX Importer, mercator projector
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
#include <math.h>

#include "mercator.h"

#ifndef MAX
#define MAX(a,b) (((a) > (b)) ? (a) : (b))
#endif

struct _MercatorProjection {
  uint32_t height, width;
  double tx, ty, byty, bxtx;
};

static inline double
mercator_double_from_coord(GPXCoord coord)
{
  return ((double)coord) / 1000000000.0;
}

static inline double
mercator_sheet_x(double longitude)
{
  return longitude;
}

static inline double
mercator_sheet_y(double latitude)
{
  if (latitude < -85.0511)
    latitude = -85.0511;
  else if (latitude > 85.0511)
    latitude = 85.0511;
  
  return log(tan(M_PI / 4 + (latitude * M_PI / 180 / 2))) / (M_PI / 180);
}

void
mercator_projection_free(MercatorProjection *projection)
{
  free(projection);
}

void
mercator_projection_project(MercatorProjection *projection,
                            GPXCoord _latitude,
                            GPXCoord _longitude,
                            uint32_t *x,
                            uint32_t *y)
{
  double latitude = mercator_double_from_coord(_latitude);
  double longitude = mercator_double_from_coord(_longitude);
  *x = ((mercator_sheet_x(longitude) - projection->tx) / projection->bxtx) * (double)(projection->width);
  *y = (double)(projection->height) - (((mercator_sheet_y(latitude) - projection->ty) / projection->byty) * (double)(projection->height));
}

MercatorProjection *
mercator_projection_new(GPXCoord _min_latitude,
                        GPXCoord _min_longitude,
                        GPXCoord _max_latitude,
                        GPXCoord _max_longitude,
                        uint32_t width,
                        uint32_t height)
{
  MercatorProjection *ret = calloc(1, sizeof(MercatorProjection));
  double xsize, ysize, xscale, yscale, scale, xpad, ypad, bx, by;
  double min_latitude, min_longitude, max_latitude, max_longitude;
  
  ret->height = height;
  ret->width = width;
  
  min_latitude = mercator_double_from_coord(_min_latitude);
  min_longitude = mercator_double_from_coord(_min_longitude);
  max_latitude = mercator_double_from_coord(_max_latitude);
  max_longitude = mercator_double_from_coord(_max_longitude);
  
  xsize = mercator_sheet_x(max_longitude) - mercator_sheet_x(min_longitude);
  ysize = mercator_sheet_y(max_latitude) - mercator_sheet_y(min_latitude);
  
  xscale = xsize / width;
  yscale = ysize / height;
  
  scale = MAX(xscale, yscale);
  
  xpad = ((double)width * scale) - xsize;
  ypad = ((double)height * scale) - ysize;
  
  ret->tx = mercator_sheet_x(min_longitude) - (xpad / 2);
  ret->ty = mercator_sheet_y(min_latitude) - (ypad / 2);
  
  bx = mercator_sheet_x(max_longitude) + (xpad / 2);
  by = mercator_sheet_y(max_latitude) + (ypad / 2);
  
  ret->byty = by - ret->ty;
  ret->bxtx = bx - ret->tx;
  
  return ret;
}
