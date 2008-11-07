/* gpx-import/src/quadtile.c
 *
 * GPX importer, quadtile calculation
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

#include <math.h>

#include "quadtile.h"

/* This code is stolen from sites/rails_port/lib/quad_tile/quad_tile.h */

static inline unsigned int
xy2tile(unsigned int x, unsigned int y)
{
   unsigned int tile = 0;
   int          i;

   for (i = 15; i >= 0; i--)
   {
      tile = (tile << 1) | ((x >> i) & 1);
      tile = (tile << 1) | ((y >> i) & 1);
   }

   return tile;
}

static inline unsigned int
lon2x(double lon)
{
   return round((lon + 180.0) * 65535.0 / 360.0);
}

static inline unsigned int
lat2y(double lat)
{
   return round((lat + 90.0) * 65535.0 / 180.0);
}

uint32_t
quadtile_for_coords(GPXCoord latitude, GPXCoord longitude)
{
  return xy2tile(lon2x((double)longitude / 1000000000.0),
                 lat2y((double)latitude / 1000000000.0));
}
