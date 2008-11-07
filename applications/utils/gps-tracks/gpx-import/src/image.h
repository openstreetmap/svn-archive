/* gpx-import/src/image.h
 *
 * GPX Importer, thumbnail/icon and animation generator
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

#ifndef GPX_IMPORT_IMAGE_H
#define GPX_IMPORT_IMAGE_H

#include "gpx.h"

extern void image_generate_icon(GPX *gpx,
                                const char *outfilename,
                                uint32_t width,
                                uint32_t height);

extern void image_generate_animation(GPX *gpx,
                                     const char *outfilename,
                                     uint32_t width,
                                     uint32_t height,
                                     uint32_t nframes);

#endif /* GPX_IMPORT_IMAGE_H */
