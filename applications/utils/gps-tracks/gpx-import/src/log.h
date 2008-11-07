/* gpx-import/src/log.h
 *
 * GPX importer, logging primitives
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

#ifndef GPX_IMPORT_LOG_H
#define GPX_IMPORT_LOG_H

extern void _gpxlog(const char *level, const char *fmt, ...)
  __attribute__ ((format (printf, 2, 3)));



#ifdef NDEBUG
#define DEBUG(X...)
#else
#define DEBUG(X...) _gpxlog("DEBUG", X)
#endif

#define INFO(X...) _gpxlog("INFO", X)
#define WARN(X...) _gpxlog("WARN", X)
#define ERROR(X...) _gpxlog("ERROR", X)


#endif /* GPX_IMPORT_LOG_H */
