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

#include <stdio.h>
#include <stdarg.h>
#include <time.h>
#include <string.h>

#include "log.h"

#define LOGBUFLEN 1024

void
_gpxlog(const char *level, const char *fmt, ...)
{
  char buffer[LOGBUFLEN];
  va_list ap;
  time_t ttnow;
  struct tm tmnow;
  
  time(&ttnow);
  gmtime_r(&ttnow, &tmnow);
  
  strftime(buffer, LOGBUFLEN, "[%Y-%m-%dT%H:%M:%SZ] ", &tmnow);
  strcat(buffer, level);
  strcat(buffer, ": ");
  va_start(ap, fmt);
  vsnprintf(buffer + strlen(buffer), LOGBUFLEN - strlen(buffer) - 1, fmt, ap);
  va_end(ap);
  puts(buffer);
}
