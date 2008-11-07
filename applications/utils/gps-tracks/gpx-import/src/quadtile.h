/* gpx-import/src/quadtile.h
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

#ifndef GPX_QUADTILE_H
#define GPX_QUADTILE_H

#include "gpx.h"

extern uint32_t quadtile_for_coords(GPXCoord latitude, GPXCoord longitude);

#endif /* GPX_QUADTILE_H */
