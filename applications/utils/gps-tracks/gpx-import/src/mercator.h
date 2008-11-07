/* gpx-import/src/mercator.h
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

#ifndef GPX_IMPORT_MERCATOR_H
#define GPX_IMPORT_MERCATOR_H

#include "gpx.h"

typedef struct _MercatorProjection MercatorProjection;

extern MercatorProjection *mercator_projection_new(GPXCoord _min_latitude,
                                                   GPXCoord _min_longitude,
                                                   GPXCoord _max_latitude,
                                                   GPXCoord _max_longitude,
                                                   uint32_t width,
                                                   uint32_t height);

extern void mercator_projection_project(MercatorProjection *projection,
                                        GPXCoord _latitude,
                                        GPXCoord _longitude,
                                        uint32_t *x,
                                        uint32_t *y);

extern void mercator_projection_free(MercatorProjection *projection);

#endif
